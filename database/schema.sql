CREATE TABLE uzytkownik (
    id_uzytkownika SERIAL PRIMARY KEY,
    imie VARCHAR(50),
    nazwisko VARCHAR(50),
    email VARCHAR(100) UNIQUE NOT NULL,
    haslo VARCHAR(255) NOT NULL,
    telefon VARCHAR(20),
    data_rejestracji TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    typ_uwierzytelniania VARCHAR(20) DEFAULT 'haslo'
);

CREATE TABLE platforma (
    id_platformy SERIAL PRIMARY KEY,
    nazwa VARCHAR(50) NOT NULL,
    url_api VARCHAR(255)
);

CREATE TABLE ogloszenie (
    id_ogloszenia SERIAL PRIMARY KEY,
    tytul VARCHAR(100) NOT NULL,
    opis TEXT,
    cena DECIMAL(10,2),
    status VARCHAR(20) DEFAULT 'aktywne',
    kategoria VARCHAR(50),
    data_dodania TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    id_platformy INT REFERENCES platforma(id_platformy) ON DELETE CASCADE,
    id_uzytkownika INT REFERENCES uzytkownik(id_uzytkownika) ON DELETE CASCADE
);

CREATE TABLE zdjecie (
    id_zdjecia SERIAL PRIMARY KEY,
    url_zdjecia VARCHAR(255) NOT NULL,
    id_ogloszenia INT REFERENCES ogloszenie(id_ogloszenia) ON DELETE CASCADE
);

CREATE TABLE wiadomosc (
    id_wiadomosci SERIAL PRIMARY KEY,
    tresc TEXT NOT NULL,
    data_otrzymania TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_odpowiedzi TIMESTAMP,
    czy_odpowiedziano BOOLEAN DEFAULT FALSE,
    id_uzytkownika INT REFERENCES uzytkownik(id_uzytkownika) ON DELETE CASCADE,
    id_platformy INT REFERENCES platforma(id_platformy) ON DELETE SET NULL
);

CREATE TABLE szablon_wiadomosci (
    id_szablonu SERIAL PRIMARY KEY,
    nazwa_szablonu VARCHAR(100),
    tresc_szablonu TEXT,
    id_uzytkownika INT REFERENCES uzytkownik(id_uzytkownika) ON DELETE CASCADE
);

CREATE TABLE statystyka (
    id_statystyki SERIAL PRIMARY KEY,
    liczba_wyswietlen INT DEFAULT 0,
    liczba_sprzedazy INT DEFAULT 0,
    okres VARCHAR(20),
    id_ogloszenia INT REFERENCES ogloszenie(id_ogloszenia) ON DELETE CASCADE
);

CREATE TABLE raport (
    id_raportu SERIAL PRIMARY KEY,
    typ_raportu VARCHAR(20),
    data_generowania TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    format VARCHAR(10),
    sciezka_plik VARCHAR(255),
    id_uzytkownika INT REFERENCES uzytkownik(id_uzytkownika) ON DELETE CASCADE
);
