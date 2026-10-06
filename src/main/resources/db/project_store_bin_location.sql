-- Adds a bin location to project_store: where an item physically sits in a given project's
-- store (e.g. "A-03-2"). Per project + item, since the same item can be binned differently
-- in each store. Plain JDBC-backed table, not managed by Hibernate ddl-auto:update. Run this
-- script manually against the application database.

ALTER TABLE project_store
    ADD COLUMN IF NOT EXISTS bin_location VARCHAR(50);
