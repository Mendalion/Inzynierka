import { env } from '../../config/env.js';

const ALLEGRO_API_BASE = 'https://api.allegro.pl.allegrosandbox.pl';
const ALLEGRO_AUTH_BASE = 'https://allegro.pl.allegrosandbox.pl/auth/oauth';

function sleep(ms: number) { return new Promise(r => setTimeout(r, ms)); }

async function requestWithRetry<T>(fn: () => Promise<T>, retries = 3): Promise<T> {
  let attempt = 0;
  while (true) {
    try { return await fn(); } catch (e) {
      if (attempt++ >= retries) throw e;
      await sleep(300 * attempt);
    }
  }
}

export async function searchAllegroProducts(accessToken: string, query: string) {
    // query to może być EAN (np. "97883...") lub nazwa
    // mode=GTIN oznacza szukanie po kodzie kreskowym, jeśli query to liczby
    // mode=PHRASE szuka po nazwie
    
    const mode = /^\d+$/.test(query) ? 'GTIN' : 'PHRASE'; 
    const url = `/sale/products?${mode}=${encodeURIComponent(query)}&language=pl-PL`;

    return requestWithRetry(async () => {
        const data: any = await allegroFetch(url, accessToken);
        return data.products || [];
    });
}

async function allegroFetch(endpoint: string, accessToken: string, options: RequestInit = {}) {
  const res = await fetch(`${ALLEGRO_API_BASE}${endpoint}`, {
    ...options,
    headers: {
      'Authorization': `Bearer ${accessToken}`,
      'Accept': 'application/vnd.allegro.public.v1+json',
      'Content-Type': 'application/vnd.allegro.public.v1+json',
      ...options.headers,
    },
  });
  if (!res.ok) {
    const errorBody = await res.text();
    console.error(`[ALLEGRO API ERROR] ${res.status} ${res.statusText}:`, errorBody);
    throw new Error(`Allegro API Error ${res.status}: ${errorBody}`);
  }
  return res.json();
}

// --- Definicje Typów ---

export interface AllegroParameter {
    id: string; 
    name: string; 
    type: string; 
    required: boolean; 
    dictionary?: { id: string; value: string }[]; 
    unit?: string;
    options?: {
        describesProduct?: boolean; // Kluczowa flaga z JSONa
        variantsAllowed?: boolean;
    }; 
}

export interface AllegroDraftPayload {
    title: string;
    description: string;
    price: string;
    categoryId: string;
    location: {
        city: string;
        zipCode: string;
        state: string;
        countryCode: string;
    };
    // Przyjmujemy tylko parametry oferty, bo produktowych bez ID produktu nie wyślemy
    offerParameters?: Array<{ id: string; valuesIds: string[]; values: string[] }>;
    productId?: string;
}

// --- Funkcje ---

export async function fetchCategoryParameters(accessToken: string, categoryId: string): Promise<AllegroParameter[]> {
    return requestWithRetry(async () => {
        const data: any = await allegroFetch(`/sale/categories/${categoryId}/parameters`, accessToken);
        return data.parameters
            .filter((p: any) => p.type !== 'dictionary' || p.dictionary) 
            .map((p: any) => ({
                id: p.id,
                name: p.name,
                type: p.type,
                required: p.required,
                unit: p.unit,
                dictionary: p.dictionary ? p.dictionary.map((d: any) => ({ id: d.id, value: d.value })) : undefined,
                options: p.options // Przekazujemy options (describesProduct)
            }));
    });
}

export async function createAllegroDraft(accessToken: string, payload: AllegroDraftPayload) {
    console.log(`[ALLEGRO-CLIENT] Tworzę szkic oferty: "${payload.title}" w kat. ${payload.categoryId}`);
    
    return requestWithRetry(async () => {
        const body: any = {
            name: payload.title,
            category: { id: payload.categoryId },
            description: {
                sections: [
                    {
                        items: [{ type: 'TEXT', content: `<p>${payload.description}</p>` }]
                    }
                ]
            },
            // Tutaj wpadnie tylko "Stan" (11323) i inne parametry oferty
            parameters: payload.offerParameters || [],
            sellingMode: {
                format: 'BUY_NOW',
                price: { amount: payload.price, currency: 'PLN' }
            },
            stock: { available: 1, unit: 'UNIT' },
            publication: { status: 'INACTIVE' },
            delivery: {
                // Hardcoded shipping ID - w sandboxie może wymagać zmiany na własny
                shippingRates: { id: 'de2860b6-2581-4217-a066-5125307222ce' }, 
                handlingTime: "PT72H"
            },
            location: {
                city: payload.location.city,
                postCode: payload.location.zipCode,
                countryCode: payload.location.countryCode,
                province: payload.location.state
            },
            payments: { invoice: 'NO_INVOICE' }
        };

        // LOGIKA:
        // Jeśli mamy ID produktu (znaleziony po EAN), wiążemy ofertę z tym produktem.
        // Wtedy NIE wysyłamy productParameters ręcznie, bo one wynikają z ID
        if (payload.productId) {
            body.product = {
                id: payload.productId
            };
        }
        // Jeśli NIE mamy ID (manualne tworzenie), usuwamy product, żeby nie było błędu 500
        // (to jest to co zrobiliśmy w poprzedniej wiadomości)


        // USUWAMY "body.product" CAŁKOWICIE
        // Dzięki temu unikamy błędu "UnknownJSONProperty: product"

        const res = await fetch(`${ALLEGRO_API_BASE}/sale/product-offers`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${accessToken}`,
                'Accept': 'application/vnd.allegro.public.v1+json',
                'Content-Type': 'application/vnd.allegro.public.v1+json'
            },
            body: JSON.stringify(body)
        });

        if (!res.ok) {
            const txt = await res.text();
            console.error("Allegro Draft Error:", txt);
            throw new Error(`Błąd tworzenia szkicu Allegro: ${txt}`);
        }

        const responseData: any = await res.json();
        return { id: responseData.id };
    });
}

// ... (Auth bez zmian)
export function getAllegroAuthUrl(state: string) {
  const redirectUri = process.env.ALLEGRO_REDIRECT_URI;
  if (!redirectUri) throw new Error("Brak ALLEGRO_REDIRECT_URI w pliku .env");
  return `${ALLEGRO_AUTH_BASE}/authorize?response_type=code&client_id=${process.env.ALLEGRO_CLIENT_ID}&redirect_uri=${encodeURIComponent(redirectUri)}&state=${state}`;
}

export async function exchangeAllegroCode(code: string) {
  const redirectUri = process.env.ALLEGRO_REDIRECT_URI;
  const clientId = process.env.ALLEGRO_CLIENT_ID!;
  const clientSecret = process.env.ALLEGRO_CLIENT_SECRET!;
  const authHeader = 'Basic ' + Buffer.from(`${clientId}:${clientSecret}`).toString('base64');

  try {
      const res = await fetch(`${ALLEGRO_AUTH_BASE}/token`, {
        method: 'POST',
        headers: {
            'Authorization': authHeader,
            'Content-Type': 'application/x-www-form-urlencoded' 
        },
        body: new URLSearchParams({
            grant_type: 'authorization_code',
            code: code,
            redirect_uri: redirectUri!
        }).toString()
      });
      if (!res.ok) {
        const txt = await res.text();
        throw new Error(`Allegro Auth Error ${res.status}: ${txt}`);
      }
      const data: any = await res.json();
      return {
        accessToken: data.access_token,
        refreshToken: data.refresh_token,
        expiresIn: data.expires_in
      };
  } catch (error: any) {
      console.error("FETCH ERROR:", error);
      throw error;
  }
}