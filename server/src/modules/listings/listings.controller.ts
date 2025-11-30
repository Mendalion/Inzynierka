// server/modules/listings/listings.controller.ts
import { Router } from 'express';
import { authMiddleware } from '../auth/auth.middleware.js';
import { prisma } from '../../db/prisma.js';
import { toListingDTO } from './listings.mapper.js';
import { z } from 'zod';

const router = Router();
const updateSchema = z.object({ 
    title: z.string().optional(), 
    description: z.string().optional(), 
    price: z.number().optional() 
});

const createSchema = z.object({
    title: z.string(),
    description: z.string(),
    price: z.number(),
    platforms: z.array(z.string()).optional()
});

//Pobierz wszystkie ogłoszenia użytkownika
router.get('/', authMiddleware, async (req, res) => {
  const userId = req.userId!;
  const listings = await prisma.listing.findMany({ 
      where: { userId },
      include: { images: true, platformStates: true },
      orderBy: { createdAt: 'desc' }
  });
  res.json(listings.map(toListingDTO));
});

router.post('/', authMiddleware, async (req, res) => {
    try {
        const userId = req.userId!;
        const data = createSchema.parse(req.body);
        
        const platformStatesData = data.platforms?.map(platformName => ({
            platform: platformName as 'ALLEGRO' | 'OLX',
            status: 'ACTIVE' as const,
            platformListingId: `PENDING_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
        })) || [];

        const listing = await prisma.listing.create({
            data: {
                userId,
                title: data.title,
                description: data.description,
                price: data.price,
                status: 'ACTIVE',
                platformStates: {
                    create: platformStatesData
                }
            },
            include: {
                images: true,
                platformStates: true
            }
        });

        res.json(toListingDTO(listing));
    } catch (e: any) {
        console.error(e);
        res.status(400).json({ error: e.message });
    }
});

router.get('/:id', authMiddleware, async (req, res) => {
  try {
    const userId = req.userId!;
    const id = req.params.id;
    const listing = await prisma.listing.findUnique({ where: { id }, include: { images: true, platformStates: true } });
    
    if (!listing) return res.status(404).json({ error: 'NOT_FOUND' });
    if (listing.userId !== userId) return res.status(403).json({ error: 'FORBIDDEN' });
    
    res.json(toListingDTO(listing));
  } catch (e: any) { res.status(400).json({ error: e.message }); }
});

router.patch('/:id', authMiddleware, async (req, res) => {
  try {
    const userId = req.userId!;
    const id = req.params.id;
    const listing = await prisma.listing.findUnique({ where: { id } });
    
    if (!listing) return res.status(404).json({ error: 'NOT_FOUND' });
    if (listing.userId !== userId) return res.status(403).json({ error: 'FORBIDDEN' });
    
    const data = updateSchema.parse(req.body);
    const updateData: any = { ...data };
    if (data.price) updateData.price = data.price; 

    await prisma.listing.update({ where: { id }, data: updateData });
    
    const updated = await prisma.listing.findUnique({ where: { id }, include: { images: true, platformStates: true } });
    res.json(toListingDTO(updated));
  } catch (e: any) { res.status(400).json({ error: e.message }); }
});

router.delete('/:id', authMiddleware, async (req, res) => {
    try {
        const userId = req.userId!;
        const id = req.params.id;
        const listing = await prisma.listing.findUnique({ where: { id } });

        if (!listing) return res.status(404).json({ error: 'NOT_FOUND' });
        if (listing.userId !== userId) return res.status(403).json({ error: 'FORBIDDEN' });

        await prisma.listing.delete({ where: { id } });
        res.json({ success: true });
    } catch (e: any) {
        res.status(400).json({ error: e.message });
    }
});

export const listingsController = router;