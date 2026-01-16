import { prisma } from '../../db/prisma.js';
import { sendPushToUser } from '../fcm/fcm.service.js';
import { fetchAllegroThreads, fetchAllegroMessagesInThread } from './allegro.client.js';
// import { fetchAllegroListings, fetchAllegroMessages } from './allegro.client.js';

export async function syncListings(userId: string) {
  const integrations = await prisma.userIntegration.findMany({ where: { userId } });
  for (const integ of integrations) {
    let fetched: Array<{ id: string; title: string; price: number }> = [];
    // if (integ.platform === 'ALLEGRO') fetched = await fetchAllegroListings(integ.accessToken);
    // else if (integ.platform === 'OLX') fetched = await fetchOlxListings(integ.accessToken);
    for (const l of fetched) {
      await prisma.listing.upsert({
        where: { id: l.id },
        update: { title: l.title, price: l.price, updatedAt: new Date() },
        create: { id: l.id, userId, title: l.title, description: l.title, price: l.price, status: 'ACTIVE' }
      });
    }
    if (fetched.length) console.log('Synced listings', integ.platform, fetched.length);
  }
}

export async function syncMessages(userId: string) {
  const integrations = await prisma.userIntegration.findMany({ where: { userId } });
  
  for (const integ of integrations) {
    if (integ.platform !== 'ALLEGRO') continue;

    try {
        const threads = await fetchAllegroThreads(integ.accessToken);

        for (const thread of threads) {
            const conversation = await prisma.messageConversation.upsert({
                where: { 
                    platform_platformConversationId: { 
                        platform: integ.platform, 
                        platformConversationId: thread.id 
                    } 
                },
                update: { updatedAt: new Date() },
                create: { 
                    userId, 
                    platform: integ.platform, 
                    platformConversationId: thread.id, 
                    lastMessageAt: new Date(thread.lastMessageDateTime) 
                }
            });

            const messages = await fetchAllegroMessagesInThread(integ.accessToken, thread.id);

            for (const m of messages) {
                const msgId = m.id || `${m.createdAt}_${thread.id}_${m.author.login}`;
                
                const exists = await prisma.message.findUnique({ where: { id: msgId } });

                if (!exists) {
                    await prisma.message.create({
                        data: { 
                            id: msgId, 
                            conversationId: conversation.id, 
                            sender: m.author.login, 
                            body: m.text, 
                            sentAt: new Date(m.createdAt) 
                        }
                    });

                    if (!m.author.isInterlocutor) {
                        console.log(`Nowa wiadomość od ${m.author.login}! Wysyłam push.`);
                        await sendPushToUser(userId, `Wiadomość od ${m.author.login}`, m.text, {
                            conversationId: conversation.id,
                            type: "NEW_MESSAGE"
                        });
                    }
                }
            }
        }
    } catch (error) {
        console.error(`Błąd synchronizacji Allegro dla użytkownika ${userId}:`, error);
    }
  }
}