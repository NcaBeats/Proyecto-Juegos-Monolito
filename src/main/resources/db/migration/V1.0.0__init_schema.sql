CREATE SCHEMA IF NOT EXISTS "public";

CREATE TABLE "public"."game" (
    "id" bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
    "name" varchar(50) NOT NULL UNIQUE,
    "price" numeric(8, 2) NOT NULL,
    "description" text NOT NULL,
    "state" varchar(25) NOT NULL,
    "launch_date" date NOT NULL,
    "created_at" timestamp NOT NULL,
    PRIMARY KEY ("id")
);

CREATE TABLE "public"."library" (
    "id" bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
    "user_id" bigint NOT NULL,
    "game_id" bigint NOT NULL,
    "acquired_at" timestamp NOT NULL,
    PRIMARY KEY ("id"),
    UNIQUE ("user_id", "game_id")
);
CREATE INDEX "library_index_2" ON "public"."library" ("game_id");

CREATE TABLE "public"."profile" (
    "user_id" bigint NOT NULL,
    "nickname" varchar NOT NULL UNIQUE,
    "avatar_image" text,
    "bio" text,
    "visibility" varchar(10) NOT NULL,
    "created_at" timestamp NOT NULL,
    PRIMARY KEY ("user_id")
);

CREATE TABLE "public"."user" (
    "id" bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
    "username" varchar(25) NOT NULL UNIQUE,
    "email" varchar(100) NOT NULL UNIQUE,
    "password" varchar(255) NOT NULL,
    "created_at" timestamp NOT NULL,
    "role" varchar(10) NOT NULL DEFAULT 'USER',
    PRIMARY KEY ("id")
);

CREATE TABLE "public"."wallet" (
    "user_id" bigint NOT NULL,
    "balance" numeric(8, 2) NOT NULL,
    "updated_at" timestamp NOT NULL,
    PRIMARY KEY ("user_id")
);

CREATE TABLE "public"."purchase" (
    "id" bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
    "user_id" bigint NOT NULL,
    "total_amount" numeric(8, 2) NOT NULL,
    "status" varchar(25) NOT NULL,
    "purchased_at" timestamp NOT NULL,
    PRIMARY KEY ("id")
);
CREATE INDEX "purchase_index_2" ON "public"."purchase" ("user_id");

CREATE TABLE "public"."purchase_item" (
    "id" bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
    "purchase_id" bigint NOT NULL,
    "game_id" bigint NOT NULL,
    "unit_price" numeric(8, 2) NOT NULL,
    "quantity" int NOT NULL,
    "subtotal" numeric(8, 2) NOT NULL,
    PRIMARY KEY ("id"),
    UNIQUE ("purchase_id", "game_id")
);
CREATE INDEX "purchase_item_index_2" ON "public"."purchase_item" ("game_id");

ALTER TABLE "public"."library" ADD CONSTRAINT "fk_library_user_id_user_id" FOREIGN KEY ("user_id") REFERENCES "public"."user"("id");
ALTER TABLE "public"."wallet" ADD CONSTRAINT "fk_wallet_user_id_user_id" FOREIGN KEY ("user_id") REFERENCES "public"."user"("id");
ALTER TABLE "public"."profile" ADD CONSTRAINT "fk_profile_user_id_user_id" FOREIGN KEY ("user_id") REFERENCES "public"."user"("id");
ALTER TABLE "public"."library" ADD CONSTRAINT "fk_library_game_id_game_id" FOREIGN KEY ("game_id") REFERENCES "public"."game"("id");
ALTER TABLE "public"."purchase" ADD CONSTRAINT "fk_purchase_user_id_user_id" FOREIGN KEY ("user_id") REFERENCES "public"."user"("id");
ALTER TABLE "public"."purchase_item" ADD CONSTRAINT "fk_purchase_item_purchase_id_purchase_id" FOREIGN KEY ("purchase_id") REFERENCES "public"."purchase"("id");
ALTER TABLE "public"."purchase_item" ADD CONSTRAINT "fk_purchase_item_game_id_game_id" FOREIGN KEY ("game_id") REFERENCES "public"."game"("id");
