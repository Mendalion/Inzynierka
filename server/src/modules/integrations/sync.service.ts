// Sync service stub for listings & messages
import { prisma } from '../../db/prisma.js';
import { fetchAllegroListings, fetchAllegroMessages } from './allegro.client.js';
import { fetchOlxListings, fetchOlxMessages } from './olx.client.js';

export async function syncListings(userId: string) {
  const integrations = await prisma.userIntegration.findMany({ where: { userId } });
  for (const integ of integrations) {
    let fetched: Array<{ id: string; title: string; price: number }> = [];
    if (integ.platform === 'ALLEGRO') fetched = await fetchAllegroListings(integ.accessToken);
    else if (integ.platform === 'OLX') fetched = await fetchOlxListings(integ.accessToken);
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
    let convs: Array<{ conversationId: string; messages: Array<{ sender: string; body: string; sentAt: string }> }>= [];
    if (integ.platform === 'ALLEGRO') convs = await fetchAllegroMessages(integ.accessToken);
    else if (integ.platform === 'OLX') convs = await fetchOlxMessages(integ.accessToken);
    for (const c of convs) {
      const conv = await prisma.messageConversation.upsert({
        where: { platform_platformConversationId: { platform: integ.platform, platformConversationId: c.conversationId } },
        update: { updatedAt: new Date() },
        create: { userId, platform: integ.platform, platformConversationId: c.conversationId }
      });
      for (const m of c.messages) {
        await prisma.message.upsert({
          where: { id: m.sentAt + '_' + c.conversationId + '_' + m.sender },
          update: { },
          create: { id: m.sentAt + '_' + c.conversationId + '_' + m.sender, conversationId: conv.id, sender: m.sender, body: m.body, sentAt: new Date(m.sentAt) }
        });
      }
    }
    if (convs.length) console.log('Synced messages', integ.platform, convs.length);
  }
}
