import { Router } from 'express';
import { authMiddleware } from '../auth/auth.middleware.js';
import { syncListings, syncMessages } from './sync.service.js';

const router = Router();
router.post('/listings', authMiddleware, async (req, res) => {
  const userId = (req as any).userId;
  await syncListings(userId);
  res.json({ ok: true });
});

router.post('/messages', authMiddleware, async (req, res) => {
  const userId = (req as any).userId;
  await syncMessages(userId);
  res.json({ ok: true });
});

export const syncController = router;

