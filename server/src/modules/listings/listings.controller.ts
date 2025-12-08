import { Router } from 'express';
import { authMiddleware } from '../auth/auth.middleware.js';
import { prisma } from '../../db/prisma.js';
import { Platform, Prisma, ListingStatus } from '@prisma/client'; 
import { toListingDTO } from './listings.mapper.js';
import { createAllegroDraft, fetchCategoryParameters, getAllegroOffer, updateAllegroOffer } from '../integrations/allegro.client.js';
import { z } from 'zod';

const router = Router();
const updateSchema = z.object({ 
    title: z.string().optional(), 
    description: z.string().optional(), 
    price: z.number().optional() 
});

// GET /listings/categories/:categoryId/parameters (bez zmian)
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

// GET /listings (bez zmian)
router.get('/', authMiddleware, async (req, res) => {
  const userId = (req as any).userId;
  const listings = await prisma.listing.findMany({ 
      where: { userId },
      include: { images: true, platformStates: true },
      orderBy: { createdAt: 'desc' }
  });
  res.json(listings.map(toListingDTO));
});

// POST /listings
router.post('/', authMiddleware, async (req, res) => {
    const userId = (req as any).userId;
    const body = req.body; 

    try {
        let createdAllegroId: string | null = null;
        let selectedPlatform: Platform | null = null;

        // Jeśli frontend przysłał productId (bo znalazł po EAN), używamy go
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

        // --- Logika dla ALLEGRO ---
        if (selectedPlatform === Platform.ALLEGRO) {
             const integration = await prisma.userIntegration.findFirst({
                where: { userId, platform: 'ALLEGRO' }
            });
            
            if (integration) {
                // A. Pobieramy definicje parametrów
                const paramDefs = await fetchCategoryParameters(integration.accessToken, body.categoryId);

                const offerParams: any[] = [];
                // UWAGA: Nie zbieramy productParams, bo API ich nie przyjmie bez ID produktu

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

                        // === FILTRACJA (KLUCZ DO SUKCESU) ===
                        // Sprawdzamy, czy to parametr produktu (używając struktury z JSONa)
                        const isProductParam = def.options?.describesProduct === true;
                        
                        if (isProductParam) {
                            // SKIP: Ignorujemy parametry produktu (Płeć, EAN), aby uniknąć błędu 500
                            // Ponieważ nie mamy ID produktu, nie możemy ich wysłać.
                            // console.log(`Skipping product param: ${def.name} (${paramId})`);
                        } else {
                            // ADD: To jest parametr oferty (np. Stan), wysyłamy go!
                            offerParams.push(paramObj);
                        }
                    });
                }

                // B. Wysyłka do API Allegro (TYLKO parametry oferty)
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
                    productId: productIdFromFrontend
                    // Nie wysyłamy productParameters
                });
                
                createdAllegroId = draft.id;
                console.log("Utworzono szkic Allegro ID:", createdAllegroId);
            }
        }

        // --- Zapis do bazy danych (Prisma) ---
        const priceDecimal = new Prisma.Decimal(body.price);

        const listing = await prisma.listing.create({
            data: {
                userId,
                ...(body.id ? { id: body.id } : {}), 
                title: body.title,
                description: body.description,
                price: priceDecimal,
                status: ListingStatus.DRAFT,
                
                platformStates: {
                    create: [
                        {
                            platform: selectedPlatform,
                            status: ListingStatus.DRAFT,
                            platformListingId: createdAllegroId || `PENDING_${Date.now()}` 
                        }
                    ]
                },
                attributes: body.parameterValues || {} 
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

// ... (GET /:id, PATCH, DELETE bez zmian) ...
router.get('/:id', authMiddleware, async (req, res) => {
  try {
    const userId = (req as any).userId;
    const id = req.params.id;
    
    // 1. Pobierz lokalne dane
    const listing = await prisma.listing.findUnique({ 
        where: { id }, 
        include: { images: true, platformStates: true } 
    });
    
    if (!listing) return res.status(404).json({ error: 'NOT_FOUND' });
    if (listing.userId !== userId) return res.status(403).json({ error: 'FORBIDDEN' });

    const responseDTO = toListingDTO(listing);

    // 2. Sprawdź czy jest powiązane z Allegro
    const allegroState = listing.platformStates.find(ps => ps.platform === 'ALLEGRO' && ps.platformListingId && !ps.platformListingId.startsWith('PENDING'));

    if (allegroState) {
        try {
            // Pobierz token użytkownika
            const integration = await prisma.userIntegration.findFirst({
                where: { userId, platform: 'ALLEGRO' }
            });

            if (integration && integration.accessToken) {
                // 3. Pobierz "żywe" dane z Allegro
                const allegroData: any = await getAllegroOffer(integration.accessToken, allegroState.platformListingId);
                
                // 4. Dołącz do odpowiedzi
                responseDTO.externalDetails = {
                    allegro: {
                        id: allegroData.id,
                        status: allegroData.publication.status, // np. ACTIVE, ENDED
                        price: allegroData.sellingMode.price.amount,
                        stock: allegroData.stock.available,
                        // Link do oferty w Sandboxie (lub produkcji)
                        webUrl: `https://allegro.pl.allegrosandbox.pl/oferta/${allegroData.id}` 
                    }
                };
            }
        } catch (err) {
            console.error("Błąd pobierania danych live z Allegro:", err);
            // Nie przerywamy requestu, po prostu nie dodajemy externalDetails
        }
    }

    res.json(responseDTO);
  } catch (e: any) { res.status(400).json({ error: e.message }); }
});

router.patch('/:id', authMiddleware, async (req, res) => {
  try {
    const userId = (req as any).userId;
    const id = req.params.id;

    // 1. Pobieramy ogłoszenie PRZED zmianami, żeby sprawdzić powiązania
    const listing = await prisma.listing.findUnique({ 
        where: { id },
        include: { platformStates: true } // Ważne: pobierz stany platform
    });

    if (!listing) return res.status(404).json({ error: 'NOT_FOUND' });
    if (listing.userId !== userId) return res.status(403).json({ error: 'FORBIDDEN' });

    // 2. Walidacja danych przychodzących
    const data = updateSchema.parse(req.body);
    const updateData: any = { ...data };
    if (data.price) updateData.price = new Prisma.Decimal(data.price); 

    // 3. --- LOGIKA ALLEGRO START ---
    // Sprawdzamy czy oferta jest wystawiona na Allegro
    const allegroState = listing.platformStates.find(ps => ps.platform === 'ALLEGRO' && ps.platformListingId && !ps.platformListingId.startsWith('PENDING'));

    if (allegroState) {
        try {
            const integration = await prisma.userIntegration.findFirst({
                where: { userId, platform: 'ALLEGRO' }
            });

            if (integration && integration.accessToken) {
                // Wysyłamy zmiany do Allegro
                await updateAllegroOffer(integration.accessToken, allegroState.platformListingId, {
                    title: data.title,
                    price: data.price,
                    description: data.description
                });
                console.log("Zaktualizowano ofertę na Allegro:", allegroState.platformListingId);
            }
        } catch (allegroError) {
            console.error("Nie udało się zaktualizować Allegro:", allegroError);
            // Opcjonalnie: możesz rzucić błąd, jeśli chcesz zablokować zmianę lokalną, 
            // ale zazwyczaj lepiej pozwolić na zmianę lokalną i tylko zalogować błąd API.
        }
    }
    // --- LOGIKA ALLEGRO KONIEC ---

    // 4. Aktualizacja w lokalnej bazie (tak jak było wcześniej)
    await prisma.listing.update({ where: { id }, data: updateData });
    
    // 5. Zwracamy zaktualizowany obiekt
    const updated = await prisma.listing.findUnique({ where: { id }, include: { images: true, platformStates: true } });
    res.json(toListingDTO(updated));

  } catch (e: any) { 
      console.error(e);
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