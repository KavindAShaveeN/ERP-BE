-- Plant (production) module: raw-material consumption -> finished-goods creation,
-- sitting between GRN (goods received into a project's store) and GIN (finished
-- goods issued out of one). Plain JDBC-backed tables (matching the GRN/GIN/
-- stock_batch pattern in this codebase), so they are NOT created automatically
-- by Hibernate's ddl-auto:update. Run this script manually against the
-- application database before using the /api/plant-recipe/**, /api/plant-
-- production/** and /api/plant-report/** endpoints.
--
-- There is no separate "plant" master record: a plant is simply the project
-- the signed-in user is currently working in (see useActiveProject on the
-- frontend) — its stock lives in project_store under that same project_code,
-- exactly like GRN/GIN already use. Recipes and production records are
-- therefore scoped directly by project_code, not by a plant_id foreign key.
--
-- plant_recipe_output.is_primary marks the one output a recipe's quantities are
-- scaled against when a user enters a produced quantity for that item; every
-- other input/output line scales by the same factor (entered qty / primary
-- recipe qty). Recipes never model wastage — wastage is only ever recorded on
-- an actual production_output row (is_waste = TRUE), never planned for.

CREATE TABLE IF NOT EXISTS plant_recipe (
    recipe_id       UUID          PRIMARY KEY,
    recipe_name     VARCHAR(120)  NOT NULL,
    project_code    VARCHAR(30)   NOT NULL,
    product_type    VARCHAR(60),
    effective_date  DATE          NOT NULL,
    is_active       BOOLEAN       NOT NULL DEFAULT TRUE,
    created_by      VARCHAR(60),
    created_date    TIMESTAMP     NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_plant_recipe_project ON plant_recipe (project_code);

CREATE TABLE IF NOT EXISTS plant_recipe_output (
    recipe_output_id  UUID           PRIMARY KEY,
    recipe_id         UUID           NOT NULL REFERENCES plant_recipe (recipe_id) ON DELETE CASCADE,
    item_code         VARCHAR(50)    NOT NULL REFERENCES item_code (code),
    quantity          NUMERIC(14,3)  NOT NULL,
    uom_id            INTEGER        NOT NULL,
    is_primary        BOOLEAN        NOT NULL DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS idx_plant_recipe_output_recipe ON plant_recipe_output (recipe_id);

CREATE TABLE IF NOT EXISTS plant_recipe_input (
    recipe_input_id  UUID           PRIMARY KEY,
    recipe_id        UUID           NOT NULL REFERENCES plant_recipe (recipe_id) ON DELETE CASCADE,
    item_code        VARCHAR(50)    NOT NULL REFERENCES item_code (code),
    quantity         NUMERIC(14,3)  NOT NULL,
    uom_id           INTEGER        NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_plant_recipe_input_recipe ON plant_recipe_input (recipe_id);

CREATE TABLE IF NOT EXISTS plant_production (
    production_id      UUID          PRIMARY KEY,
    production_code     VARCHAR(40)   NOT NULL,
    project_code         VARCHAR(30)   NOT NULL,
    production_date     DATE          NOT NULL,
    recipe_id           UUID          REFERENCES plant_recipe (recipe_id) ON DELETE SET NULL,
    status               VARCHAR(20)   NOT NULL DEFAULT 'DRAFT',
    remarks              VARCHAR(500),
    submitted_by         VARCHAR(60),
    submitted_date       TIMESTAMP,
    approved_by          VARCHAR(60),
    approved_date        TIMESTAMP,
    rejected_by          VARCHAR(60),
    rejected_date        TIMESTAMP,
    rejection_reason     VARCHAR(500),
    reversed_by          VARCHAR(60),
    reversed_date        TIMESTAMP,
    reversal_reason      VARCHAR(500),
    created_by           VARCHAR(60),
    created_date         TIMESTAMP     NOT NULL DEFAULT now(),
    CONSTRAINT plant_production_production_code_key UNIQUE (production_code),
    CONSTRAINT plant_production_status_check CHECK (
        status IN ('DRAFT', 'SUBMITTED', 'APPROVED', 'REJECTED', 'REVERSED')
    )
);
CREATE INDEX IF NOT EXISTS idx_plant_production_project ON plant_production (project_code, production_date);
CREATE INDEX IF NOT EXISTS idx_plant_production_status ON plant_production (status);

CREATE TABLE IF NOT EXISTS plant_production_input (
    production_input_id  UUID           PRIMARY KEY,
    production_id         UUID           NOT NULL REFERENCES plant_production (production_id) ON DELETE CASCADE,
    item_code              VARCHAR(50)    NOT NULL REFERENCES item_code (code),
    uom_id                  INTEGER        NOT NULL,
    planned_quantity        NUMERIC(14,3),
    consumed_quantity       NUMERIC(14,3)  NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_plant_production_input_production ON plant_production_input (production_id);

CREATE TABLE IF NOT EXISTS plant_production_output (
    production_output_id  UUID           PRIMARY KEY,
    production_id           UUID           NOT NULL REFERENCES plant_production (production_id) ON DELETE CASCADE,
    item_code                VARCHAR(50)    NOT NULL REFERENCES item_code (code),
    uom_id                    INTEGER        NOT NULL,
    planned_quantity          NUMERIC(14,3),
    produced_quantity         NUMERIC(14,3)  NOT NULL,
    is_waste                  BOOLEAN        NOT NULL DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS idx_plant_production_output_production ON plant_production_output (production_id);
