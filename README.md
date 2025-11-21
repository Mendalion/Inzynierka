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


docker-compose up
(cd) server
    npm run prisma:migrate
    npm run dev  
    npx tsx src/test_full.ts //testy