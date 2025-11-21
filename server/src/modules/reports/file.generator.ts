import fs from 'fs';
import path from 'path';

export function ensureGeneratedDir() {
  const dir = path.join(process.cwd(), 'generated');
  if (!fs.existsSync(dir)) fs.mkdirSync(dir);
  return dir;
}

export async function generateCsv(reportId: string) {
  const dir = ensureGeneratedDir();
  const filePath = path.join(dir, `${reportId}.csv`);
  await fs.promises.writeFile(filePath, 'id,title,metric\n');
  return filePath;
}
