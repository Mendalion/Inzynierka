// server/modules/auth/auth.controller.ts
import { Router } from 'express';
import { z } from 'zod';
import { login, register, issueTokensInternal, refresh } from './auth.service.js';
const router = Router();

const registerSchema = z.object({
  email: z.string().email(),
  password: z.string().min(6),
  name: z.string().optional()
});

const loginSchema = z.object({
  email: z.string().email(),
  password: z.string()
});

const refreshSchema = z.object({
  refreshToken: z.string()
})

router.post('/register', async (req, res) => {
  try {
    const data = registerSchema.parse(req.body);
    const result = await register(data.email, data.password, data.name);
    res.json(result);
  } catch (e: any) {
    res.status(400).json({ error: e.message || 'REGISTRATION_FAILED' });
  }
});

router.post('/login', async (req, res) => {
  try {
    const data = loginSchema.parse(req.body);
    const result = await login(data.email, data.password);
    res.json(result);
  } catch (e: any) {
    res.status(401).json({ error: e.message || 'LOGIN_FAILED' });
  }
});

router.post('/refresh', async (req, res) => {
  try {
    const data = refreshSchema.parse(req.body);
    const result = await refresh(data.refreshToken);
    res.json(result);
  } catch (e: any) {
    res.status(401).json({ error: e.message || 'REFRESH_FAILED' });
  }
});

// Endpoint dla biometrii (placeholder z Twojego kodu)
router.post('/login/biometric', async (req, res) => {
  const userId = req.body.userId;
  if (!userId) return res.status(400).json({ error: 'MISSING_USER_ID' });
  try {
    const tokens = await issueTokensInternal(userId);
    res.json(tokens);
  } catch (e) {
    res.status(400).json({ error: 'FAILED' });
  }
});

export const authController = router;