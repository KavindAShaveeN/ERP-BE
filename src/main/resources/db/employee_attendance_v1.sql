-- Manual attendance register: one row per employee per day. Run once against the database.

CREATE TABLE employee_attendance (
	attendance_id uuid NOT NULL,
	employee_code varchar(50) NOT NULL,
	attendance_date date NOT NULL,
	status varchar(20) NOT NULL,
	check_in_time time NULL,
	check_out_time time NULL,
	remarks varchar(500) NULL,
	recorded_by varchar(50) NULL,
	is_active bool DEFAULT true NOT NULL,
	created_at timestamp DEFAULT now() NOT NULL,
	updated_at timestamp DEFAULT now() NOT NULL,
	CONSTRAINT employee_attendance_pkey PRIMARY KEY (attendance_id),
	CONSTRAINT employee_attendance_status_check CHECK (status IN ('PRESENT', 'ABSENT', 'HALF_DAY', 'ON_LEAVE')),
	CONSTRAINT employee_attendance_employee_code_fkey FOREIGN KEY (employee_code) REFERENCES employee(employee_code)
);
-- One live record per employee per day.
CREATE UNIQUE INDEX uq_employee_attendance_day ON employee_attendance USING btree (employee_code, attendance_date) WHERE (is_active = true);
CREATE INDEX idx_employee_attendance_date ON employee_attendance USING btree (attendance_date) WHERE (is_active = true);
