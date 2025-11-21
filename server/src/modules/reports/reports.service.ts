import { prisma } from '../../db/prisma.js';
import { generatePdf } from './pdf.generator.placeholder.js';
import { generateCsv } from './file.generator.js';
import { sendPushToUser } from '../fcm/fcm.service.js';
import PDFDocument from 'pdfkit';
import fs from 'fs';
import path from 'path';

export async function createReportJob(reportId: string) {
  //Symulacja opóźnienia
  setTimeout(async () => {
    try {
      const report = await prisma.report.findUnique({ where: { id: reportId } });
      if (!report) return;

      let filePath: string;
      if (report.type === 'CSV') {
        filePath = await generateCsv(reportId);
      } else {
        filePath = await generatePdf(reportId);
      }

      await prisma.report.update({ 
        where: { id: reportId }, 
        data: { status: 'READY', filePath } 
      });

      await sendPushToUser(report.userId, { 
        type: 'REPORT_READY', 
        reportId 
      });
      
    } catch (e) {
      console.error('Report generation failed', e);
      await prisma.report.update({ 
          where: { id: reportId }, 
          data: { status: 'FAILED' } 
      });
    }
  }, 2000);
}
