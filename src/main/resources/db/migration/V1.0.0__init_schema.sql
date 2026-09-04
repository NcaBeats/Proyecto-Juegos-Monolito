CREATE SCHEMA IF NOT EXISTS "public";

-- =============================================
-- GAME
-- =============================================
CREATE TABLE "public"."game" (
    "id"                bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
    "name"              varchar(50) NOT NULL UNIQUE,
    "original_price"    numeric(8, 2) NOT NULL,
    "discount_percent"  integer NOT NULL DEFAULT 0,
    "description"       text NOT NULL,
    "state"             varchar(25) NOT NULL,
    "launch_date"       date NOT NULL,
    "image_url"         varchar(500),
    "banner_url"        varchar(500),
    "minimum_specs"     text,
    "recommended_specs" text,
    "created_at"        timestamp NOT NULL,
    PRIMARY KEY ("id")
);

-- =============================================
-- CATEGORY
-- =============================================
CREATE TABLE "public"."category" (
    "id"        bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
    "name"      varchar(50) NOT NULL UNIQUE,
    "created_at" timestamp NOT NULL,
    PRIMARY KEY ("id")
);

-- =============================================
-- GAME_CATEGORY (join table)
-- =============================================
CREATE TABLE "public"."game_category" (
    "game_id"       bigint NOT NULL,
    "category_id"   bigint NOT NULL,
    PRIMARY KEY ("game_id", "category_id")
);
ALTER TABLE "public"."game_category" ADD CONSTRAINT "fk_game_category_game_id" FOREIGN KEY ("game_id") REFERENCES "public"."game"("id") ON DELETE CASCADE;
ALTER TABLE "public"."game_category" ADD CONSTRAINT "fk_game_category_category_id" FOREIGN KEY ("category_id") REFERENCES "public"."category"("id") ON DELETE CASCADE;

-- =============================================
-- USER
-- =============================================
CREATE TABLE "public"."user" (
    "id"            bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
    "email"         varchar(100) NOT NULL UNIQUE,
    "password"      varchar(255) NOT NULL,
    "created_at"    timestamp NOT NULL,
    "role"          varchar(10) NOT NULL DEFAULT 'CLIENTE',
    "token_version" integer NOT NULL DEFAULT 0,
    PRIMARY KEY ("id")
);

-- =============================================
-- PROFILE
-- =============================================
CREATE TABLE "public"."profile" (
    "user_id"       bigint NOT NULL,
    "nickname"      varchar NOT NULL UNIQUE,
    "avatar_image"  text,
    "bio"           text,
    "visibility"    varchar(10) NOT NULL,
    "run"           varchar(9) NOT NULL UNIQUE,
    "first_name"    varchar(50) NOT NULL,
    "last_name"     varchar(100) NOT NULL,
    "birth_date"    date,
    "region"        varchar(50) NOT NULL,
    "comuna"        varchar(50) NOT NULL,
    "address"       varchar(300) NOT NULL,
    "created_at"    timestamp NOT NULL,
    PRIMARY KEY ("user_id")
);

-- =============================================
-- WALLET
-- =============================================
CREATE TABLE "public"."wallet" (
    "user_id"    bigint NOT NULL,
    "balance"    numeric(8, 2) NOT NULL,
    "updated_at" timestamp NOT NULL,
    "version"    bigint NOT NULL DEFAULT 0,
    PRIMARY KEY ("user_id")
);

-- =============================================
-- LIBRARY
-- =============================================
CREATE TABLE "public"."library" (
    "id"          bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
    "user_id"     bigint NOT NULL,
    "game_id"     bigint NOT NULL,
    "acquired_at" timestamp NOT NULL,
    PRIMARY KEY ("id"),
    UNIQUE ("user_id", "game_id")
);
CREATE INDEX "library_index_2" ON "public"."library" ("game_id");

-- =============================================
-- PURCHASE
-- =============================================
CREATE TABLE "public"."purchase" (
    "id"                bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
    "user_id"           bigint NOT NULL,
    "total_amount"      numeric(8, 2) NOT NULL,
    "status"            varchar(25) NOT NULL,
    "purchased_at"      timestamp NOT NULL,
    "idempotency_key"   varchar(64) NOT NULL,
    PRIMARY KEY ("id"),
    UNIQUE ("user_id", "idempotency_key")
);
CREATE INDEX "purchase_index_2" ON "public"."purchase" ("user_id");

-- =============================================
-- PURCHASE_ITEM
-- =============================================
CREATE TABLE "public"."purchase_item" (
    "id"            bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
    "purchase_id"   bigint NOT NULL,
    "game_id"       bigint NOT NULL,
    "unit_price"    numeric(8, 2) NOT NULL,
    "quantity"      int NOT NULL,
    "subtotal"      numeric(8, 2) NOT NULL,
    PRIMARY KEY ("id"),
    UNIQUE ("purchase_id", "game_id")
);
CREATE INDEX "purchase_item_index_2" ON "public"."purchase_item" ("game_id");

-- =============================================
-- CONTACT
-- =============================================
CREATE TABLE "public"."contact" (
    "id"          bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
    "name"        varchar(100) NOT NULL,
    "email"       varchar(100) NOT NULL,
    "comment"     text NOT NULL,
    "created_at"  timestamp NOT NULL,
    PRIMARY KEY ("id")
);

-- =============================================
-- FOREIGN KEYS
-- =============================================
ALTER TABLE "public"."library"           ADD CONSTRAINT "fk_library_user_id_user_id"             FOREIGN KEY ("user_id")     REFERENCES "public"."user"("id");
ALTER TABLE "public"."wallet"            ADD CONSTRAINT "fk_wallet_user_id_user_id"              FOREIGN KEY ("user_id")     REFERENCES "public"."user"("id");
ALTER TABLE "public"."profile"           ADD CONSTRAINT "fk_profile_user_id_user_id"             FOREIGN KEY ("user_id")     REFERENCES "public"."user"("id");
ALTER TABLE "public"."library"           ADD CONSTRAINT "fk_library_game_id_game_id"             FOREIGN KEY ("game_id")     REFERENCES "public"."game"("id");
ALTER TABLE "public"."purchase"          ADD CONSTRAINT "fk_purchase_user_id_user_id"            FOREIGN KEY ("user_id")     REFERENCES "public"."user"("id");
ALTER TABLE "public"."purchase_item"     ADD CONSTRAINT "fk_purchase_item_purchase_id_purchase_id" FOREIGN KEY ("purchase_id") REFERENCES "public"."purchase"("id");
ALTER TABLE "public"."purchase_item"     ADD CONSTRAINT "fk_purchase_item_game_id_game_id"       FOREIGN KEY ("game_id")     REFERENCES "public"."game"("id");
