-- Salary advances and salary payments paid out of employee (petty) cash accounts. Run once against the database.
-- Every advance / salary payment is backed by a POSTED EXPENSE row in cash_transaction, so the cash account balance,
-- the cash statement and these records always agree.

-- Expense categories used by the posted cash transactions.
INSERT INTO cash_expense_category (category_code, category_name, is_active)
VALUES ('SALARY_ADVANCE', 'Salary advance', true),
       ('SALARY', 'Salary payment', true)
ON CONFLICT (category_code) DO NOTHING;

CREATE TABLE salary_advance (
	advance_id uuid DEFAULT gen_random_uuid() NOT NULL,
	advance_code varchar(40) NOT NULL,
	employee_code varchar(50) NOT NULL,
	cash_account_id uuid NOT NULL,
	cash_transaction_id uuid NOT NULL,
	amount numeric(15, 2) NOT NULL,
	recovered_amount numeric(15, 2) DEFAULT 0.00 NOT NULL,
	advance_date date NOT NULL,
	status varchar(20) DEFAULT 'OPEN' NOT NULL,
	remarks text NULL,
	created_by varchar(50) NOT NULL,
	created_at timestamp DEFAULT now() NOT NULL,
	CONSTRAINT salary_advance_pkey PRIMARY KEY (advance_id),
	CONSTRAINT salary_advance_code_key UNIQUE (advance_code),
	CONSTRAINT salary_advance_amount_check CHECK (amount > 0),
	CONSTRAINT salary_advance_recovered_check CHECK (recovered_amount >= 0 AND recovered_amount <= amount),
	CONSTRAINT salary_advance_status_check CHECK (status IN ('OPEN', 'PARTIAL', 'CLOSED', 'VOID')),
	CONSTRAINT salary_advance_employee_fkey FOREIGN KEY (employee_code) REFERENCES employee(employee_code),
	CONSTRAINT salary_advance_account_fkey FOREIGN KEY (cash_account_id) REFERENCES cash_account(cash_account_id),
	CONSTRAINT salary_advance_transaction_fkey FOREIGN KEY (cash_transaction_id) REFERENCES cash_transaction(transaction_id),
	CONSTRAINT salary_advance_created_by_fkey FOREIGN KEY (created_by) REFERENCES employee(employee_code)
);
CREATE INDEX idx_salary_advance_employee ON salary_advance USING btree (employee_code);
CREATE INDEX idx_salary_advance_account ON salary_advance USING btree (cash_account_id);

CREATE TABLE salary_payment (
	salary_payment_id uuid DEFAULT gen_random_uuid() NOT NULL,
	payment_code varchar(40) NOT NULL,
	employee_code varchar(50) NOT NULL,
	salary_period varchar(7) NOT NULL, -- yyyy-MM
	gross_amount numeric(15, 2) NOT NULL,
	advance_deducted numeric(15, 2) DEFAULT 0.00 NOT NULL,
	net_amount numeric(15, 2) NOT NULL,
	cash_account_id uuid NULL, -- null only when the whole salary was absorbed by advance recovery (net 0)
	cash_transaction_id uuid NULL,
	payment_date date NOT NULL,
	status varchar(20) DEFAULT 'PAID' NOT NULL,
	remarks text NULL,
	created_by varchar(50) NOT NULL,
	created_at timestamp DEFAULT now() NOT NULL,
	CONSTRAINT salary_payment_pkey PRIMARY KEY (salary_payment_id),
	CONSTRAINT salary_payment_code_key UNIQUE (payment_code),
	CONSTRAINT salary_payment_amounts_check CHECK (gross_amount > 0 AND advance_deducted >= 0 AND net_amount >= 0
		AND net_amount = gross_amount - advance_deducted),
	CONSTRAINT salary_payment_period_check CHECK (salary_period ~ '^[0-9]{4}-(0[1-9]|1[0-2])$'),
	CONSTRAINT salary_payment_status_check CHECK (status IN ('PAID', 'VOID')),
	CONSTRAINT salary_payment_employee_fkey FOREIGN KEY (employee_code) REFERENCES employee(employee_code),
	CONSTRAINT salary_payment_account_fkey FOREIGN KEY (cash_account_id) REFERENCES cash_account(cash_account_id),
	CONSTRAINT salary_payment_transaction_fkey FOREIGN KEY (cash_transaction_id) REFERENCES cash_transaction(transaction_id),
	CONSTRAINT salary_payment_created_by_fkey FOREIGN KEY (created_by) REFERENCES employee(employee_code)
);
CREATE INDEX idx_salary_payment_employee ON salary_payment USING btree (employee_code);
-- One live salary payment per employee per month (void it first to pay again).
CREATE UNIQUE INDEX uq_salary_payment_period ON salary_payment USING btree (employee_code, salary_period) WHERE (status = 'PAID');

-- Which advances a salary payment recovered, and how much from each.
CREATE TABLE salary_advance_recovery (
	recovery_id uuid DEFAULT gen_random_uuid() NOT NULL,
	salary_payment_id uuid NOT NULL,
	advance_id uuid NOT NULL,
	amount numeric(15, 2) NOT NULL,
	CONSTRAINT salary_advance_recovery_pkey PRIMARY KEY (recovery_id),
	CONSTRAINT salary_advance_recovery_amount_check CHECK (amount > 0),
	CONSTRAINT salary_advance_recovery_payment_fkey FOREIGN KEY (salary_payment_id) REFERENCES salary_payment(salary_payment_id),
	CONSTRAINT salary_advance_recovery_advance_fkey FOREIGN KEY (advance_id) REFERENCES salary_advance(advance_id)
);
CREATE INDEX idx_salary_advance_recovery_payment ON salary_advance_recovery USING btree (salary_payment_id);
CREATE INDEX idx_salary_advance_recovery_advance ON salary_advance_recovery USING btree (advance_id);
