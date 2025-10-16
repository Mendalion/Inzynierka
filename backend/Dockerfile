# Etap 1: obraz Node.js
FROM node:20

# Ustaw katalog roboczy
WORKDIR /app

# Skopiuj pliki pakietów i zainstaluj zależności
COPY package*.json ./
RUN npm install --production

# Skopiuj resztę kodu
COPY . .

# Otwórz port
EXPOSE 5000

# Uruchom serwer
CMD ["node", "server.js"]
