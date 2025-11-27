import express from 'express';
import cors from 'cors';
import morgan from 'morgan';
import { rateLimit } from './modules/rateLimit.js';
import { getJwks, ensureActiveKey } from './modules/auth/keys.service.js';
import { errorHandler } from './modules/middleware/errorHandler.js';
import { healthController } from './modules/health/health.controller.js';
import { authController } from './modules/auth/auth.controller.js';
import { userController } from './modules/user/user.controller.js';
import { listingsController } from './modules/listings/listings.controller.js';
import { importController } from './modules/listings/import.controller.js';
import { integrationController } from './modules/integrations/integration.controller.js';
import { deviceController } from './modules/devices/device.controller.js';
import { messagesController } from './modules/messages/messages.controller.js';
import { syncController } from './modules/integrations/sync.controller.js';
import { passwordController } from './modules/auth/password.controller.js';
import { imageController } from './modules/listings/image.controller.js';
import { statusController } from './modules/listings/status.controller.js';
import { reportsController } from './modules/reports/reports.controller.js';
import { statsController } from './modules/stats/stats.controller.js';
import { ingestController } from './modules/stats/ingest.controller.js';
import { downloadController } from './modules/reports/download.controller.js';
import { archiveController } from './modules/listings/archive.controller.js';

export function createApp() {
  const app = express();

  app.use(cors());
  app.use(express.json());
  app.use(morgan('dev'));

  app.use('/health', healthController);
  app.use('/auth', rateLimit(50, 60000), authController);
  app.use('/password', passwordController);
  
  app.get('/auth/jwks.json', async (_req, res) => {
    const jwks = await getJwks();
    res.json(jwks);
  });

  app.use('/user', userController);
  app.use('/listings', listingsController);
  app.use('/import', importController);
  app.use('/integrations', integrationController);
  app.use('/devices', deviceController);
  app.use('/messages', messagesController);
  app.use('/sync', syncController);

  app.use('/listings', imageController);
  app.use('/listings', statusController);
  app.use('/listings', archiveController);
  app.use('/stats', statsController);
  app.use('/ingest', ingestController);
  app.use('/reports', reportsController);
  app.use('/reports', downloadController);
  app.use(errorHandler);

  ensureActiveKey().catch(console.error);

  return app;
}