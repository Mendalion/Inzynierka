import { Request, Response, NextFunction } from 'express';

const buckets = new Map<string, { count: number; reset: number }>();

export function rateLimit(max = 100, windowMs = 60_000) {
  return (req: Request, res: Response, next: NextFunction) => {
    const ip = req.ip || '0.0.0.0'; 
    
    const now = Date.now();
    const bucket = buckets.get(ip);

    if (!bucket || bucket.reset < now) {
      buckets.set(ip, { count: 1, reset: now + windowMs });
      return next();
    }

    if (bucket.count >= max) {
      return res.status(429).json({ error: 'RATE_LIMIT' });
    }

    bucket.count++;
    next();
  };
}