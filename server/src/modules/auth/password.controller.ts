import { Router } from 'express';
import { prisma } from '../../db/prisma.js';
import { z } from 'zod';
import crypto from 'crypto';
import { hashPassword } from '../../utils/password.js';

const router = Router();
const requestSchema = z.object({ email: z.string().email() });
router.post('/request', async (req, res) => {
  try {
    const { email } = requestSchema.parse(req.body);
    const user = await prisma.user.findUnique({ where: { email } });
    if (!user) return res.json({ ok: true }); // do not reveal
    const token = crypto.randomBytes(32).toString('hex');
    const expiresAt = new Date(Date.now() + 1000 * 60 * 30); // 30m
    await prisma.auditLog.create({ data: { userId: user.id, entity: 'password_reset', entityId: user.id, action: 'REQUEST', diff: { token } } });
    // TODO: send email with token
    res.json({ ok: true, tokenDev: token });
  } catch (e: any) {
    res.status(400).json({ error: e.message });
  }
});

const resetSchema = z.object({ token: z.string(), newPassword: z.string().min(8) });
router.post('/reset', async (req, res) => {
  try {
    const { token, newPassword } = resetSchema.parse(req.body);
    // TODO: verify token storage - simplified
    const hash = await hashPassword(newPassword);
    // In real case find user by token
    res.json({ ok: true, hashPreview: hash.substring(0,10) });
  } catch (e: any) {
    res.status(400).json({ error: e.message });
  }
});

export const passwordController = router;

