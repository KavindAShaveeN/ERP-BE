-- Adds the "Outside Sell" GIN type — produced goods issued to outside buyers.
-- gin_type is a plain lookup table (see GINTypeRepository), not managed by Hibernate
-- ddl-auto:update. Run this script manually against the application database.

INSERT INTO gin_type (gin_type_name)
SELECT 'Outside Sell'
WHERE NOT EXISTS (
    SELECT 1 FROM gin_type WHERE gin_type_name = 'Outside Sell'
);
