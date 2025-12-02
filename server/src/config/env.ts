import dotenv from 'dotenv';
import { z } from 'zod';

dotenv.config();
const envSchema = z.object({
  DATABASE_URL: z.string().url(),
  JWT_ACCESS_SECRET: z.string().min(16),
  JWT_REFRESH_SECRET: z.string().min(16),
  JWT_ACCESS_EXP: z.string().default('15m'),
  JWT_REFRESH_EXP: z.string().default('30d'),
  PORT: z.string().default('4000'),
  ALLEGRO_CLIENT_ID: z.string(),
  ALLEGRO_CLIENT_SECRET: z.string(),
  ALLEGRO_REDIRECT_URI: z.string().url().optional(),
});

const parsed = envSchema.safeParse(process.env);
if (!parsed.success) {
  console.error('Invalid environment variables', parsed.error.flatten());
  process.exit(1);
}

export const env = parsed.data;

