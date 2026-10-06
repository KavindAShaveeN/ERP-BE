-- Item migration: switch the (still physically named code_master_*) tables from
-- code-based primary/foreign keys to surrogate BIGSERIAL id primary keys with
-- numeric parent_id foreign keys, composite (parent_id, code) uniqueness (global
-- (type, code) for category) — and, as the final step, rename every table/constraint/
-- index from the old "code_master_*" naming to the current "item_*" naming.
--
-- WHY IN-PLACE, NOT DROP-AND-RECREATE:
-- This script assumes the OLDER code_master_schema.sql (the code-PK version) may
-- already have been run against a live database that may already contain data entered
-- through the old /api/code-master/** endpoints. Dropping and recreating the tables
-- would silently destroy that data. This script only ever ADDs/ALTERs/backfills/renames
-- and never issues DROP TABLE or DELETE, so it is safe whether the tables are empty or
-- populated.
--
-- NOTE: nobody has confirmed to the author of this script whether these tables
-- currently hold any rows (no DB access was available while writing it). If you know
-- for a fact the tables are empty, it is simpler to just drop them and run the current
-- item_schema.sql from scratch instead of running this migration. Do that ONLY if you
-- have verified there is no data to lose.
--
-- IF YOU ALREADY RAN A PREVIOUS VERSION OF THIS MIGRATION (tables converted to id-based
-- keys but still named code_master_*), only PHASE 12 below (the renaming) still applies
-- to you — phases 1-11 will simply no-op against the already-migrated columns.
--
-- HOW TO RUN (stops immediately and rolls back on any error):
--   psql "<connection-string>" -v ON_ERROR_STOP=1 -f item_migration_v2_id_primary_keys.sql
--
-- Safe to re-run up until (and including) a successful completion: every step in phases
-- 1-11 is guarded (IF NOT EXISTS / IF EXISTS / existence checks). PHASE 12 (the renaming)
-- is not re-run-safe once it has already succeeded once (the old code_master_* names will
-- no longer exist to rename) — that is fine, since this script is meant to run exactly
-- once per environment.

BEGIN;

-- ---------------------------------------------------------------------------
-- Helper functions (session-local; dropped again at the end of this script).
-- Used instead of hardcoded constraint names, since the exact auto-generated
-- constraint names on the live database were never confirmed.
-- ---------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION pg_temp.drop_all_foreign_keys(target_table text) RETURNS void AS $$
DECLARE
    cname text;
BEGIN
    FOR cname IN
        SELECT conname FROM pg_constraint
        WHERE conrelid = target_table::regclass AND contype = 'f'
    LOOP
        EXECUTE format('ALTER TABLE %s DROP CONSTRAINT %I', target_table, cname);
    END LOOP;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION pg_temp.drop_primary_key(target_table text) RETURNS void AS $$
DECLARE
    cname text;
BEGIN
    SELECT conname INTO cname FROM pg_constraint
    WHERE conrelid = target_table::regclass AND contype = 'p';
    IF cname IS NOT NULL THEN
        EXECUTE format('ALTER TABLE %s DROP CONSTRAINT %I', target_table, cname);
    END IF;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION pg_temp.has_primary_key(target_table text) RETURNS boolean AS $$
    SELECT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conrelid = target_table::regclass AND contype = 'p'
    );
$$ LANGUAGE sql;

-- ---------------------------------------------------------------------------
-- Guard: make sure the tables this migration targets actually exist.
-- ---------------------------------------------------------------------------
DO $$
BEGIN
    IF to_regclass('public.code_master_category') IS NULL THEN
        RAISE EXCEPTION 'code_master_category does not exist. This migration upgrades an existing code-PK schema; for a brand new database just run item_schema.sql instead.';
    END IF;
END $$;

-- ===========================================================================
-- PHASE 1: add surrogate "id" column to every level.
-- ADD COLUMN ... BIGSERIAL backfills sequential ids for existing rows
-- automatically (nextval() is called per-row for pre-existing data).
-- ===========================================================================
ALTER TABLE code_master_category           ADD COLUMN IF NOT EXISTS id BIGSERIAL;
ALTER TABLE code_master_sub_category       ADD COLUMN IF NOT EXISTS id BIGSERIAL;
ALTER TABLE code_master_sub_sub_category   ADD COLUMN IF NOT EXISTS id BIGSERIAL;
ALTER TABLE code_master_brand              ADD COLUMN IF NOT EXISTS id BIGSERIAL;
ALTER TABLE code_master_model              ADD COLUMN IF NOT EXISTS id BIGSERIAL;
ALTER TABLE code_master_optional_one       ADD COLUMN IF NOT EXISTS id BIGSERIAL;
ALTER TABLE code_master_optional_two       ADD COLUMN IF NOT EXISTS id BIGSERIAL;
ALTER TABLE code_master_optional_three     ADD COLUMN IF NOT EXISTS id BIGSERIAL;

-- Rename the category asset-class column to match the requested asset_class naming.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'code_master_category' AND column_name = 'assetclass'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'code_master_category' AND column_name = 'asset_class'
    ) THEN
        ALTER TABLE code_master_category RENAME COLUMN assetclass TO asset_class;
    END IF;
END $$;

-- ===========================================================================
-- PHASE 2: add nullable numeric parent_id columns to every child level.
-- ===========================================================================
ALTER TABLE code_master_sub_category       ADD COLUMN IF NOT EXISTS category_id          BIGINT;
ALTER TABLE code_master_sub_sub_category   ADD COLUMN IF NOT EXISTS sub_category_id      BIGINT;
ALTER TABLE code_master_brand              ADD COLUMN IF NOT EXISTS sub_sub_category_id  BIGINT;
ALTER TABLE code_master_model              ADD COLUMN IF NOT EXISTS brand_id             BIGINT;
ALTER TABLE code_master_optional_one       ADD COLUMN IF NOT EXISTS model_id             BIGINT;
ALTER TABLE code_master_optional_two       ADD COLUMN IF NOT EXISTS optional_one_id      BIGINT;
ALTER TABLE code_master_optional_three     ADD COLUMN IF NOT EXISTS optional_two_id      BIGINT;

-- ===========================================================================
-- PHASE 3: backfill parent_id by matching the OLD parent-code column against
-- the parent table's business code. This is unambiguous because, under the
-- old schema, "code" was each table's PRIMARY KEY (i.e. already globally
-- unique per level), so each old code matches at most one parent row.
-- Guarded with "IF EXISTS column" so re-running after phase 11 (which drops
-- the old columns) is a safe no-op.
-- ===========================================================================
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'code_master_sub_category' AND column_name = 'categorycode') THEN
        UPDATE code_master_sub_category c
        SET category_id = p.id
        FROM code_master_category p
        WHERE c.categorycode = p.code
          AND c.category_id IS NULL;
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'code_master_sub_sub_category' AND column_name = 'subcategorycode') THEN
        UPDATE code_master_sub_sub_category c
        SET sub_category_id = p.id
        FROM code_master_sub_category p
        WHERE c.subcategorycode = p.code
          AND c.sub_category_id IS NULL;
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'code_master_brand' AND column_name = 'subsubcategorycode') THEN
        UPDATE code_master_brand c
        SET sub_sub_category_id = p.id
        FROM code_master_sub_sub_category p
        WHERE c.subsubcategorycode = p.code
          AND c.sub_sub_category_id IS NULL;
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'code_master_model' AND column_name = 'brandcode') THEN
        UPDATE code_master_model c
        SET brand_id = p.id
        FROM code_master_brand p
        WHERE c.brandcode = p.code
          AND c.brand_id IS NULL;
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'code_master_optional_one' AND column_name = 'modelcode') THEN
        UPDATE code_master_optional_one c
        SET model_id = p.id
        FROM code_master_model p
        WHERE c.modelcode = p.code
          AND c.model_id IS NULL;
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'code_master_optional_two' AND column_name = 'optionalonecode') THEN
        UPDATE code_master_optional_two c
        SET optional_one_id = p.id
        FROM code_master_optional_one p
        WHERE c.optionalonecode = p.code
          AND c.optional_one_id IS NULL;
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'code_master_optional_three' AND column_name = 'optionaltwocode') THEN
        UPDATE code_master_optional_three c
        SET optional_two_id = p.id
        FROM code_master_optional_two p
        WHERE c.optionaltwocode = p.code
          AND c.optional_two_id IS NULL;
    END IF;
END $$;

-- ===========================================================================
-- PHASE 4: validate every child row now has a parent_id. Any NULL here means
-- either the old parent-code value was NULL or it did not match any parent
-- row (an orphaned/corrupt reference) — abort the whole migration rather
-- than silently dropping the relationship.
-- ===========================================================================
DO $$
DECLARE
    missing_count int;
BEGIN
    SELECT count(*) INTO missing_count FROM code_master_sub_category WHERE category_id IS NULL;
    IF missing_count > 0 THEN
        RAISE EXCEPTION 'Migration aborted: % row(s) in code_master_sub_category have no matching category', missing_count;
    END IF;

    SELECT count(*) INTO missing_count FROM code_master_sub_sub_category WHERE sub_category_id IS NULL;
    IF missing_count > 0 THEN
        RAISE EXCEPTION 'Migration aborted: % row(s) in code_master_sub_sub_category have no matching sub category', missing_count;
    END IF;

    SELECT count(*) INTO missing_count FROM code_master_brand WHERE sub_sub_category_id IS NULL;
    IF missing_count > 0 THEN
        RAISE EXCEPTION 'Migration aborted: % row(s) in code_master_brand have no matching sub sub category', missing_count;
    END IF;

    SELECT count(*) INTO missing_count FROM code_master_model WHERE brand_id IS NULL;
    IF missing_count > 0 THEN
        RAISE EXCEPTION 'Migration aborted: % row(s) in code_master_model have no matching brand', missing_count;
    END IF;

    SELECT count(*) INTO missing_count FROM code_master_optional_one WHERE model_id IS NULL;
    IF missing_count > 0 THEN
        RAISE EXCEPTION 'Migration aborted: % row(s) in code_master_optional_one have no matching model', missing_count;
    END IF;

    SELECT count(*) INTO missing_count FROM code_master_optional_two WHERE optional_one_id IS NULL;
    IF missing_count > 0 THEN
        RAISE EXCEPTION 'Migration aborted: % row(s) in code_master_optional_two have no matching optional 1', missing_count;
    END IF;

    SELECT count(*) INTO missing_count FROM code_master_optional_three WHERE optional_two_id IS NULL;
    IF missing_count > 0 THEN
        RAISE EXCEPTION 'Migration aborted: % row(s) in code_master_optional_three have no matching optional 2', missing_count;
    END IF;
END $$;

-- ===========================================================================
-- PHASE 5: parent_id is now fully populated and validated -> make it required.
-- ===========================================================================
ALTER TABLE code_master_sub_category       ALTER COLUMN category_id         SET NOT NULL;
ALTER TABLE code_master_sub_sub_category   ALTER COLUMN sub_category_id     SET NOT NULL;
ALTER TABLE code_master_brand              ALTER COLUMN sub_sub_category_id SET NOT NULL;
ALTER TABLE code_master_model              ALTER COLUMN brand_id            SET NOT NULL;
ALTER TABLE code_master_optional_one       ALTER COLUMN model_id            SET NOT NULL;
ALTER TABLE code_master_optional_two       ALTER COLUMN optional_one_id     SET NOT NULL;
ALTER TABLE code_master_optional_three     ALTER COLUMN optional_two_id     SET NOT NULL;

-- ===========================================================================
-- PHASE 6: drop the old code-based foreign keys and code-based primary keys.
-- ===========================================================================
SELECT pg_temp.drop_all_foreign_keys('code_master_sub_category');
SELECT pg_temp.drop_all_foreign_keys('code_master_sub_sub_category');
SELECT pg_temp.drop_all_foreign_keys('code_master_brand');
SELECT pg_temp.drop_all_foreign_keys('code_master_model');
SELECT pg_temp.drop_all_foreign_keys('code_master_optional_one');
SELECT pg_temp.drop_all_foreign_keys('code_master_optional_two');
SELECT pg_temp.drop_all_foreign_keys('code_master_optional_three');

SELECT pg_temp.drop_primary_key('code_master_category');
SELECT pg_temp.drop_primary_key('code_master_sub_category');
SELECT pg_temp.drop_primary_key('code_master_sub_sub_category');
SELECT pg_temp.drop_primary_key('code_master_brand');
SELECT pg_temp.drop_primary_key('code_master_model');
SELECT pg_temp.drop_primary_key('code_master_optional_one');
SELECT pg_temp.drop_primary_key('code_master_optional_two');
SELECT pg_temp.drop_primary_key('code_master_optional_three');

-- ===========================================================================
-- PHASE 7: make "id" the primary key everywhere.
-- ===========================================================================
DO $$
BEGIN
    IF NOT pg_temp.has_primary_key('code_master_category') THEN
        ALTER TABLE code_master_category ADD PRIMARY KEY (id);
    END IF;
    IF NOT pg_temp.has_primary_key('code_master_sub_category') THEN
        ALTER TABLE code_master_sub_category ADD PRIMARY KEY (id);
    END IF;
    IF NOT pg_temp.has_primary_key('code_master_sub_sub_category') THEN
        ALTER TABLE code_master_sub_sub_category ADD PRIMARY KEY (id);
    END IF;
    IF NOT pg_temp.has_primary_key('code_master_brand') THEN
        ALTER TABLE code_master_brand ADD PRIMARY KEY (id);
    END IF;
    IF NOT pg_temp.has_primary_key('code_master_model') THEN
        ALTER TABLE code_master_model ADD PRIMARY KEY (id);
    END IF;
    IF NOT pg_temp.has_primary_key('code_master_optional_one') THEN
        ALTER TABLE code_master_optional_one ADD PRIMARY KEY (id);
    END IF;
    IF NOT pg_temp.has_primary_key('code_master_optional_two') THEN
        ALTER TABLE code_master_optional_two ADD PRIMARY KEY (id);
    END IF;
    IF NOT pg_temp.has_primary_key('code_master_optional_three') THEN
        ALTER TABLE code_master_optional_three ADD PRIMARY KEY (id);
    END IF;
END $$;

-- ===========================================================================
-- PHASE 8: composite uniqueness — global (type, code) for category,
-- (parent_id, code) for every child level. This is what allows, e.g., the
-- same brand code to exist under two different sub-sub-categories.
-- ===========================================================================
ALTER TABLE code_master_category
    ADD CONSTRAINT code_master_category_type_code_key UNIQUE (type, code);

ALTER TABLE code_master_sub_category
    ADD CONSTRAINT code_master_sub_category_category_id_code_key UNIQUE (category_id, code);

ALTER TABLE code_master_sub_sub_category
    ADD CONSTRAINT code_master_sub_sub_category_sub_category_id_code_key UNIQUE (sub_category_id, code);

ALTER TABLE code_master_brand
    ADD CONSTRAINT code_master_brand_sub_sub_category_id_code_key UNIQUE (sub_sub_category_id, code);

ALTER TABLE code_master_model
    ADD CONSTRAINT code_master_model_brand_id_code_key UNIQUE (brand_id, code);

ALTER TABLE code_master_optional_one
    ADD CONSTRAINT code_master_optional_one_model_id_code_key UNIQUE (model_id, code);

ALTER TABLE code_master_optional_two
    ADD CONSTRAINT code_master_optional_two_optional_one_id_code_key UNIQUE (optional_one_id, code);

ALTER TABLE code_master_optional_three
    ADD CONSTRAINT code_master_optional_three_optional_two_id_code_key UNIQUE (optional_two_id, code);

-- ===========================================================================
-- PHASE 9: replace code-based foreign keys with id-based foreign keys.
-- ON DELETE RESTRICT: a parent cannot be deleted while children reference it.
-- ===========================================================================
ALTER TABLE code_master_sub_category
    ADD CONSTRAINT code_master_sub_category_category_id_fkey
    FOREIGN KEY (category_id) REFERENCES code_master_category (id) ON DELETE RESTRICT;

ALTER TABLE code_master_sub_sub_category
    ADD CONSTRAINT code_master_sub_sub_category_sub_category_id_fkey
    FOREIGN KEY (sub_category_id) REFERENCES code_master_sub_category (id) ON DELETE RESTRICT;

ALTER TABLE code_master_brand
    ADD CONSTRAINT code_master_brand_sub_sub_category_id_fkey
    FOREIGN KEY (sub_sub_category_id) REFERENCES code_master_sub_sub_category (id) ON DELETE RESTRICT;

ALTER TABLE code_master_model
    ADD CONSTRAINT code_master_model_brand_id_fkey
    FOREIGN KEY (brand_id) REFERENCES code_master_brand (id) ON DELETE RESTRICT;

ALTER TABLE code_master_optional_one
    ADD CONSTRAINT code_master_optional_one_model_id_fkey
    FOREIGN KEY (model_id) REFERENCES code_master_model (id) ON DELETE RESTRICT;

ALTER TABLE code_master_optional_two
    ADD CONSTRAINT code_master_optional_two_optional_one_id_fkey
    FOREIGN KEY (optional_one_id) REFERENCES code_master_optional_one (id) ON DELETE RESTRICT;

ALTER TABLE code_master_optional_three
    ADD CONSTRAINT code_master_optional_three_optional_two_id_fkey
    FOREIGN KEY (optional_two_id) REFERENCES code_master_optional_two (id) ON DELETE RESTRICT;

-- ===========================================================================
-- PHASE 10: indexes on every foreign-key column (dependent-dropdown filtering).
-- ===========================================================================
CREATE INDEX IF NOT EXISTS idx_code_master_sub_category_category_id         ON code_master_sub_category (category_id);
CREATE INDEX IF NOT EXISTS idx_code_master_sub_sub_category_sub_category_id ON code_master_sub_sub_category (sub_category_id);
CREATE INDEX IF NOT EXISTS idx_code_master_brand_sub_sub_category_id        ON code_master_brand (sub_sub_category_id);
CREATE INDEX IF NOT EXISTS idx_code_master_model_brand_id                   ON code_master_model (brand_id);
CREATE INDEX IF NOT EXISTS idx_code_master_optional_one_model_id            ON code_master_optional_one (model_id);
CREATE INDEX IF NOT EXISTS idx_code_master_optional_two_optional_one_id     ON code_master_optional_two (optional_one_id);
CREATE INDEX IF NOT EXISTS idx_code_master_optional_three_optional_two_id   ON code_master_optional_three (optional_two_id);

-- ===========================================================================
-- PHASE 11: only now that everything above has succeeded, drop the old
-- code-based parent columns. If any earlier phase failed, execution never
-- reaches here and the whole transaction rolls back, leaving the original
-- columns untouched.
-- ===========================================================================
ALTER TABLE code_master_sub_category       DROP COLUMN IF EXISTS categorycode;
ALTER TABLE code_master_sub_sub_category   DROP COLUMN IF EXISTS subcategorycode;
ALTER TABLE code_master_brand              DROP COLUMN IF EXISTS subsubcategorycode;
ALTER TABLE code_master_model              DROP COLUMN IF EXISTS brandcode;
ALTER TABLE code_master_optional_one       DROP COLUMN IF EXISTS modelcode;
ALTER TABLE code_master_optional_two       DROP COLUMN IF EXISTS optionalonecode;
ALTER TABLE code_master_optional_three     DROP COLUMN IF EXISTS optionaltwocode;

-- ===========================================================================
-- PHASE 12: everything above has succeeded — the tables are now on id-based
-- keys. Rename tables, then their constraints and indexes, from the old
-- "code_master_*" naming to the current "item_*" naming.
-- ===========================================================================
ALTER TABLE IF EXISTS code_master_category         RENAME TO item_category;
ALTER TABLE IF EXISTS code_master_sub_category      RENAME TO item_sub_category;
ALTER TABLE IF EXISTS code_master_sub_sub_category  RENAME TO item_sub_sub_category;
ALTER TABLE IF EXISTS code_master_brand             RENAME TO item_brand;
ALTER TABLE IF EXISTS code_master_model             RENAME TO item_model;
ALTER TABLE IF EXISTS code_master_optional_one      RENAME TO item_optional_one;
ALTER TABLE IF EXISTS code_master_optional_two      RENAME TO item_optional_two;
ALTER TABLE IF EXISTS code_master_optional_three    RENAME TO item_optional_three;

ALTER TABLE item_category
    RENAME CONSTRAINT code_master_category_type_code_key TO item_category_type_code_key;

ALTER TABLE item_sub_category
    RENAME CONSTRAINT code_master_sub_category_category_id_code_key TO item_sub_category_category_id_code_key;
ALTER TABLE item_sub_category
    RENAME CONSTRAINT code_master_sub_category_category_id_fkey TO item_sub_category_category_id_fkey;

ALTER TABLE item_sub_sub_category
    RENAME CONSTRAINT code_master_sub_sub_category_sub_category_id_code_key TO item_sub_sub_category_sub_category_id_code_key;
ALTER TABLE item_sub_sub_category
    RENAME CONSTRAINT code_master_sub_sub_category_sub_category_id_fkey TO item_sub_sub_category_sub_category_id_fkey;

ALTER TABLE item_brand
    RENAME CONSTRAINT code_master_brand_sub_sub_category_id_code_key TO item_brand_sub_sub_category_id_code_key;
ALTER TABLE item_brand
    RENAME CONSTRAINT code_master_brand_sub_sub_category_id_fkey TO item_brand_sub_sub_category_id_fkey;

ALTER TABLE item_model
    RENAME CONSTRAINT code_master_model_brand_id_code_key TO item_model_brand_id_code_key;
ALTER TABLE item_model
    RENAME CONSTRAINT code_master_model_brand_id_fkey TO item_model_brand_id_fkey;

ALTER TABLE item_optional_one
    RENAME CONSTRAINT code_master_optional_one_model_id_code_key TO item_optional_one_model_id_code_key;
ALTER TABLE item_optional_one
    RENAME CONSTRAINT code_master_optional_one_model_id_fkey TO item_optional_one_model_id_fkey;

ALTER TABLE item_optional_two
    RENAME CONSTRAINT code_master_optional_two_optional_one_id_code_key TO item_optional_two_optional_one_id_code_key;
ALTER TABLE item_optional_two
    RENAME CONSTRAINT code_master_optional_two_optional_one_id_fkey TO item_optional_two_optional_one_id_fkey;

ALTER TABLE item_optional_three
    RENAME CONSTRAINT code_master_optional_three_optional_two_id_code_key TO item_optional_three_optional_two_id_code_key;
ALTER TABLE item_optional_three
    RENAME CONSTRAINT code_master_optional_three_optional_two_id_fkey TO item_optional_three_optional_two_id_fkey;

ALTER INDEX IF EXISTS idx_code_master_sub_category_category_id         RENAME TO idx_item_sub_category_category_id;
ALTER INDEX IF EXISTS idx_code_master_sub_sub_category_sub_category_id RENAME TO idx_item_sub_sub_category_sub_category_id;
ALTER INDEX IF EXISTS idx_code_master_brand_sub_sub_category_id        RENAME TO idx_item_brand_sub_sub_category_id;
ALTER INDEX IF EXISTS idx_code_master_model_brand_id                   RENAME TO idx_item_model_brand_id;
ALTER INDEX IF EXISTS idx_code_master_optional_one_model_id            RENAME TO idx_item_optional_one_model_id;
ALTER INDEX IF EXISTS idx_code_master_optional_two_optional_one_id     RENAME TO idx_item_optional_two_optional_one_id;
ALTER INDEX IF EXISTS idx_code_master_optional_three_optional_two_id   RENAME TO idx_item_optional_three_optional_two_id;

-- ---------------------------------------------------------------------------
-- Cleanup session-local helper functions.
-- ---------------------------------------------------------------------------
DROP FUNCTION IF EXISTS pg_temp.drop_all_foreign_keys(text);
DROP FUNCTION IF EXISTS pg_temp.drop_primary_key(text);
DROP FUNCTION IF EXISTS pg_temp.has_primary_key(text);

COMMIT;
