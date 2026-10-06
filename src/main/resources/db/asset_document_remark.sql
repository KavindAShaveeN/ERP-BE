-- One free-text "document remark" per asset, shown in the Documents section of the asset detail
-- page (long, unrestricted text). Kept in its own table so it does not change the shared "asset"
-- table that every asset query reads.
--
-- PREREQUISITE: the "asset" table (see asset_vehicle_schema.sql).
--
-- Safe to re-run: guarded with IF NOT EXISTS.

CREATE TABLE IF NOT EXISTS asset_document_remark (
    asset_code   VARCHAR(20)  PRIMARY KEY REFERENCES asset (asset_code) ON DELETE CASCADE,
    remark       TEXT         NOT NULL DEFAULT '',
    updated_at   TIMESTAMP    NOT NULL DEFAULT now()
);
