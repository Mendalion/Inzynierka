/*
  Warnings:

  - The values [OLX] on the enum `Platform` will be removed. If these variants are still used in the database, this will fail.

*/
-- AlterEnum
ALTER TYPE "ListingStatus" ADD VALUE 'DRAFT';

-- AlterEnum
BEGIN;
CREATE TYPE "Platform_new" AS ENUM ('ALLEGRO', 'EBAY');
ALTER TABLE "UserIntegration" ALTER COLUMN "platform" TYPE "Platform_new" USING ("platform"::text::"Platform_new");
ALTER TABLE "ListingPlatformState" ALTER COLUMN "platform" TYPE "Platform_new" USING ("platform"::text::"Platform_new");
ALTER TABLE "MessageConversation" ALTER COLUMN "platform" TYPE "Platform_new" USING ("platform"::text::"Platform_new");
ALTER TYPE "Platform" RENAME TO "Platform_old";
ALTER TYPE "Platform_new" RENAME TO "Platform";
DROP TYPE "Platform_old";
COMMIT;

-- DropForeignKey
ALTER TABLE "ListingImage" DROP CONSTRAINT "ListingImage_listingId_fkey";

-- DropForeignKey
ALTER TABLE "ListingPlatformState" DROP CONSTRAINT "ListingPlatformState_listingId_fkey";

-- DropForeignKey
ALTER TABLE "StatsSale" DROP CONSTRAINT "StatsSale_listingId_fkey";

-- DropForeignKey
ALTER TABLE "StatsView" DROP CONSTRAINT "StatsView_listingId_fkey";

-- AlterTable
ALTER TABLE "Listing" ADD COLUMN     "attributes" JSONB,
ADD COLUMN     "categoryId" TEXT,
ALTER COLUMN "status" SET DEFAULT 'DRAFT';

-- AlterTable
ALTER TABLE "Message" ADD COLUMN     "attachments" JSONB;

-- AlterTable
ALTER TABLE "MessageConversation" ADD COLUMN     "subject" TEXT;

-- AlterTable
ALTER TABLE "User" ADD COLUMN     "city" TEXT,
ADD COLUMN     "countryCode" TEXT DEFAULT 'PL',
ADD COLUMN     "district" TEXT,
ADD COLUMN     "state" TEXT,
ADD COLUMN     "street" TEXT,
ADD COLUMN     "zipCode" TEXT;

-- AddForeignKey
ALTER TABLE "ListingPlatformState" ADD CONSTRAINT "ListingPlatformState_listingId_fkey" FOREIGN KEY ("listingId") REFERENCES "Listing"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "ListingImage" ADD CONSTRAINT "ListingImage_listingId_fkey" FOREIGN KEY ("listingId") REFERENCES "Listing"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "StatsView" ADD CONSTRAINT "StatsView_listingId_fkey" FOREIGN KEY ("listingId") REFERENCES "Listing"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "StatsSale" ADD CONSTRAINT "StatsSale_listingId_fkey" FOREIGN KEY ("listingId") REFERENCES "Listing"("id") ON DELETE CASCADE ON UPDATE CASCADE;
