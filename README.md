# Android App Skeleton

Implements:
- Auth (login) with EncryptedSharedPreferences
- Listings fetch + display (placeholder)
- WorkManager periodic sync stubs
- FCM Messaging service placeholder
- Compose navigation skeleton

Next Steps:
1. Implement message sync & templates
2. Implement stats & reports UI
3. Add image upload & edit listing form
4. Integrate biometric login unlocking tokens
5. Add proper notifications channel & rendering

//zmieniłem sobie agp = "8.8.0" w libs.versions.toml
i w build.gradle.kts
//    compileSdk {
//        version = release(36)
//    }
    compileSdk = 36

git clone -b olxAllegro  https://github.com/Mendalion/Inzynierka 
cd Inzynierka
docker-compose up
(cd) server
    //dodac plik .env na wzór .envexample
    (jednorazowo)nmp install
    npm run prisma:migrate
    npm run dev  
    npx tsx src/test_full.ts //testy
do mobileApp
zmien ewntualnie w pliku local.properties bo jest sdk.dir=E\:\\androidStudioSDK

    

listingRepository zrobic by przerwalo gdyby nie siadło
https://github.com/Mendalion/Inzynierka/tree/refactor-r