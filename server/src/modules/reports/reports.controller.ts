import { Router } from 'express';
import { authMiddleware } from '../auth/auth.middleware.js';
import { prisma } from '../../db/prisma.js';
import { audit } from '../audit/audit.service.js';
import { createReportJob } from './reports.service.js';
import { z } from 'zod';

const router = Router();

// Schemat walidacji danych wejściowych
const createReportSchema = z.object({
  type: z.enum(['CSV', 'PDF']),
  rangeStart: z.string().datetime(), // oczekuje formatu ISO 8601
  rangeEnd: z.string().datetime()
});

router.post('/', authMiddleware, async (req, res) => {
  const userId = req.userId!;
  
  try {
    //Walidacja danych
    const data = createReportSchema.parse(req.body);
    
    //Utworzenie wpisu w bazie (status PENDING)
    const report = await prisma.report.create({
      data: {
        userId,
        type: data.type,
        rangeStart: data.rangeStart,
        rangeEnd: data.rangeEnd,
        status: 'PENDING'
      }
    });

    //Uruchomienie zadania w tle (generowanie pliku)
    // Nie czekamy na await, żeby nie blokować odpowiedzi
    createReportJob(report.id).catch(console.error);

    //Logowanie akcji (Audyt)
    await audit(userId, 'report', report.id, 'CREATE_REPORT', { type: data.type });

    res.json(report);
  } catch (e: any) {
    res.status(400).json({ error: e.message });
  }
});

export const reportsController = router;