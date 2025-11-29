// Placeholder Allegro client
const ALLEGRO_BASE = 'https://api.allegro.pl';

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

export async function fetchAllegroListings(accessToken: string): Promise<AllegroListing[]> {
  // TODO real fetch
  return requestWithRetry(async () => []);
}
export async function fetchAllegroMessages(accessToken: string): Promise<AllegroConversation[]> {
  return requestWithRetry(async () => []);
}

export function getAllegroAuthUrl(state: string) {
  return `https://allegro.pl/auth/oauth/authorize?response_type=code&client_id=${process.env.ALLEGRO_CLIENT_ID}&redirect_uri=${encodeURIComponent(process.env.ALLEGRO_REDIRECT_URI!)}&state=${state}`;
}
export async function exchangeAllegroCode(code: string) {
  // TODO fetch token
  return { accessToken: 'allegro_demo_token', expiresIn: 3600 };
}
