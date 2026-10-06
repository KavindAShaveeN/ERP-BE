-- Other (non-material) expenses on a plant production record — e.g. labour, power,
-- transport, machine hire — each with a free-text description and an amount. They are
-- added to the raw-material cost when the production is approved, so they raise the
-- unit cost of the finished-goods batch(es) it opens (PlantProductionService#applyStockMovements).
-- Plain JDBC-backed table: run manually, not created by Hibernate's ddl-auto:update.

CREATE TABLE IF NOT EXISTS plant_production_expense (
    production_expense_id  UUID           PRIMARY KEY,
    production_id           UUID           NOT NULL REFERENCES plant_production (production_id) ON DELETE CASCADE,
    description              VARCHAR(200)   NOT NULL,
    amount                   NUMERIC(14,2)  NOT NULL CHECK (amount > 0)
);
CREATE INDEX IF NOT EXISTS idx_plant_production_expense_production ON plant_production_expense (production_id);
