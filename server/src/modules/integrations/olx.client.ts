// Placeholder OLX client
const OLX_BASE = 'https://api.olx.pl';

interface OlxListing { id: string; title: string; price: number }
interface OlxConversation { conversationId: string; messages: Array<{ sender: string; body: string; sentAt: string }> }

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

export async function fetchOlxListings(accessToken: string): Promise<OlxListing[]> {
  return requestWithRetry(async () => []);
}
export async function fetchOlxMessages(accessToken: string): Promise<OlxConversation[]> {
  return requestWithRetry(async () => []);
}

export function getOlxAuthUrl(state: string) {
  return `https://www.olx.pl/oauth/authorize?client_id=${process.env.OLX_CLIENT_ID}&redirect_uri=${encodeURIComponent(process.env.OLX_REDIRECT_URI!)}&response_type=code&state=${state}`;
}
export async function exchangeOlxCode(code: string) {
  return { accessToken: 'olx_demo_token', expiresIn: 3600 };
}
