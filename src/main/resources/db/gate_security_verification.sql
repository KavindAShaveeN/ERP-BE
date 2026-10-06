-- Adds a security gate-verification stage to GIN (exit), GRN (arrival) and stock returns
-- (exit/arrival), following the same actor/date/flag pattern as approved_by/is_approved and
-- GRN's checked_by/is_approved: a security guard records their name at the gate independently
-- of the store's own check/approve steps.
-- Plain JDBC-backed tables, not managed by Hibernate ddl-auto:update. Run this script
-- manually against the application database.

ALTER TABLE gin
    ADD COLUMN IF NOT EXISTS gate_verified_by VARCHAR(255),
    ADD COLUMN IF NOT EXISTS gate_verified_date TIMESTAMP,
    ADD COLUMN IF NOT EXISTS is_gate_verified BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE grn
    ADD COLUMN IF NOT EXISTS gate_verified_by VARCHAR(255),
    ADD COLUMN IF NOT EXISTS gate_verified_date TIMESTAMP,
    ADD COLUMN IF NOT EXISTS is_gate_verified BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE stock_return
    ADD COLUMN IF NOT EXISTS gate_verified_by VARCHAR(255),
    ADD COLUMN IF NOT EXISTS gate_verified_date TIMESTAMP,
    ADD COLUMN IF NOT EXISTS is_gate_verified BOOLEAN NOT NULL DEFAULT FALSE;
