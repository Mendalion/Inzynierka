export interface ListingDTO {
  id: string;
  title: string;
  description: string;
  price: any;
  status: string;
  images: { id: string; url: string }[];
  platformStates: { platform: string; status: string; platformListingId: string }[];

  externalDetails?: {
    allegro?: {
      id: string;
      status: string;
      price: string;
      stock: number;
      viewsCount?: number;
      webUrl: string;
    }
  };
}
