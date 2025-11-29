import { prisma } from '../../db/prisma.js';

export async function audit(userId: string | null, entity: string, entityId: string | null, action: string, diff?: any) {
  await prisma.auditLog.create({ data: { userId: userId || undefined, entity, entityId: entityId || undefined, action, diff } });
}
