import { Router } from 'express';
import { authMiddleware } from '../auth/auth.middleware.js';
import { prisma } from '../../db/prisma.js';
import { Platform, Prisma, ListingStatus } from '@prisma/client'; 
import { toListingDTO } from './listings.mapper.js';
import { createAllegroDraft, fetchCategoryParameters, getAllegroOffer, updateAllegroOffer, getMyAllegroOffers } from '../integrations/allegro.client.js';
import { z } from 'zod';


const router = Router();
const updateSchema = z.object({ 
    title: z.string().optional(), 
    description: z.string().optional(), 
    price: z.number().optional(),
    parameterValues: z.record(z.any()).optional(),
    images: z.array(z.object({ url: z.string() })).optional()
});

router.get('/categories/:categoryId/parameters', authMiddleware, async (req, res) => {
    const userId = (req as any).userId;
    const { categoryId } = req.params;
    try {
        const integration = await prisma.userIntegration.findFirst({
            where: { userId: userId, platform: 'ALLEGRO' }
        });
        if (!integration || !integration.accessToken) {
            return res.status(400).json({ error: 'Brak integracji z Allegro.' });
        }
        const params = await fetchCategoryParameters(integration.accessToken, categoryId);
        res.json(params);
    } catch (e: any) {
        console.error("Błąd pobierania parametrów:", e);
        res.status(500).json({ error: e.message });
    }
});

router.get('/', authMiddleware, async (req, res) => {
  const userId = (req as any).userId;
  const listings = await prisma.listing.findMany({ 
      where: { userId },
      include: { images: true, platformStates: true },
      orderBy: { createdAt: 'desc' }
  });
  res.json(listings.map(toListingDTO));
});

router.post('/', authMiddleware, async (req, res) => {
    const userId = (req as any).userId;
    const body = req.body; 

    try {
        let createdAllegroId: string | null = null;
        let selectedPlatform: Platform | null = null;

        //Jesli frontend przysłał productId (bo znalazł po EAN), używamy go
        const productIdFromFrontend = body.productId || null;

        // Wybór platformy
        if (body.platform && Object.values(Platform).includes(body.platform as Platform)) {
             selectedPlatform = body.platform as Platform;
        } else if (body.platforms && Array.isArray(body.platforms) && body.platforms.length > 0) {
             const first = body.platforms[0];
             if (Object.values(Platform).includes(first as Platform)) {
                 selectedPlatform = first as Platform;
             }
        }

        if (!selectedPlatform) {
            return res.status(400).json({ error: "Wymagana jest poprawna platforma (np. ALLEGRO)" });
        }

        const imagesPayload = body.images || body.photos || [];
        const formattedImages = imagesPayload.map((img: any) => ({ url: img.url || img }));

        if (selectedPlatform === Platform.ALLEGRO) {
             const integration = await prisma.userIntegration.findFirst({
                where: { userId, platform: 'ALLEGRO' }
            });
            
            if (integration) {
                const paramDefs = await fetchCategoryParameters(integration.accessToken, body.categoryId);

                const offerParams: any[] = [];
                //nie zbieramy productParams, bo API ich nie przyjmie bez ID produktu

                if (body.parameterValues) {
                    Object.entries(body.parameterValues).forEach(([paramId, value]) => {
                        const valStr = String(value);
                        if (!valStr) return;

                        const def = paramDefs.find((p:any) => p.id === paramId);
                        if (!def) return; 

                        const isDictionary = def.type === 'dictionary';
                        const paramObj = {
                            id: paramId,
                            valuesIds: isDictionary ? [valStr] : [],
                            values: isDictionary ? [] : [valStr]
                        };

                        const isProductParam = def.options?.describesProduct === true;
                        
                        if (isProductParam) {
                            // SKIP: Ignorujemy parametry produktu EAN aby uniknąć błędu 500
                            // poniewaz nie mamy ID produktu, nie możemy ich wysłać.
                        } else {
                            offerParams.push(paramObj);
                        }
                    });
                }

                //tylko parametry oferty
                const draft = await createAllegroDraft(integration.accessToken, {
                    title: body.title,
                    description: body.description,
                    price: String(body.price),
                    categoryId: body.categoryId,
                    location: {
                        city: "Warszawa",
                        zipCode: "00-001",
                        state: "MAZOWIECKIE",
                        countryCode: "PL"
                    },
                    offerParameters: offerParams,
                    productId: productIdFromFrontend,
                    // Nie wysyłamy productParameters
                    images: formattedImages
                });
                
                createdAllegroId = draft.id;
                console.log("Utworzono szkic Allegro ID:", createdAllegroId);
            }
        }

        //zapis do bazy danych prisma
        const priceDecimal = new Prisma.Decimal(body.price);

        const listing = await prisma.listing.create({
            data: {
                userId,
                ...(body.id ? { id: body.id } : {}), 
                title: body.title,
                description: body.description,
                price: priceDecimal,
                status: ListingStatus.DRAFT,
                categoryId: body.categoryId,
                
                platformStates: {
                    create: [
                        {
                            platform: selectedPlatform,
                            status: ListingStatus.DRAFT,
                            platformListingId: createdAllegroId || `PENDING_${Date.now()}` 
                        }
                    ]
                },
                attributes: body.parameterValues || {},
                images: {
                    create: formattedImages
                }
            },
            include: {
                platformStates: true,
                images: true
            }
        });

        res.json(toListingDTO(listing));

    } catch (e: any) {
        console.error("Błąd tworzenia ogłoszenia:", e);
        res.status(500).json({ error: e.message });
    }
});

router.post('/import/allegro', authMiddleware, async (req, res) => {
    const userId = (req as any).userId;

    try {
        const integration = await prisma.userIntegration.findFirst({
            where: { userId, platform: 'ALLEGRO' }
        });

        if (!integration || !integration.accessToken) {
            return res.status(400).json({ error: "Brak integracji z Allegro" });
        }

        const allegroOffers = await getMyAllegroOffers(integration.accessToken);
        let importedCount = 0;
        let updatedCount = 0;

        for (const offer of allegroOffers) {
            const allegroId = offer.id;
            
            const price = offer.sellingMode?.price?.amount || "0";
            
            let description = offer.name;
            if (offer.description && offer.description.sections) {
                 const textItem = offer.description.sections
                    .flatMap((s:any) => s.items)
                    .find((i:any) => i.type === 'TEXT');
                 if (textItem) description = textItem.content;
            }

            const attributesJson: any = {};
            if (offer.parameters) {
                offer.parameters.forEach((p: any) => {
                    let val = null;
                    if (p.valuesIds && p.valuesIds.length > 0) val = p.valuesIds[0];
                    else if (p.values && p.values.length > 0) val = p.values[0];
                    
                    if (val !== null) {
                        attributesJson[p.id] = val;
                    }
                });
            }

            let targetStatus: any = 'DRAFT'; // Domyślnie szkic
            if (offer.publication.status === 'ACTIVE') targetStatus = 'ACTIVE';
            else if (offer.publication.status === 'ENDED') targetStatus = 'ARCHIVED';

            //Sprawdź czy mamy to ogłoszenie
            const existingState = await prisma.listingPlatformState.findFirst({
                where: {
                    platform: 'ALLEGRO',
                    platformListingId: allegroId
                },
                include: { listing: true }
            });
            
            const categoryId = offer.category?.id || null;

            const commonData = {
                title: offer.name,
                price: new Prisma.Decimal(price),
                description: description,
                attributes: attributesJson,
                categoryId: categoryId
            };

            if (existingState) {
                //UPDATE
                await prisma.listing.update({
                    where: { id: existingState.listingId },
                    data: commonData
                });

                //usuwamy stare zdjęcia z bazy (żeby nie dublować) i dodajemy aktualne z Allegro
                await prisma.listingImage.deleteMany({
                    where: { listingId: existingState.listingId }
                });

                if (offer.images && offer.images.length > 0) {
                    await prisma.listingImage.createMany({
                        data: offer.images.map((img: any) => ({
                            listingId: existingState.listingId,
                            url: img.url
                        }))
                    });
                }
                updatedCount++;
            } else {
                // INSERT
                await prisma.listing.create({
                    data: {
                        userId,
                        ...commonData,
                        status: offer.publication.status === 'ACTIVE' ? 'ACTIVE' : 'DRAFT',
                        platformStates: {
                            create: {
                                platform: 'ALLEGRO',
                                status: targetStatus,
                                platformListingId: allegroId
                            }
                        },
                        images: {
                            create: offer.images?.map((img: any) => ({ url: img.url })) || []
                        }
                    }
                });
                importedCount++;
            }
        }

        res.json({ success: true, imported: importedCount, updated: updatedCount });

    } catch (e: any) {
        console.error("Import error:", e);
        res.status(500).json({ error: e.message });
    }
});

router.get('/:id', authMiddleware, async (req, res) => {
  try {
    const userId = (req as any).userId;
    const id = req.params.id;
    
    const listing = await prisma.listing.findUnique({ 
        where: { id }, 
        include: { images: true, platformStates: true } 
    });
    
    if (!listing) return res.status(404).json({ error: 'NOT_FOUND' });
    if (listing.userId !== userId) return res.status(403).json({ error: 'FORBIDDEN' });

    const responseDTO = toListingDTO(listing);

    const allegroState = listing.platformStates.find(ps => ps.platform === 'ALLEGRO' && ps.platformListingId && !ps.platformListingId.startsWith('PENDING'));

    if (allegroState) {
        try {
            const integration = await prisma.userIntegration.findFirst({
                where: { userId, platform: 'ALLEGRO' }
            });

            if (integration && integration.accessToken) {
                const allegroData: any = await getAllegroOffer(integration.accessToken, allegroState.platformListingId);
                
                responseDTO.externalDetails = {
                    allegro: {
                        id: allegroData.id,
                        status: allegroData.publication.status,
                        price: allegroData.sellingMode.price.amount,
                        stock: allegroData.stock.available,
                        webUrl: `https://allegro.pl.allegrosandbox.pl/oferta/${allegroData.id}` 
                    }
                };
            }
        } catch (err) {
            console.error("Błąd pobierania danych live z Allegro:", err);
        }
    }

    res.json(responseDTO);
  } catch (e: any) { res.status(400).json({ error: e.message }); }
});

router.patch('/:id', authMiddleware, async (req, res) => {
  try {
    const userId = (req as any).userId;
    const id = req.params.id;

    const data = updateSchema.parse(req.body);

    const existingListing = await prisma.listing.findUnique({ where: { id } });
    if (!existingListing) return res.status(404).json({ error: 'NOT_FOUND' });
    if (existingListing.userId !== userId) return res.status(403).json({ error: 'FORBIDDEN' });

    const updateData: any = { 
        title: data.title,
        description: data.description 
    };
    
    if (data.price !== undefined) {
        updateData.price = new Prisma.Decimal(data.price);
    }

    //Mapowanie parameterValues
    if (data.parameterValues) {
        updateData.attributes = data.parameterValues;
    }

    //aktualizacja bazy danych u nas
    await prisma.listing.update({ 
        where: { id }, 
        data: updateData 
    });

    if (data.images) {
        await prisma.listingImage.deleteMany({ where: { listingId: id } });
        
        if (data.images.length > 0) {
            await prisma.listingImage.createMany({
                data: data.images.map(img => ({
                    listingId: id,
                    url: img.url
                }))
            });
        }
    }

    const updatedListing = await prisma.listing.findUnique({ 
        where: { id }, 
        include: { 
            images: true,
            platformStates: true
        } 
    });

    if (!updatedListing) throw new Error("Błąd pobierania zaktualizowanego ogłoszenia");

    // Sprawdzamy, czy oferta jest połączona z Allegro i nie jest w trakcie tworzenia PENDING
    const allegroState = updatedListing.platformStates.find(
        ps => ps.platform === 'ALLEGRO' && 
        ps.platformListingId && 
        !ps.platformListingId.startsWith('PENDING')
    );

    if (allegroState) {
        try {
            const integration = await prisma.userIntegration.findFirst({
                where: { userId, platform: 'ALLEGRO' }
            });

            if (integration && integration.accessToken) {
                console.log(`[SYNC] Wysyłam aktualizację do Allegro dla oferty: ${allegroState.platformListingId}`);
                
                await updateAllegroOffer(
                    integration.accessToken, 
                    allegroState.platformListingId, 
                    {
                        title: updatedListing.title,
                        price: Number(updatedListing.price),
                        description: updatedListing.description,
                        images: updatedListing.images,
                        attributes: updatedListing.attributes 
                    }
                );
                console.log("[SYNC] Sukces aktualizacji Allegro");
            }
        } catch (allegroError: any) {
            console.error("[SYNC] Błąd aktualizacji Allegro:", allegroError.message);
        }
    }

    res.json(toListingDTO(updatedListing));

  } catch (e: any) { 
      console.error("Listing Update Error:", e);
      res.status(400).json({ error: e.message }); 
  }
});

router.delete('/:id', authMiddleware, async (req, res) => {
    try {
        const userId = (req as any).userId;
        const id = req.params.id;
        const listing = await prisma.listing.findUnique({ where: { id } });
        if (!listing) return res.status(404).json({ error: 'NOT_FOUND' });
        if (listing.userId !== userId) return res.status(403).json({ error: 'FORBIDDEN' });
        await prisma.listing.delete({ where: { id } });
        res.json({ success: true });
    } catch (e: any) { res.status(400).json({ error: e.message }); }
});

export const listingsController = router;