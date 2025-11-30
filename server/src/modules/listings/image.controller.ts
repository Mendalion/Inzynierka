import { Router } from 'express';
import multer from 'multer';
import path from 'path';
import fs from 'fs';
import { z } from 'zod';
import { authMiddleware } from '../auth/auth.middleware.js';
import { prisma } from '../../db/prisma.js';

const router = Router();
const upload = multer({ dest: path.join(process.cwd(), 'uploads') });

const schema = z.object({
  url: z.string().url() // Walidacja czy to poprawny URL
});

//Dodawanie zdjęcia przez URL (link zewnętrzny)
router.post('/:id/images', authMiddleware, async (req, res) => {
  const userId = req.userId!;
  const listingId = req.params.id;

  try {
    const existing = await prisma.listing.findUnique({ where: { id: listingId } });
    if (!existing) return res.status(404).json({ error: 'NOT_FOUND' });
    if (existing.userId !== userId) return res.status(403).json({ error: 'FORBIDDEN' });

    const { url } = schema.parse(req.body);
    const img = await prisma.listingImage.create({ data: { listingId, url } });
    res.json(img);
  } catch (e: any) { 
    res.status(400).json({ error: e.message }); 
  }
});

//Usuwanie zdjęcia
router.delete('/:id/images/:imageId', authMiddleware, async (req, res) => {
  const userId = req.userId!;
  const imageId = req.params.imageId;

  try {
    const existing = await prisma.listingImage.findUnique({ where: { id: imageId } });
    if (!existing) return res.status(404).json({ error: 'NOT_FOUND' });

    const listing = await prisma.listing.findUnique({ where: { id: existing.listingId } });
    //Sprawdzamy czy to nasze ogłoszenie
    if (!listing || listing.userId !== userId) return res.status(403).json({ error: 'FORBIDDEN' });

    await prisma.listingImage.delete({ where: { id: imageId } });

    //Jeśli to plik lokalny (z uploads), usuń go z dysku
    if (existing.url.startsWith('/uploads/')) {
      const filePath = path.join(process.cwd(), 'uploads', existing.url.replace('/uploads/', ''));
      if (fs.existsSync(filePath)) {
        try { fs.unlinkSync(filePath); } catch { }
      }
    }
    res.json({ ok: true });
  } catch (e: any) { 
    res.status(400).json({ error: e.message }); 
  }
});

//Upload pliku z dysku
router.post('/:id/images/upload', authMiddleware, upload.single('file'), async (req, res) => {
  const userId = req.userId!;
  const listingId = req.params.id;

  try {
    const existing = await prisma.listing.findUnique({ where: { id: listingId } });
    if (!existing) return res.status(404).json({ error: 'NOT_FOUND' });
    if (existing.userId !== userId) return res.status(403).json({ error: 'FORBIDDEN' });

    if (!req.file) return res.status(400).json({ error: 'NO_FILE' });

    // serwujemy plik statycznie z /uploads
    const publicUrl = `/uploads/${req.file.filename}`; 
    
    const img = await prisma.listingImage.create({ data: { listingId, url: publicUrl } });
    res.json(img);
  } catch (e: any) { 
    res.status(400).json({ error: e.message }); 
  }
});

export const imageController = router;