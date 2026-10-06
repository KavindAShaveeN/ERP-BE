-- Separable "pack" items of an asset (laptop -> charger, machine -> battery pack).
-- A pack item travels with its parent asset on GIN / GRN / stock return documents; the user
-- ticks or unticks it per transaction, and is_with_asset tracks where it currently is.
--
-- PREREQUISITE: the "asset" table (see asset_vehicle_schema.sql).
-- Safe to re-run: every statement is guarded (IF NOT EXISTS).

CREATE TABLE IF NOT EXISTS asset_pack_item (
    pack_item_id   BIGSERIAL     PRIMARY KEY,
    asset_code     VARCHAR(20)   NOT NULL REFERENCES asset (asset_code) ON DELETE CASCADE,
    name           VARCHAR(255)  NOT NULL,
    description    VARCHAR(500),
    serial_number  VARCHAR(100),
    quantity       INTEGER       NOT NULL DEFAULT 1,
    is_with_asset  BOOLEAN       NOT NULL DEFAULT TRUE,
    remarks        VARCHAR(500),
    is_active      BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at     TIMESTAMP     NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_asset_pack_item_asset ON asset_pack_item (asset_code);

-- Which pack items were ticked (included = TRUE) or unticked on a document line.
-- doc_type: GIN | GRN | STOCK_RETURN. Keyed by document + asset (not line id), because a
-- document's lines are re-created on every edit.
CREATE TABLE IF NOT EXISTS asset_pack_transaction (
    pack_transaction_id  BIGSERIAL    PRIMARY KEY,
    doc_type             VARCHAR(30)  NOT NULL,
    doc_id               UUID         NOT NULL,
    asset_code           VARCHAR(20)  NOT NULL,
    pack_item_id         BIGINT       NOT NULL REFERENCES asset_pack_item (pack_item_id) ON DELETE CASCADE,
    included             BOOLEAN      NOT NULL,
    created_at           TIMESTAMP    NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_asset_pack_txn_doc  ON asset_pack_transaction (doc_type, doc_id);
CREATE INDEX IF NOT EXISTS idx_asset_pack_txn_item ON asset_pack_transaction (pack_item_id);
