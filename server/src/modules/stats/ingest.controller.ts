import { Router } from 'express';
import { authMiddleware } from '../auth/auth.middleware.js';
import { recordView, recordSale } from './ingest.service.js';
import { z } from 'zod';

const router = Router();
const schema = z.object({ listingId: z.string() });
router.post('/view', authMiddleware, async (req, res) => {
  try {
    const { listingId } = schema.parse(req.body);
    await recordView(listingId);
    res.json({ ok: true });
  } catch (e: any) { res.status(400).json({ error: e.message }); }
});

router.post('/sale', authMiddleware, async (req, res) => {
  try {
    const { listingId } = schema.parse(req.body);
    await recordSale(listingId);
    res.json({ ok: true });
  } catch (e: any) { res.status(400).json({ error: e.message }); }
});

export const ingestController = router;

