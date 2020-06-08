# --- !Ups
INSERT INTO "category"("name") VALUES("sample1");
INSERT INTO "category"("name") VALUES("sample2");
INSERT INTO "product"("name", "description", "category") VALUES ("product1", "description1", 1);
INSERT INTO "product"("name", "description", "category") VALUES ("product2", "description2", 1);
INSERT INTO "product"("name", "description", "category") VALUES ("product3", "description3", 2);
INSERT INTO "product"("name", "description", "category") VALUES ("product4", "description4", 2);

# --- !Downs
DELETE FROM "product";
DELETE FROM "category";
