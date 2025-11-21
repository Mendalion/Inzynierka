import { prisma } from '../../db/prisma.js';

export async function searchListings(userId: string, query: string) {
  return prisma.listing.findMany({ where: { userId, title: { contains: query, mode: 'insensitive' } } });
}
