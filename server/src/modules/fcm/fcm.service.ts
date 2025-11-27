// Placeholder for Firebase Cloud Messaging integration
import admin from 'firebase-admin';
import { prisma } from '../../db/prisma.js';

if (!admin.apps.length) {
  try {
    admin.initializeApp({ credential: admin.credential.applicationDefault() }); 
  } catch (e) {
    console.warn('FCM init failed (missing credentials)', e);
  }
}

export interface PushPayload {
  type: string;
  [key: string]: any;
}

export async function sendPushToUser(userId: string, payload: PushPayload) {
  const tokens = await prisma.deviceToken.findMany({ where: { userId } });
  for (const t of tokens) {
    try {
      if (admin.apps.length) {
        await admin.messaging().send({ token: t.token, data: Object.fromEntries(Object.entries(payload).map(([k,v]) => [k, String(v)])) });
      }
    } catch (e) {
      console.warn('Push send failed', e);
    }
  }
}