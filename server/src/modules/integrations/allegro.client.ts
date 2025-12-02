// Placeholder Allegro client
//narazie dla sanboxa
const ALLEGRO_API_BASE = 'https://api.allegro.pl.allegrosandbox.pl';
const ALLEGRO_AUTH_BASE = 'https://allegro.pl.allegrosandbox.pl/auth/oauth';

interface AllegroListing { id: string; title: string; price: number }
interface AllegroConversation { conversationId: string; messages: Array<{ sender: string; body: string; sentAt: string }> }

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
    throw new Error(`Allegro API Error ${res.status}: ${errorBody}`);
  }
  return res.json();
}

export async function fetchAllegroListings(accessToken: string): Promise<AllegroListing[]> {
  return requestWithRetry(async () => {
    // Pobieranie ofert (endpoint /sale/offers)
    const data: any = await allegroFetch('/sale/offers?publication.status=ACTIVE&publication.status=INACTIVE&limit=100', accessToken);
    
    // Mapowanie odpowiedzi Allegro na Twój interfejs
    return data.offers.map((offer: any) => ({
      id: offer.id,
      title: offer.name,
      // Cena może być w sellingMode.price lub promocyjna, bierzemy podstawową
      price: parseFloat(offer.sellingMode?.price?.amount || '0'),
    }));
  });
}

export async function fetchAllegroMessages(accessToken: string): Promise<AllegroConversation[]> {
  return requestWithRetry(async () => {
    // Pobieranie wątków (endpoint /messaging/threads)
    const data: any = await allegroFetch('/messaging/threads?limit=20', accessToken);

    return data.threads.map((thread: any) => ({
      conversationId: thread.id,
      messages: thread.lastMessage ? [{
        // API listy wątków zwraca tylko ostatnią wiadomość. 
        // Aby pobrać wszystkie, trzeba by odpytać endpoint /messaging/threads/{id}/messages dla każdego wątku.
        // Tutaj dla wydajności zwracamy ostatnią jako podgląd.
        sender: thread.lastMessage.author.login,
        body: thread.lastMessage.text,
        sentAt: thread.lastMessage.createdAt
      }] : []
    }));
  });
}

export function getAllegroAuthUrl(state: string) {
  const redirectUri = process.env.ALLEGRO_REDIRECT_URI || 'http://localhost:4000/integrations/oauth/allegro/callback';
  
  return `${ALLEGRO_AUTH_BASE}/authorize?response_type=code&client_id=${process.env.ALLEGRO_CLIENT_ID}&redirect_uri=${encodeURIComponent(redirectUri)}&state=${state}`;
}

export async function exchangeAllegroCode(code: string) {
  const redirectUri = process.env.ALLEGRO_REDIRECT_URI || 'http://localhost:4000/integrations/oauth/allegro/callback';
  const clientId = process.env.ALLEGRO_CLIENT_ID!;
  const clientSecret = process.env.ALLEGRO_CLIENT_SECRET!;

  const authHeader = 'Basic ' + Buffer.from(`${clientId}:${clientSecret}`).toString('base64');

  const url = `${ALLEGRO_AUTH_BASE}/token?grant_type=authorization_code&code=${code}&redirect_uri=${encodeURIComponent(redirectUri)}`;

  const res = await fetch(url, {
    method: 'POST',
    headers: {
      'Authorization': authHeader,
    }
  });

  if (!res.ok) {
    const txt = await res.text();
    throw new Error(`Failed to exchange token: ${txt}`);
  }

  const data: any = await res.json();
  
  return {
    accessToken: data.access_token,
    refreshToken: data.refresh_token,
    expiresIn: data.expires_in
  };
}