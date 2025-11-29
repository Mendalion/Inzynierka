import PDFDocument from 'pdfkit';
import fs from 'fs';
import path from 'path';

export async function generatePdf(reportId: string) {
  const dir = path.join(process.cwd(), 'generated');
  if (!fs.existsSync(dir)) fs.mkdirSync(dir);
  const filePath = path.join(dir, `${reportId}.pdf`);
  return new Promise<string>((resolve, reject) => {
    const doc = new PDFDocument();
    const stream = fs.createWriteStream(filePath);
    doc.pipe(stream);
    doc.fontSize(20).text('Report ' + reportId);
    doc.moveDown().fontSize(12).text('Generated at: ' + new Date().toISOString());
    doc.end();
    stream.on('finish', () => resolve(filePath));
    stream.on('error', reject);
  });
}
