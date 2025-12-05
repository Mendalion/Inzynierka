import { Router } from 'express';
import { authMiddleware } from '../auth/auth.middleware.js';
import { prisma } from '../../db/prisma.js';
// import { fetchAllegroListings } from '../integrations/allegro.client.js';
import { fetchOlxListings } from '../integrations/olx.client.js';

const router = Router();
router.post('/', authMiddleware, async (req, res) => {
  const userId = (req as any).userId;
  const platform = req.body.platform as 'ALLEGRO' | 'OLX';
  if (!platform) return res.status(400).json({ error: 'MISSING_PLATFORM' });
  const integ = await prisma.userIntegration.findFirst({ where: { userId, platform } });
  if (!integ) return res.status(400).json({ error: 'NO_INTEGRATION' });
  let listings: any[] = [];
  // if (platform === 'ALLEGRO') listings = await fetchAllegroListings(integ.accessToken);
  if (platform === 'OLX') listings = await fetchOlxListings(integ.accessToken);
  // Upsert stub
  for (const l of listings) {
    await prisma.listing.upsert({
      where: { id: l.id },
      update: { title: l.title, price: l.price.toFixed(2) },
      create: { id: l.id, userId, title: l.title, description: l.title, price: l.price.toFixed(2) }
    });
  }
  res.json({ imported: listings.length });
});

export const importController = router;

