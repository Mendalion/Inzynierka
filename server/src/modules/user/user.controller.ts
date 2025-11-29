import { Router } from 'express';
import { authMiddleware } from '../auth/auth.middleware.js';
import { prisma } from '../../db/prisma.js';
import { audit } from '../audit/audit.service.js';
import { z } from 'zod';

const router = Router();
const updateProfileSchema = z.object({
  name: z.string().optional(),
  phone: z.string().optional(),
  //pózniej sie zrobi
});

router.patch('/me', authMiddleware, async (req, res) => {
  try {
    const userId = (req as any).userId;
    const data = updateProfileSchema.parse(req.body);

    const updatedUser = await prisma.user.update({
      where: { id: userId },
      data: {
        name: data.name,
        phone: data.phone
      }
    });

    await audit(userId, 'user', userId, 'UPDATE_PROFILE', data);
    res.json(updatedUser);
  } catch (e) {
    res.status(400).json({ error: e });
  }
});

router.get('/me/export', authMiddleware, async (req, res) => {
  try {
    const userId = (req as any).userId;
    const user = await prisma.user.findUnique({ 
      where: { id: userId }, 
      include: { 
        listings: true, 
        conversations: true, 
        messageTemplates: true, 
        reports: true 
      } 
    });
    res.json({ user });
  } catch (e: any) {
    res.status(500).json({ error: e.message });
  }
})

router.delete('/me', authMiddleware, async (req, res) => {
  try {
    const userId = (req as any).userId!;

    const userListings = await prisma.listing.findMany({
      where: { userId },
      select: { id: true }
    });
    const listingIds = userListings.map(l => l.id);

    const userConversations = await prisma.messageConversation.findMany({
        where: { userId },
        select: { id: true }
    });
    const conversationIds = userConversations.map(c => c.id);

    await prisma.$transaction([//translakcja zapewnia ze usunie sie wszystko albo nic
      prisma.listingImage.deleteMany({ where: { listingId: { in: listingIds } } }),
      prisma.listingPlatformState.deleteMany({ where: { listingId: { in: listingIds } } }),
      prisma.statsView.deleteMany({ where: { listingId: { in: listingIds } } }),
      prisma.statsSale.deleteMany({ where: { listingId: { in: listingIds } } }),
      
      prisma.message.deleteMany({ where: { conversationId: { in: conversationIds } } }),
      prisma.listing.deleteMany({ where: { userId } }),
      prisma.messageConversation.deleteMany({ where: { userId } }),
      prisma.userIntegration.deleteMany({ where: { userId } }),
      prisma.messageTemplate.deleteMany({ where: { userId } }),
      prisma.report.deleteMany({ where: { userId } }),

      prisma.refreshToken.deleteMany({ where: { userId } }),
      prisma.deviceToken.deleteMany({ where: { userId } }),

      prisma.user.delete({ where: { id: userId } })
    ]);
    res.json({ erased: true });
  } catch (e: any) {
    res.status(500).json({ error: e.message });
  }
});

router.get('/me', authMiddleware, async (req, res) => {
  try {
    const userId = (req as any).userId;
    const user = await prisma.user.findUnique({ where: { id: userId } });
    if (!user) return res.status(404).json({ error: 'NOT_FOUND' });
    res.json({ id: user.id, email: user.email, name: user.name, phone: user.phone });
  } catch (e: any) { res.status(400).json({ error: e.message }); }
});

export const userController = router;