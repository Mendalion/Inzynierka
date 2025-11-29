import { prisma } from '../../db/prisma.js';
import { generateKeyPairSync } from 'crypto';

export async function ensureActiveKey() {
  const active = await prisma.jwtKey.findFirst({ where: { active: true }, orderBy: { createdAt: 'desc' } });
  if (active) return active;
  return rotateKey();
}

export async function rotateKey() {
  const { publicKey, privateKey } = generateKeyPairSync('rsa', { modulusLength: 2048 });
  const publicPem = publicKey.export({ type: 'pkcs1', format: 'pem' }).toString();
  const privatePem = privateKey.export({ type: 'pkcs1', format: 'pem' }).toString();
  await prisma.jwtKey.updateMany({ where: { active: true }, data: { active: false } });
  const kid = 'kid_' + Date.now();
  return prisma.jwtKey.create({ data: { kid, publicPem, privatePem } });
}

export async function getJwks() {
  const keys = await prisma.jwtKey.findMany({ where: { active: true } });
  return {
    keys: keys.map(k => ({
      kty: 'RSA',
      kid: k.kid,
      alg: 'RS256',
      use: 'sig',
      // simplified for demo
      pem: k.publicPem
    }))
  };
}
