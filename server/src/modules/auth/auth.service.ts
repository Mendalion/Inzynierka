import { prisma } from '../../db/prisma.js';
import { hashPassword, verifyPassword } from '../../utils/password.js';
import jwt from 'jsonwebtoken';
import { env } from '../../config/env.js';
import { ensureActiveKey } from './keys.service.js';

export async function register(email: string, password: string, name?: string) {
  const existing = await prisma.user.findUnique({ where: { email } });
  if (existing) throw new Error('EMAIL_TAKEN');

  const passwordHash = await hashPassword(password);
  
  const user = await prisma.user.create({
    data: {
      email,
      passwordHash,
      name
    }
  });

  return issueTokensInternal(user.id);
}

export async function login(email: string, password: string) {
  const user = await prisma.user.findUnique({ where: { email } });
  if (!user) throw new Error('INVALID_CREDENTIALS');

  const isValid = await verifyPassword(user.passwordHash, password);
  if (!isValid) throw new Error('INVALID_CREDENTIALS');

  return issueTokensInternal(user.id);
}

// Funkcja wewnętrzna, eksportowana również dla kontrolera biometrii
export async function issueTokensInternal(userId: string) {
  // Upewnij się, że mamy klucz do podpisywania
  await ensureActiveKey();
  
  const accessToken = jwt.sign({ sub: userId, type: 'access' }, env.JWT_ACCESS_SECRET, {
    expiresIn: env.JWT_ACCESS_EXP,
  });

  const refreshToken = jwt.sign({ sub: userId, type: 'refresh' }, env.JWT_REFRESH_SECRET, {
    expiresIn: env.JWT_REFRESH_EXP,
  });

  //refresh token w bazie
  await prisma.refreshToken.create({
    data: {
      token: refreshToken,
      userId: userId,
      expiresAt: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000) // np. 30 dni
    }
  });

  return { accessToken, refreshToken };
}