-- Quotation module: call for quotations (quotation_request, header + line items,
-- optionally sourced from an MR) and the per-supplier offers received back (quotation,
-- header + line items). mr_id on quotation_request is nullable and has no FK, mirroring
-- the same optional-MR pattern already used on po.mr_id.

CREATE TABLE IF NOT EXISTS quotation_request (
    quotation_request_id   UUID PRIMARY KEY,
    quotation_request_code VARCHAR(30) NOT NULL UNIQUE,
    mr_id                   UUID NULL,
    request_date            DATE NOT NULL,
    requested_by             VARCHAR(255) NOT NULL,
    due_date                 DATE NULL,
    remark                   VARCHAR(500) NULL,
    status                   VARCHAR(30) NOT NULL,
    created_at               TIMESTAMP NOT NULL DEFAULT now(),
    updated_at               TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS quotation_request_item (
    quotation_request_item_id UUID PRIMARY KEY,
    quotation_request_id       UUID NOT NULL REFERENCES quotation_request(quotation_request_id) ON DELETE CASCADE,
    mr_item_id                  UUID NULL REFERENCES mr_item(mr_item_id),
    item_code_code               VARCHAR(255) NOT NULL REFERENCES item_code(item_code_code),
    description                  VARCHAR(250) NULL,
    uom_id                        INTEGER NULL REFERENCES uom(uom_id),
    quantity                     INTEGER NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_quotation_request_item_request
    ON quotation_request_item (quotation_request_id);

CREATE TABLE IF NOT EXISTS quotation_request_supplier (
    quotation_request_id UUID NOT NULL REFERENCES quotation_request(quotation_request_id) ON DELETE CASCADE,
    supplier_code          VARCHAR(255) NOT NULL REFERENCES supplier(suppliercode),
    PRIMARY KEY (quotation_request_id, supplier_code)
);

CREATE TABLE IF NOT EXISTS quotation (
    quotation_id           UUID PRIMARY KEY,
    quotation_code          VARCHAR(60) NOT NULL UNIQUE,
    quotation_request_id     UUID NOT NULL REFERENCES quotation_request(quotation_request_id) ON DELETE CASCADE,
    supplier_code             VARCHAR(255) NOT NULL REFERENCES supplier(suppliercode),
    quotation_date             DATE NOT NULL,
    valid_until                 DATE NULL,
    currency_id                 INTEGER NULL REFERENCES currency(currency_id),
    payment_term                 VARCHAR(120) NULL,
    delivery_term                 VARCHAR(120) NULL,
    remark                        VARCHAR(500) NULL,
    total_value                   NUMERIC(18, 2) NULL,
    status                         VARCHAR(30) NOT NULL,
    received_date                  DATE NULL,
    created_at                      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at                      TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (quotation_request_id, supplier_code)
);

-- quotation_request_item_id cascades too (not just quotation_id) — Postgres checks
-- NO ACTION constraints immediately as each cascade branch runs, so without this a
-- quotation_request delete could hit the quotation_request_item cascade before the
-- quotation -> quotation_item cascade finishes, and fail with a FK violation.
CREATE TABLE IF NOT EXISTS quotation_item (
    quotation_item_id           UUID PRIMARY KEY,
    quotation_id                  UUID NOT NULL REFERENCES quotation(quotation_id) ON DELETE CASCADE,
    quotation_request_item_id      UUID NOT NULL REFERENCES quotation_request_item(quotation_request_item_id) ON DELETE CASCADE,
    quantity                        INTEGER NOT NULL,
    unit_price                       NUMERIC(18, 2) NOT NULL,
    amount                            NUMERIC(18, 2) NOT NULL,
    remark                             VARCHAR(500) NULL,
    is_selected                         BOOLEAN NOT NULL DEFAULT false
);

CREATE INDEX IF NOT EXISTS idx_quotation_item_quotation
    ON quotation_item (quotation_id);
