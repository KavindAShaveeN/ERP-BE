-- One shared salary account. Salary payments and advances for every employee are paid out of it
-- (the employee is recorded on each advance / payment). Top it up with a transfer from main cash.
-- Run once; safe to re-run.
INSERT INTO cash_account (account_name, account_type, account_balance, is_active)
SELECT 'Salary Account', 'MAIN_CASH', 0.00, true
WHERE NOT EXISTS (SELECT 1 FROM cash_account WHERE account_name = 'Salary Account');
