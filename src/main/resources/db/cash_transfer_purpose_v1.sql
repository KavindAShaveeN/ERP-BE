-- Purpose of a cash payment: INTERNAL (transfer between accounts) or PURCHASING / SALARY / SUBCONTRACTOR / ... paid to
-- someone outside the accounts (recorded as an expense, no to account). A SALARY payment also records the
-- employee and whether it is a salary ADVANCE or a FULL_SALARY.
-- Safe to run more than once, and safe on a database where an earlier version of this script already ran.
ALTER TABLE cash_transaction
    ADD COLUMN IF NOT EXISTS purpose varchar(20) NULL,
    ADD COLUMN IF NOT EXISTS employee_code varchar(50) NULL,
    ADD COLUMN IF NOT EXISTS salary_kind varchar(20) NULL;

ALTER TABLE cash_transaction
    DROP CONSTRAINT IF EXISTS cash_transaction_purpose_check,
    DROP CONSTRAINT IF EXISTS cash_transaction_salary_kind_check,
    DROP CONSTRAINT IF EXISTS cash_transaction_salary_fields_check,
    DROP CONSTRAINT IF EXISTS cash_transaction_employee_fkey;

ALTER TABLE cash_transaction
    ADD CONSTRAINT cash_transaction_purpose_check CHECK (purpose IS NULL OR purpose IN
        ('INTERNAL', 'PURCHASING', 'SALARY', 'SUBCONTRACTOR', 'TRANSPORT', 'UTILITIES', 'OTHER')),
    ADD CONSTRAINT cash_transaction_salary_kind_check CHECK (salary_kind IS NULL OR salary_kind IN ('ADVANCE', 'FULL_SALARY')),
    ADD CONSTRAINT cash_transaction_salary_fields_check CHECK (
        (purpose = 'SALARY' AND employee_code IS NOT NULL AND salary_kind IS NOT NULL)
        OR (purpose IS DISTINCT FROM 'SALARY' AND employee_code IS NULL AND salary_kind IS NULL)),
    ADD CONSTRAINT cash_transaction_employee_fkey FOREIGN KEY (employee_code) REFERENCES employee(employee_code);

CREATE INDEX IF NOT EXISTS idx_cash_transaction_purpose ON cash_transaction USING btree (purpose);
CREATE INDEX IF NOT EXISTS idx_cash_transaction_employee ON cash_transaction USING btree (employee_code) WHERE (employee_code IS NOT NULL);

-- One expense category per external purpose (a payment with no to account is filed under the category of its purpose).
INSERT INTO cash_expense_category (category_code, category_name, is_active)
VALUES ('PURCHASING', 'Purchasing', true),
       ('SALARY', 'Salary payment', true),
       ('SUBCONTRACTOR', 'Subcontractor', true),
       ('TRANSPORT', 'Transport', true),
       ('UTILITIES', 'Utilities', true),
       ('OTHER', 'Other', true)
ON CONFLICT (category_code) DO NOTHING;
