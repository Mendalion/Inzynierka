import fs from 'fs';
import path from 'path';

const BASE_URL = 'http://localhost:4000';

// --- POMOCNICY ---
const c = {
  green: (text: string) => `\x1b[32m${text}\x1b[0m`,
  red: (text: string) => `\x1b[31m${text}\x1b[0m`,
  blue: (text: string) => `\x1b[34m${text}\x1b[0m`,
};

async function sleep(ms: number) { return new Promise(resolve => setTimeout(resolve, ms)); }

async function runTests() {
  console.log(c.blue('Rozpoczynam ZAAWANSOWANE testy API...'));

  // Dane testowe
  const randomId = Math.floor(Math.random() * 99999);
  const user = { email: `pro${randomId}@test.pl`, password: 'password123', name: 'Pro User' };
  let token = '';
  let userId = '';
  let listingId = '';
  let reportId = '';

  // 1. AUTH & SETUP
  await step('Rejestracja i Logowanie', async () => {
    // Rejestracja
    let res = await fetch(`${BASE_URL}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(user)
    });
    let data = await res.json();
    if (!res.ok) throw new Error(data.error);
    token = data.accessToken;

    // Pobranie ID
    res = await authFetch('/user/me');
    data = await res.json();
    userId = data.id;
    console.log(`Użytkownik: ${userId}`);
  });

  //LISTINGI I OBRAZY
  await step('Tworzenie ogłoszenia i dodawanie zdjęcia (URL)', async () => {
    // Utwórz ogłoszenie
    const resList = await authFetch('/listings', {
      method: 'POST',
      body: JSON.stringify({ title: 'Konsola do gier', description: 'Nowa', price: 1500 })
    });
    const listData = await resList.json();
    listingId = listData.id;

    //Dodaj zdjęcie przez URL
    const resImg = await authFetch(`/listings/${listingId}/images`, {
      method: 'POST',
      body: JSON.stringify({ url: 'https://placehold.co/600x400.png' })
    });
    const imgData = await resImg.json();
    
    if (!imgData.id) throw new Error('Nie udało się dodać zdjęcia');
    console.log('Zdjęcie dodane, ID ogłoszenia:', listingId);
  });

  //STATYSTYKI (Ingest & Read)
  await step('Symulacja sprzedaży i sprawdzanie statystyk', async () => {
    // 
  //endpoint ingest.controller.ts używa: z.object({ listingId: z.string() })
  const resIngest = await authFetch('/ingest/sale', {
        method: 'POST',
        body: JSON.stringify({ listingId })
    });
    if(!resIngest.ok) throw new Error((await resIngest.json()).error);

    //Pobierz statystyki (Stats Controller)
    const resStats = await authFetch(`/stats/listings/sales?listingId=${listingId}`);
    const statsData = await resStats.json();
    
    if (!Array.isArray(statsData.sales) || statsData.sales.length === 0) {
        throw new Error('Brak zarejestrowanej sprzedaży w statystykach');
    }
    console.log(`Znaleziono ${statsData.sales.length} wpisów sprzedaży`);
  });

  //RAPORTY (Asynchroniczność)
  await step('Generowanie Raportu PDF (Async Job)', async () => {
    const res = await authFetch('/reports', {
        method: 'POST',
        body: JSON.stringify({ 
            type: 'PDF', 
            rangeStart: new Date(Date.now() - 86400000).toISOString(), // wczoraj
            rangeEnd: new Date().toISOString() 
        })
    });
    const data = await res.json();
    reportId = data.id;
    console.log('Raport zlecony, ID:', reportId);

    await sleep(5000);

    //próbuj pobrać (Download Controller sprawdza czy status READY)
    const resDown = await authFetch(`/reports/${reportId}/download`);
    
    if (resDown.status === 200) {
        console.log(' Plik raportu jest gotowy do pobrania (Status 200)');
    } else {
        const err = await resDown.json();
        throw new Error(`Raport niegotowy: ${err.error}`);
    }
  });

  //USER MANAGEMENT & EXPORT
  await step('Eksport danych użytkownika', async () => {
    const res = await authFetch('/user/me/export');
    const data = await res.json();
    
    // Sprawdź czy export zawiera nasze ogłoszenie
    const hasListing = data.user.listings.some((l: any) => l.id === listingId);
    const hasReport = data.user.reports.some((r: any) => r.id === reportId);

    if (!hasListing || !hasReport) throw new Error('Eksport danych nieprawidłowy');
    console.log('Eksport zawiera poprawne relacje (ogłoszenia, raporty)');
  });

  await step('Aktualizacja profilu', async () => {
      const res = await authFetch('/user/me', {
          method: 'PATCH',
          body: JSON.stringify({ name: 'Zaktualizowany Jan' })
      });
      if (!res.ok) throw new Error('Błąd aktualizacji');
      
      // Weryfikacja
      const check = await authFetch('/user/me');
      const data = await check.json();
      if (data.name !== 'Zaktualizowany Jan') throw new Error('Nazwa nie została zmieniona');
      console.log('Profil zaktualizowany');
  });

  //RESET HASŁA (Symulacja flow)
  await step('Flow Resetu Hasła', async () => {
      //Request
      const reqRes = await fetch(`${BASE_URL}/password/request`, {
          method: 'POST',
          headers: {'Content-Type': 'application/json'},
          body: JSON.stringify({ email: user.email })
      });
      const reqData = await reqRes.json();
      //W kodzie dev zwracamy tokenDev dla łatwości
      const resetToken = reqData.tokenDev; 
      if(!resetToken) throw new Error('Nie otrzymano tokenu resetu (tryb dev)');

      //Reset
      const resetRes = await fetch(`${BASE_URL}/password/reset`, {
          method: 'POST',
          headers: {'Content-Type': 'application/json'},
          body: JSON.stringify({ token: resetToken, newPassword: 'newpassword123' })
      });
      if(!resetRes.ok) throw new Error('Reset hasła nieudany');
      console.log('Hasło zresetowane pomyślnie');
  });

  //USUWANIE KONTA
  await step('Usuwanie konta', async () => {
      const res = await authFetch('/user/me', { method: 'DELETE' });
      const data = await res.json();
      if (!data.erased) throw new Error('Nie udało się usunąć konta');

      // Sprawdź czy token już nie działą
      const check = await authFetch('/user/me');
      if (check.status !== 401 && check.status !== 404) {
          throw new Error('Token nadal działa po usunięciu konta!');
      }
      console.log('Konto usunięte permanentnie');
  });

  console.log('\n' + c.green('Zakonczono'));

  async function step(name: string, fn: () => Promise<void>) {
    process.stdout.write(`-> ${name}... `);
    try {
      await fn();
    } catch (e: any) {
      console.log(c.red('error'));
      console.error(c.red(e.message));
      process.exit(1);
    }
  }

  async function authFetch(endpoint: string, options: RequestInit = {}) {
    return fetch(`${BASE_URL}${endpoint}`, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
        ...options.headers,
      },
    });
  }
}

runTests().catch(console.error);