# --- !Ups

CREATE TABLE "category" (
    "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "name" VARCHAR NOT NULL
);

CREATE TABLE "position" (
    "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "name" VARCHAR NOT NULL
);

CREATE TABLE "employee" (
    "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "name" VARCHAR NOT NULL,
    "position" INT NOT NULL,
    FOREIGN KEY (position) REFERENCES position(id)
);

CREATE TABLE "user" (
    "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "name" VARCHAR NOT NULL,
    "address" VARCHAR NOT NULL
);

CREATE TABLE "supplier" (
    "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "name" VARCHAR NOT NULL,
    "address" VARCHAR NOT NULL
);

CREATE TABLE "loyalty" (
    "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "user" INT NOT NULL,
    FOREIGN KEY (user) REFERENCES user (id),
    "points" INT NOT NULL
);

CREATE TABLE "cart" (
    "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "product" INT NOT NULL,
    FOREIGN KEY (product) REFERENCES product(id),
    "user" INT NOT NULL,
    FOREIGN KEY (user) REFERENCES user(id)
);

CREATE TABLE "product" (
    "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "name" VARCHAR NOT NULL,
    "description" TEXT NOT NULL,
    "category" INT NOT NULL,
    FOREIGN KEY(category) references category(id)
);

CREATE TABLE "payment" (
    "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "user" INT NOT NULL,
    FOREIGN KEY (user) REFERENCES user(id),
    "amount" INT NOT NULL,
    "accountNumber" VARCHAR NOT NULL,
)

CREATE TABLE "order" (
    "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "user" INT NOT NULL,
    FOREIGN KEY (user) REFERENCES user(id),
    "cart" INT NOT NULL,
    FOREIGN KEY (cart) REFERENCES cart(id),
    "payment" INT NOT NULL,
    FOREIGN KEY (payment) REFERENCES payment(id),
)


# --- !Downs

DROP TABLE "category"
DROP TABLE "position"
DROP TABLE "employee"
DROP TABLE "user"
DROP TABLE "supplier"
DROP TABLE "loyalty"
DROP TABLE "cart"
DROP TABLE "product"
DROP TABLE "payment"
DROP TABLE "order"
