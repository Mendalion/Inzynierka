import { Listing } from '@prisma/client';
import { ListingDTO } from './listings.types.js';

export function toListingDTO(l: any): ListingDTO {
  return {
    id: l.id,
    title: l.title,
    description: l.description,
    price: l.price,
    status: l.status,
    categoryId: l.categoryId,
    categoryName: l.categoryName,
    images: l.images?.map((i: any) => ({ id: i.id, url: i.url })) || [],
    platformStates: l.platformStates?.map((ps: any) => ({ platform: ps.platform, status: ps.status, platformListingId: ps.platformListingId })) || []
  };
}
