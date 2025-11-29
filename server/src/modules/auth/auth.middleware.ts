import { Request, Response, NextFunction } from 'express';
import jwt from 'jsonwebtoken';
import { env } from '../../config/env.js';

export function authMiddleware(req: Request, res: Response, next: NextFunction) {
  const header = req.headers.authorization;
  if (!header) return res.status(401).json({ error: 'NO_AUTH_HEADER' });

  const [scheme, token] = header.split(' ');
  if (scheme !== 'Bearer' || !token) return res.status(401).json({ error: 'BAD_AUTH_FORMAT' });

  try {
    const payload: any = jwt.verify(token, env.JWT_ACCESS_SECRET);
    (req as any).userId = payload.sub;
    next();
  } catch (err) {
    return res.status(401).json({ error: 'INVALID_OR_EXPIRED_TOKEN' });
  }
}