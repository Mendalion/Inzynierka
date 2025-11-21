// Simple ingest helpers
import { prisma } from '../../db/prisma.js';
export async function recordView(listingId: string) {
  await prisma.statsView.create({ data: { listingId, timestamp: new Date(), count: 1 } });
}

export async function recordSale(listingId: string) {
  await prisma.statsSale.create({ data: { listingId, timestamp: new Date(), count: 1 } });
}

