import { Request, Response, NextFunction } from 'express';

// Placeholder for optimistic locking based on header 'X-Listing-Version'
export function listingVersionMiddleware(req: Request, res: Response, next: NextFunction) {
  // TODO: implement version check before update
  next();
}
