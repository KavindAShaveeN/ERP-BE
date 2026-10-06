-- Operator History for an asset: tracks which employee operates the asset over time, who
-- recorded the change and when, plus a remark. The operator is always an employee (name and
-- contact number are read from the employee record, not stored separately here). Shown as its
-- own section on the asset detail page. The latest active entry for an asset is also used to
-- auto-fill the operator on the Fuel Issue ("Received by") and Service Request forms for that
-- asset (same idea as asset_location driving "where is this asset" elsewhere).
--
-- PREREQUISITE: the "asset" and "employee" tables.
-- Run manually against the application database (this codebase has no Hibernate ddl-auto / Flyway).
-- Safe to re-run: guarded with IF NOT EXISTS.

CREATE TABLE IF NOT EXISTS asset_operator_history (
    asset_operator_history_id UUID         PRIMARY KEY,
    asset_code                VARCHAR(20)  NOT NULL REFERENCES asset (asset_code) ON DELETE CASCADE,
    operator_employee_code    VARCHAR(50)  NOT NULL REFERENCES employee (employee_code),
    changed_by                VARCHAR(50)  NOT NULL REFERENCES employee (employee_code),
    changed_date              TIMESTAMP    NOT NULL DEFAULT now(),
    remarks                   VARCHAR(500),
    is_active                 BOOL         NOT NULL DEFAULT TRUE,
    created_at                TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at                TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_asset_operator_history_asset_code
    ON asset_operator_history (asset_code)
    WHERE is_active = TRUE;
