import { z } from 'zod';
export const reportCreateSchema = z.object({
  type: z.enum(['CSV','PDF']),
  rangeStart: z.string().refine(v => !isNaN(Date.parse(v)), 'Invalid date'),
  rangeEnd: z.string().refine(v => !isNaN(Date.parse(v)), 'Invalid date')
}).refine(d => new Date(d.rangeStart) <= new Date(d.rangeEnd), 'rangeStart must be <= rangeEnd');

