import { Router } from 'express';
import { authMiddleware } from '../auth/auth.middleware.js';
import { prisma } from '../../db/prisma.js';

const router = Router();

router.get('/listings/sales', authMiddleware, async (req, res) => {
  const userId = req.userId!;
  const listingId = req.query.listingId as string | undefined;

  if (!listingId) return res.status(400).json({ error: 'MISSING_LISTING_ID' });

  try {
    const listing = await prisma.listing.findUnique({ where: { id: listingId } });
    
    // Sprawdź czy ogłoszenie istnieje i czy należy do użytkownika
    if (!listing || listing.userId !== userId) {
      return res.status(403).json({ error: 'FORBIDDEN' });
    }

    const sales = await prisma.statsSale.findMany({ 
      where: { listingId }, 
      orderBy: { timestamp: 'asc' } 
    });

    res.json({ listingId, sales });
  } catch (e: any) {
    res.status(500).json({ error: e.message });
  }
});

export const statsController = router;