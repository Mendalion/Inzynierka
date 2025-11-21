// server/index.ts
import { createApp } from './app.js';
import { initDb } from './db/prisma.js';
import { env } from './config/env.js';
import express from 'express';
import cron from 'node-cron';
import { prisma } from './db/prisma.js';
import { syncListings, syncMessages } from './modules/integrations/sync.service.js';
import { rotateKey } from './modules/auth/keys.service.js';

async function main() {
  await initDb();
  
  const app = createApp();
  
  app.use('/uploads', express.static('uploads'));
  
  app.listen(Number(env.PORT), () => {
    console.log(`🚀 API listening on port ${env.PORT}`);
  });

  //Cron: Synchronizacja (co 15 minut)
  cron.schedule('*/15 * * * *', async () => {
    console.log('Starting sync job...');
    try {
      const users = await prisma.user.findMany({ select: { id: true } });
      for (const u of users) {
        try {
           //Oddzielne try-catch dla usera, żeby błąd jednego nie blokował reszty
           await syncListings(u.id);
           await syncMessages(u.id);
        } catch (err) {
           console.error(`Sync failed for user ${u.id}`, err);
        }
      }
    } catch (e) {
      console.error('Sync job fatal error', e);
    }
  });

  //Cron: Rotacja kluczy JWT (raz dziennie w nocy)
  cron.schedule('0 3 * * *', async () => {
    try {
      await rotateKey();
      console.log('Rotated JWT key');
    } catch (e) {
      console.error('Key rotation failed', e);
    }
  });
}

main().catch(err => {
  console.error('Startup error', err);
  process.exit(1);
});