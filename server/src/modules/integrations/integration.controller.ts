import { Router } from 'express';
import { authMiddleware } from '../auth/auth.middleware.js';
import { prisma } from '../../db/prisma.js';
import { z } from 'zod';
import { getAllegroAuthUrl, exchangeAllegroCode, searchAllegroProducts } from './allegro.client.js';

const router = Router();

const connectSchema = z.object({ platform: z.enum(['ALLEGRO','EBAY']), accessToken: z.string() });
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

router.get('/allegro/products', authMiddleware, async (req, res) => {
    const userId = (req as any).userId;
    const { query } = req.query;

    if (!query || typeof query !== 'string') {
        return res.status(400).json({ error: "Missing query parameter" });
    }

    try {
        const integration = await prisma.userIntegration.findFirst({
             where: { userId, platform: 'ALLEGRO' } 
        });
        
        if (!integration) return res.status(400).json({ error: "No Allegro integration" });

        const products = await searchAllegroProducts(integration.accessToken, query);
        
        const result = products.map((p: any) => ({
            id: p.id,
            name: p.name,
            categoryId: p.category.id,
            images: p.images.map((img: any) => img.url),
            parameters: p.parameters
        }));

        res.json(result);
    } catch (e: any) {
        console.error(e);
        res.status(500).json({ error: e.message });
    }
});

router.get('/', authMiddleware, async (req, res) => {
  const userId = (req as any).userId;
  const integrations = await prisma.userIntegration.findMany({ 
        where: { userId },
        select: { id: true, platform: true, expiresAt: true, lastSyncAt: true }
    });
  res.json(integrations);
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

    switch (platform) {
        case 'ALLEGRO':
            return res.json({ url: getAllegroAuthUrl(state) });
        case 'EBAY':
            return res.status(501).json({ error: 'eBay todo' }); 
        default:
            return res.status(400).json({ error: 'Unsupported platform' });
    }
});

const callbackSchema = z.object({ code: z.string(), state: z.string() });

router.post('/oauth/:platform/callback', authMiddleware, async (req, res) => {
    const userId = (req as any).userId;
    try {
        const platformName = req.params.platform.toUpperCase();
        const { code } = callbackSchema.parse(req.body);

        let tokenData;

        switch (platformName) {
            case 'ALLEGRO':
                tokenData = await exchangeAllegroCode(code);
                break;
            case 'EBAY':
                throw new Error("eBay implementation missing");
            default:
                throw new Error(`Platform ${platformName} not supported`);
        }

        const existing = await prisma.userIntegration.findFirst({ 
            where: { userId, platform: platformName as any } 
        });

        if (existing) {
            await prisma.userIntegration.update({
                where: { id: existing.id },
                data: {
                    accessToken: tokenData.accessToken,
                    refreshToken: tokenData.refreshToken || existing.refreshToken,
                    expiresAt: new Date(Date.now() + tokenData.expiresIn * 1000),
                    updatedAt: new Date(),
                }
            });
        } else {
            await prisma.userIntegration.create({
                data: {
                    userId,
                    platform: platformName as any,
                    accessToken: tokenData.accessToken,
                    refreshToken: tokenData.refreshToken,
                    expiresAt: new Date(Date.now() + tokenData.expiresIn * 1000)
                }
            });
        }

        res.json({ success: true, platform: platformName });
    } catch (e: any) {
        console.error(e);
        res.status(400).json({ error: e.message });
    }
});

export const integrationController = router;