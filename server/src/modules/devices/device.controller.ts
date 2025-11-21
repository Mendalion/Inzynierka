import { Router } from 'express';
import { authMiddleware } from '../auth/auth.middleware.js';
import { z } from 'zod';
import { prisma } from '../../db/prisma.js';

const router = Router();

const registerSchema = z.object({ token: z.string() });
router.post('/register', authMiddleware, async (req, res) => {
  try {
    const { token } = registerSchema.parse(req.body);
    const userId = (req as any).userId;
    const existing = await prisma.deviceToken.findUnique({ where: { token } });
    if (existing && existing.userId !== userId) {
      // reassign token to this user (e.g. reinstall) - delete old
      await prisma.deviceToken.delete({ where: { token } });
    }
    await prisma.deviceToken.upsert({ where: { token }, update: { userId }, create: { token, userId } });
    res.json({ ok: true });
  } catch (e: any) { res.status(400).json({ error: e.message }); }
});

export const deviceController = router;
