-- Daily project allocation: which project(s) each employee works on each day, with optional hours.
-- An employee may work on several projects in one day. Run once against the database.

CREATE TABLE employee_project_allocation (
	allocation_id uuid NOT NULL,
	employee_code varchar(50) NOT NULL,
	project_id int4 NOT NULL,
	work_date date NOT NULL,
	hours numeric(4, 2) NULL,
	remarks varchar(500) NULL,
	assigned_by varchar(50) NULL,
	created_at timestamp DEFAULT now() NOT NULL,
	updated_at timestamp DEFAULT now() NOT NULL,
	CONSTRAINT employee_project_allocation_pkey PRIMARY KEY (allocation_id),
	CONSTRAINT employee_project_allocation_hours_check CHECK (hours IS NULL OR (hours > 0 AND hours <= 24)),
	CONSTRAINT fk_epa_employee FOREIGN KEY (employee_code) REFERENCES employee(employee_code),
	CONSTRAINT fk_epa_project FOREIGN KEY (project_id) REFERENCES project(projectid),
	CONSTRAINT uq_epa_day UNIQUE (employee_code, work_date, project_id)
);
CREATE INDEX idx_epa_date ON employee_project_allocation USING btree (work_date);
CREATE INDEX idx_epa_project_date ON employee_project_allocation USING btree (project_id, work_date);
