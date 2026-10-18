-- Daily labour attendance: one row per (employee, project, day) stint. An employee can have several
-- rows on the same day (worked at multiple projects). Run once against the database.

CREATE TABLE labour_entry (
	labour_entry_id uuid NOT NULL,
	employee_code varchar(50) NOT NULL,
	project_code varchar(50) NOT NULL,
	work_date date NOT NULL,
	start_time time NULL,
	end_time time NULL,
	hours numeric(5, 2) NOT NULL,
	work_description varchar(500) NULL,
	recorded_by varchar(50) NULL,
	is_active bool DEFAULT true NOT NULL,
	created_at timestamp DEFAULT now() NOT NULL,
	updated_at timestamp DEFAULT now() NOT NULL,
	CONSTRAINT labour_entry_pkey PRIMARY KEY (labour_entry_id),
	CONSTRAINT labour_entry_hours_check CHECK (hours > 0 AND hours <= 24),
	CONSTRAINT labour_entry_employee_code_fkey FOREIGN KEY (employee_code) REFERENCES employee(employee_code),
	CONSTRAINT labour_entry_project_code_fkey FOREIGN KEY (project_code) REFERENCES project(project_code)
);
CREATE INDEX idx_labour_entry_work_date ON labour_entry USING btree (work_date) WHERE (is_active = true);
CREATE INDEX idx_labour_entry_employee ON labour_entry USING btree (employee_code, work_date) WHERE (is_active = true);
CREATE INDEX idx_labour_entry_project ON labour_entry USING btree (project_code, work_date) WHERE (is_active = true);
