-- Adds a separate "arrival at destination gate" verification stage to GIN and stock_return,
-- alongside the existing exit gate_verified_by/date/is_gate_verified columns added by
-- gate_security_verification.sql.
--
-- Why a second stage: for an outgoing GIN, security at the ISSUING project confirms the goods
-- actually left through gate_verified_*. For an incoming shipment, security at the RECEIVING
-- project needs to confirm arrival too — but that happens before any GRN exists for it (the
-- store keeper only raises the GRN afterwards). So the arrival confirmation is recorded
-- against the GIN/stock_return itself; once a GRN is created for it, GRNService copies this
-- confirmation onto the GRN's own gate_verified_* columns so the GRN just displays it done.
--
-- Plain JDBC-backed tables, not managed by Hibernate ddl-auto:update. Run this script
-- manually against the application database.

ALTER TABLE gin
    ADD COLUMN IF NOT EXISTS arrival_gate_verified_by VARCHAR(255),
    ADD COLUMN IF NOT EXISTS arrival_gate_verified_date TIMESTAMP,
    ADD COLUMN IF NOT EXISTS is_arrival_gate_verified BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE stock_return
    ADD COLUMN IF NOT EXISTS arrival_gate_verified_by VARCHAR(255),
    ADD COLUMN IF NOT EXISTS arrival_gate_verified_date TIMESTAMP,
    ADD COLUMN IF NOT EXISTS is_arrival_gate_verified BOOLEAN NOT NULL DEFAULT FALSE;
