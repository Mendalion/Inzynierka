import { PrismaClient } from '@prisma/client';

export const prisma = new PrismaClient();

export async function initDb() {
  const MAX_RETRIES = 20;
  const DELAY_MS = 3000;

  for (let i = 0; i < MAX_RETRIES; i++) {
    try {
      await prisma.$connect();
      console.log('Połączono z bazą danych.');
      return;
    } catch (error: any) {
      await new Promise((resolve) => setTimeout(resolve, DELAY_MS));
    }
  }

  throw new Error('Nie udało się połączyć z bazą danych');
}
