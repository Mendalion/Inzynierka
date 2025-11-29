import { Router } from 'express';
import { Platform } from '@prisma/client';
import { authMiddleware } from '../auth/auth.middleware.js';
import { prisma } from '../../db/prisma.js';
import { toConversationDTO, toMessageDTO } from './messages.mapper.js';
import { z } from 'zod';

const router = Router();

// Schema dla wysyłania odpowiedzi
const replySchema = z.object({
  body: z.string().min(1)
});

const templateSchema = z.object({
  title: z.string().min(1),
  body: z.string().min(1)
});

//Do TESTÓW potem usunac
//Do TESTÓW potem usunac
//Do TESTÓW potem usunac
const createInternalSchema = z.object({
  email: z.string().email(),
  body: z.string().min(1),
  sender: z.string().default('SYSTEM'),
  platform: z.nativeEnum(Platform).default(Platform.ALLEGRO), // Domyślnie Allegro
  platformConversationId: z.string().optional()
});

router.get('/unread/count', authMiddleware, async (req, res) => {
  const userId = req.userId!;
  try {
    const conversations = await prisma.messageConversation.findMany({
        where: { userId },
        select: { id: true }
    });
    const convIds = conversations.map(c => c.id);
    
    const count = await prisma.message.count({
        where: {
            conversationId: { in: convIds },
            sender: { not: 'ME' },
            isRead: false
        }
    });
    res.json({ unread: count });
  } catch (e: any) { res.status(500).json({ error: e.message }); }
});

router.get('/templates', authMiddleware, async (req, res) => {
    const userId = req.userId!;
    try {
        const templates = await prisma.messageTemplate.findMany({ where: { userId } });
        res.json(templates.map(t => ({ id: t.id, title: t.title, body: t.body }))); 
    } catch (e: any) { res.status(500).json({ error: e.message }); }
});

router.post('/templates', authMiddleware, async (req, res) => {
    const userId = req.userId!;
    try {
        const data = templateSchema.parse(req.body);
        const t = await prisma.messageTemplate.create({
            data: { userId, title: data.title, body: data.body } 
        });
        res.json({ id: t.id, title: t.title, body: t.body });
    } catch (e: any) { res.status(400).json({ error: e.message }); }
});

router.delete('/templates/:id', authMiddleware, async (req, res) => {
    const userId = req.userId!;
    const id = req.params.id;
    try {
        const t = await prisma.messageTemplate.findUnique({ where: { id }});
        if (!t || t.userId !== userId) return res.status(403).json({ error: 'FORBIDDEN' });
        
        await prisma.messageTemplate.delete({ where: { id } });
        res.json({ ok: true });
    } catch (e: any) { res.status(400).json({ error: e.message }); }
});

//Do TESTÓW potem usunac
//Do TESTÓW potem usunac
//Do TESTÓW potem usunac
router.post('/internal/conversations', async (req, res) => {
  try {
    const data = createInternalSchema.parse(req.body);
    const user = await prisma.user.findUnique({
        where: { email: data.email }
    });
    if (!user) {
        return res.status(404).json({ error: `Nie znaleziono użytkownika o emailu: ${data.email}` });
    }

    const mockPlatformId = data.platformConversationId || `MOCK-${Date.now()}-${Math.random().toString(36).substring(7).toUpperCase()}`;  
    const conv = await prisma.messageConversation.create({
      data: {
        userId: user.id,
        platform: data.platform,
        platformConversationId: mockPlatformId,
        lastMessageAt: new Date(),
        unreadCount: 1,
        messages: {
          create: {
            sender: data.sender,
            body: data.body,
            sentAt: new Date(),
            isRead: false
          }
        }
      },
      include: {
        messages: true
      }
    });

    res.json(toConversationDTO(conv));
  } catch (e: any) {
    res.status(400).json({ error: e.message });
  }
});

//Pobierz listę konwersacji
router.get('/conversations', authMiddleware, async (req, res) => {
  const userId = req.userId!;
  try {
    const conversations = await prisma.messageConversation.findMany({
      where: { userId },
      orderBy: { lastMessageAt: 'desc' }
    });
    res.json(conversations.map(toConversationDTO));
  } catch (e: any) {
    res.status(500).json({ error: e.message });
  }
});

//Pobierz szczegóły konwersacji wraz z wiadomościami
router.get('/conversations/:id', authMiddleware, async (req, res) => {
  const userId = req.userId!;
  const { id } = req.params;

  try {
    const conv = await prisma.messageConversation.findUnique({
      where: { id },
      include: { 
        messages: {
          orderBy: { sentAt: 'asc' }
        } 
      }
    });

    if (!conv) return res.status(404).json({ error: 'NOT_FOUND' });
    if (conv.userId !== userId) return res.status(403).json({ error: 'FORBIDDEN' });

    res.json({ 
      ...toConversationDTO(conv), 
      messages: conv.messages.map(toMessageDTO) 
    });
  } catch (e: any) {
    res.status(500).json({ error: e.message });
  }
});

//Wyślij odpowiedź (symulacja - zapisuje w bazie)
router.post('/conversations/:id/reply', authMiddleware, async (req, res) => {
  const userId = req.userId!;
  const { id } = req.params;

  try {
    const { body } = replySchema.parse(req.body);

    const conv = await prisma.messageConversation.findUnique({ where: { id } });
    if (!conv) return res.status(404).json({ error: 'NOT_FOUND' });
    if (conv.userId !== userId) return res.status(403).json({ error: 'FORBIDDEN' });

    // Tworzymy nową wiadomość
    const msg = await prisma.message.create({
      data: {
        conversationId: id,
        sender: 'ME',
        body: body,
        sentAt: new Date(),
        isRead: true
      }
    });

    // Aktualizujemy czas ostatniej wiadomości w konwersacji
    await prisma.messageConversation.update({
      where: { id },
      data: { lastMessageAt: new Date() }
    });

    res.json(toMessageDTO(msg));
  } catch (e: any) {
    res.status(400).json({ error: e.message });
  }
});

export const messagesController = router;