import { Router } from 'express';
import { authMiddleware } from '../auth/auth.middleware.js';
import { prisma } from '../../db/prisma.js';
import { audit } from '../audit/audit.service.js'; 

const router = Router();

router.post('/:id/archive', authMiddleware, async (req, res) => {
  const userId = req.userId!;
  const id = req.params.id;

  try {
    const listing = await prisma.listing.findUnique({ where: { id } });

    if (!listing) return res.status(404).json({ error: 'NOT_FOUND' });
    if (listing.userId !== userId) return res.status(403).json({ error: 'FORBIDDEN' });

    //Aktualizacja statusu na ARCHIVED
    const updated = await prisma.listing.update({
      where: { id },
      data: { status: 'ARCHIVED' }
    });

    await audit(userId, 'listing', listing.id, 'ARCHIVE');

    res.json(updated);
  } catch (e: any) {
    res.status(400).json({ error: e.message });
  }
});

router.delete('/:id', authMiddleware, async (req, res) => {
  const userId = req.userId!;
  const id = req.params.id;
  try {
      const listing = await prisma.listing.findUnique({ where: { id } });
      if (!listing) return res.status(404).json({ error: 'NOT_FOUND' });
      if (listing.userId !== userId) return res.status(403).json({ error: 'FORBIDDEN' });

      await prisma.listing.delete({ where: { id } });
      res.json({ ok: true });
  } catch (e: any) { res.status(400).json({ error: e.message }); }
});

export const archiveController = router;