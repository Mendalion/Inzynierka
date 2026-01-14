import { prisma } from '../../db/prisma.js';
import { sendPushToUser } from '../fcm/fcm.service.js';
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
    let convs: Array<{ conversationId: string; messages: Array<{ sender: string; body: string; sentAt: string }> }> = []; 

    for (const c of convs) {
        const conversation = await prisma.messageConversation.upsert({
            where: { platform_platformConversationId: { platform: integ.platform, platformConversationId: c.conversationId } },
            update: { updatedAt: new Date() },
            create: { userId, platform: integ.platform, platformConversationId: c.conversationId, lastMessageAt: new Date() }
        });

        for (const m of c.messages) {
            const msgId = m.sentAt + '_' + c.conversationId + '_' + m.sender;
            const exists = await prisma.message.findUnique({ where: { id: msgId } });

            if (!exists) {
                await prisma.message.create({
                    data: { 
                        id: msgId, 
                        conversationId: conversation.id, 
                        sender: m.sender, 
                        body: m.body, 
                        sentAt: new Date(m.sentAt) 
                    }
                });

                if (m.sender !== 'ME') {
                    console.log(`Nowa wiadomość od ${m.sender}! Wysyłam push.`);
                    await sendPushToUser(userId, "Nowa wiadomość Allegro", m.body, {
                        conversationId: conversation.id,
                        type: "NEW_MESSAGE"
                    });
                }
            }
        }
    }
  }
}
