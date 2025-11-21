import { Router } from 'express';
import { authMiddleware } from '../auth/auth.middleware.js';
import { prisma } from '../../db/prisma.js';
import { toConversationDTO, toMessageDTO } from './messages.mapper.js';
import { z } from 'zod';

const router = Router();

// Schema dla wysyłania odpowiedzi
const replySchema = z.object({
  body: z.string().min(1)
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