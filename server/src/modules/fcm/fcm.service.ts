import admin from 'firebase-admin';
import { prisma } from '../../db/prisma.js';

if (!admin.apps.length) {
  admin.initializeApp({
    credential: admin.credential.applicationDefault()
  });
}

export async function sendPushToUser(userId: string, title: string, body: string, data: Record<string, string> = {}) {
  const tokens = await prisma.deviceToken.findMany({ where: { userId } });
  
  const uniqueTokens = [...new Set(tokens.map(t => t.token))];

  if (uniqueTokens.length === 0) return;

  const message: admin.messaging.MulticastMessage = {
    tokens: uniqueTokens,
    notification: {
      title: title,
      body: body,
    },
    data: data,
    android: {
      priority: 'high',
      notification: {
        sound: 'default',
        clickAction: 'FLUTTER_NOTIFICATION_CLICK',
      }
    }
  };

  try {
    const response = await admin.messaging().sendMulticast(message);
    console.log(`Wysłano powiadomienia: ${response.successCount} sukcesów, ${response.failureCount} błędów.`);
  } catch (e) {
    console.error('Błąd wysyłania FCM:', e);
  }
}