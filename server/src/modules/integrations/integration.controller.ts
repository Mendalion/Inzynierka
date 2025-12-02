import { Router } from 'express';
import { authMiddleware } from '../auth/auth.middleware.js';
import { prisma } from '../../db/prisma.js';
import { z } from 'zod';
import { getAllegroAuthUrl, exchangeAllegroCode } from './allegro.client.js';
import { getOlxAuthUrl, exchangeOlxCode } from './olx.client.js';

const router = Router();

const connectSchema = z.object({ platform: z.enum(['ALLEGRO','OLX']), accessToken: z.string() });
router.post('/connect', authMiddleware, async (req, res) => {
  const userId = (req as any).userId;
  try {
    const data = connectSchema.parse(req.body);
    const integ = await prisma.userIntegration.create({ data: { userId, platform: data.platform, accessToken: data.accessToken } });
    res.json(integ);
  } catch (e: any) {
    res.status(400).json({ error: e.message });
  }
});



router.get('/', authMiddleware, async (req, res) => {
  const userId = (req as any).userId;
  const list = await prisma.userIntegration.findMany({ where: { userId } });
  res.json(list);
});
router.delete('/:id', authMiddleware, async (req, res) => {
  const id = req.params.id;
  try {
    await prisma.userIntegration.delete({ where: { id } });
    res.json({ ok: true });
  } catch (e: any) {
    res.status(400).json({ error: e.message });
  }
});

router.get('/oauth/:platform/start', authMiddleware, async (req, res) => {
  const platform = req.params.platform.toUpperCase();
  const state = `${platform}_${Date.now()}`;
  if (platform === 'ALLEGRO') return res.json({ url: getAllegroAuthUrl(state) });
  if (platform === 'OLX') return res.json({ url: getOlxAuthUrl(state) });
  res.status(400).json({ error: 'Unsupported platform' });
});

const callbackSchema = z.object({ code: z.string(), state: z.string() });

router.post('/oauth/:platform/callback', authMiddleware, async (req, res) => {
  const userId = (req as any).userId;
  try {
    const platform = req.params.platform.toUpperCase();
    const { code } = callbackSchema.parse(req.body);
    
    let tokenData: { accessToken: string; refreshToken?: string; expiresIn: number };
    
    if (platform === 'ALLEGRO') {
      tokenData = await exchangeAllegroCode(code);
    } else if (platform === 'OLX') {
      tokenData = await exchangeOlxCode(code);
    } else {
      return res.status(400).json({ error: 'Unsupported platform' });
    }

    const existing = await prisma.userIntegration.findFirst({ where: { userId, platform: platform as any } });

    if (existing) {
        const updated = await prisma.userIntegration.update({
            where: { id: existing.id },
            data: {
                accessToken: tokenData.accessToken,
                refreshToken: tokenData.refreshToken || existing.refreshToken,
                expiresAt: new Date(Date.now() + tokenData.expiresIn * 1000),
                updatedAt: new Date(),
            }
        });
        res.json(updated);
    } else {
        const integ = await prisma.userIntegration.create({
            data: {
                userId,
                platform: platform as any,
                accessToken: tokenData.accessToken,
                refreshToken: tokenData.refreshToken,
                expiresAt: new Date(Date.now() + tokenData.expiresIn * 1000)
            }
        });
        res.json(integ);
    }
  } catch (e: any) {
    console.error(e);
    res.status(400).json({ error: e.message });
  }
});

export const integrationController = router;
