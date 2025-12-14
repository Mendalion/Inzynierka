# Android App Skeleton
//zmieniłem sobie agp = "8.8.0" w libs.versions.toml
i w build.gradle.kts
//    compileSdk {
//        version = release(36)
//    }
    compileSdk = 36

nowy sposób
cd Inzynierka

w serverze -> npx prisma migrate dev

docker-compose up

docker-compose stop server
docker-compose rm -f server
docker-compose build server
docker-compose start server  docker-compose up -d server


git clone -b olxAllegro  https://github.com/Mendalion/Inzynierka 
cd Inzynierka
docker-compose up
(cd) server
    //dodac plik .env na wzór .envexample
    (jednorazowo)nmp install
    npm run prisma:migrate //ewentualnie to do czyszczenia i aktualizacji: npx prisma migrate reset
    npm run dev  
    npx tsx src/test_full.ts //testy
do mobileApp
zmien ewntualnie w pliku local.properties bo jest sdk.dir=E\:\\androidStudioSDK

    

listingRepository zrobic by przerwalo gdyby nie siadło
https://github.com/Mendalion/Inzynierka/tree/refactor-r