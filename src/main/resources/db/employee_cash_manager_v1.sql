-- Cash managers see every cash account, statement and report; everyone else sees only their own petty cash.
-- Run once, then flag the people who manage cash, e.g.:
--   UPDATE employee SET is_cash_manager = true WHERE employee_code IN ('EMP001', 'EMP002');
ALTER TABLE employee ADD COLUMN IF NOT EXISTS is_cash_manager boolean DEFAULT false NOT NULL;
