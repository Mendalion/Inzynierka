# server/Dockerfile
FROM node:20-alpine

RUN apk add --no-cache openssl ca-certificates

WORKDIR /app

COPY package*.json ./
COPY prisma ./prisma/

RUN npm install

RUN npx prisma generate

COPY . .

EXPOSE 4000

CMD ["npm", "run", "dev"]