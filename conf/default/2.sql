# --- !Ups

INSERT INTO "category"("name") VALUES("sample1");
INSERT INTO "category"("name") VALUES("sample2");
-- INSERT INTO "user"("name", "address") VALUES ("sampleuser1", "sampleaddress1");
-- INSERT INTO "user"("name", "address") VALUES ("sampleuser2", "sampleaddress2");

# --- !Downs

DELETE FROM "category" WHERE name="sample1"";
DELETE FROM "category" WHERE name="sample2"";
-- DELETE FROM "user" WHERE name="sampleuser1";
-- DELETE FROM "user" WHERE name="sampleuser2";
