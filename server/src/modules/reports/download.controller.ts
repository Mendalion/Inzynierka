import { Router } from 'express';
import { authMiddleware } from '../auth/auth.middleware.js';
import { prisma } from '../../db/prisma.js';
import path from 'path';
import fs from 'fs';

const router = Router();

router.get('/:id/download', authMiddleware, async (req, res) => {
  const userId = (req as any).userId;
  const id = req.params.id;
  const report = await prisma.report.findUnique({ where: { id } });
  if (!report) return res.status(404).json({ error: 'NOT_FOUND' });
  if (report.userId !== userId) return res.status(403).json({ error: 'FORBIDDEN' });
  if (report.status !== 'READY' || !report.filePath) return res.status(400).json({ error: 'NOT_READY' });
  const file = path.join(process.cwd(), 'generated', path.basename(report.filePath));
  if (!fs.existsSync(file)) return res.status(404).json({ error: 'FILE_NOT_FOUND' });
  res.download(file);
});

export const downloadController = router;
