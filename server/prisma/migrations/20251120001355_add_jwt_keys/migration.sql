-- CreateTable
CREATE TABLE "JwtKey" (
    "id" TEXT NOT NULL,
    "kid" TEXT NOT NULL,
    "publicPem" TEXT NOT NULL,
    "privatePem" TEXT NOT NULL,
    "active" BOOLEAN NOT NULL DEFAULT true,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "JwtKey_pkey" PRIMARY KEY ("id")
);

-- CreateIndex
CREATE UNIQUE INDEX "JwtKey_kid_key" ON "JwtKey"("kid");
