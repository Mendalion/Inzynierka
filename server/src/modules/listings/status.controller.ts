import { Router } from 'express';
import { authMiddleware } from '../auth/auth.middleware.js';
import { prisma } from '../../db/prisma.js';
import { audit } from '../audit/audit.service.js';
import { z } from 'zod';

const router = Router();

// Zdefiniowałem brakujący schemat
const schema = z.object({
  status: z.enum(['ACTIVE', 'SOLD', 'ARCHIVED'])
});

router.post('/:id/status', authMiddleware, async (req, res) => {
  const userId = req.userId!; 
  const id = req.params.id;

  try {
    const { status } = schema.parse(req.body);

    const existing = await prisma.listing.findUnique({ where: { id } });
    if (!existing) return res.status(404).json({ error: 'NOT_FOUND' });
    
    if (existing.userId !== userId) return res.status(403).json({ error: 'FORBIDDEN' });

    const listing = await prisma.listing.update({ 
      where: { id }, 
      data: { status } 
    });

    // Opcjonalnie: Logowanie akcji w audycie
    await audit(userId, 'listing', id, 'UPDATE_STATUS', { status });

    res.json(listing);
  } catch (e: any) { 
    res.status(400).json({ error: e.message }); 
  }
});

export const statusController = router;