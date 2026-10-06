-- DROP SCHEMA public;

CREATE SCHEMA public AUTHORIZATION pg_database_owner;

COMMENT ON SCHEMA public IS 'standard public schema';

-- DROP SEQUENCE asset_code_asset_code_id_seq;

CREATE SEQUENCE asset_code_asset_code_id_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 2147483647
	START 1
	CACHE 1
	NO CYCLE;
-- DROP SEQUENCE asset_pack_item_pack_item_id_seq;

CREATE SEQUENCE asset_pack_item_pack_item_id_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 9223372036854775807
	START 1
	CACHE 1
	NO CYCLE;
-- DROP SEQUENCE asset_pack_transaction_pack_transaction_id_seq;

CREATE SEQUENCE asset_pack_transaction_pack_transaction_id_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 9223372036854775807
	START 1
	CACHE 1
	NO CYCLE;
-- DROP SEQUENCE asset_spare_part_asset_spare_part_id_seq;

CREATE SEQUENCE asset_spare_part_asset_spare_part_id_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 9223372036854775807
	START 1
	CACHE 1
	NO CYCLE;
-- DROP SEQUENCE consumable_item_consumable_item_id_seq;

CREATE SEQUENCE consumable_item_consumable_item_id_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 2147483647
	START 1
	CACHE 1
	NO CYCLE;
-- DROP SEQUENCE credentials_credential_id_seq;

CREATE SEQUENCE credentials_credential_id_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 2147483647
	START 1
	CACHE 1
	NO CYCLE;
-- DROP SEQUENCE currency_currency_id_seq;

CREATE SEQUENCE currency_currency_id_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 2147483647
	START 1
	CACHE 1
	NO CYCLE;
-- DROP SEQUENCE designation_designationid_seq;

CREATE SEQUENCE designation_designationid_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 2147483647
	START 1
	CACHE 1
	NO CYCLE;
-- DROP SEQUENCE employeestatus_employeestatusid_seq;

CREATE SEQUENCE employeestatus_employeestatusid_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 2147483647
	START 1
	CACHE 1
	NO CYCLE;
-- DROP SEQUENCE gin_type_gin_type_id_seq;

CREATE SEQUENCE gin_type_gin_type_id_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 2147483647
	START 1
	CACHE 1
	NO CYCLE;
-- DROP SEQUENCE inventory_item_inventory_item_id_seq;

CREATE SEQUENCE inventory_item_inventory_item_id_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 2147483647
	START 1
	CACHE 1
	NO CYCLE;
-- DROP SEQUENCE issue_item_type_issue_item_type_id_seq;

CREATE SEQUENCE issue_item_type_issue_item_type_id_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 2147483647
	START 1
	CACHE 1
	NO CYCLE;
-- DROP SEQUENCE item_brand_item_brand_id_seq;

CREATE SEQUENCE item_brand_item_brand_id_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 2147483647
	START 1
	CACHE 1
	NO CYCLE;
-- DROP SEQUENCE item_category_item_category_id_seq;

CREATE SEQUENCE item_category_item_category_id_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 2147483647
	START 1
	CACHE 1
	NO CYCLE;
-- DROP SEQUENCE item_code_item_code_id_seq;

CREATE SEQUENCE item_code_item_code_id_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 2147483647
	START 1
	CACHE 1
	NO CYCLE;
-- DROP SEQUENCE item_model_item_model_id_seq;

CREATE SEQUENCE item_model_item_model_id_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 2147483647
	START 1
	CACHE 1
	NO CYCLE;
-- DROP SEQUENCE item_optional1_item_optional1_id_seq;

CREATE SEQUENCE item_optional1_item_optional1_id_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 2147483647
	START 1
	CACHE 1
	NO CYCLE;
-- DROP SEQUENCE item_optional2_item_optional2_id_seq;

CREATE SEQUENCE item_optional2_item_optional2_id_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 2147483647
	START 1
	CACHE 1
	NO CYCLE;
-- DROP SEQUENCE item_optional3_item_optional3_id_seq;

CREATE SEQUENCE item_optional3_item_optional3_id_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 2147483647
	START 1
	CACHE 1
	NO CYCLE;
-- DROP SEQUENCE item_subcategory_item_subcategory_id_seq;

CREATE SEQUENCE item_subcategory_item_subcategory_id_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 2147483647
	START 1
	CACHE 1
	NO CYCLE;
-- DROP SEQUENCE item_subsubcategory_item_subsubcategory_id_seq;

CREATE SEQUENCE item_subsubcategory_item_subsubcategory_id_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 2147483647
	START 1
	CACHE 1
	NO CYCLE;
-- DROP SEQUENCE item_type_item_type_id_seq;

CREATE SEQUENCE item_type_item_type_id_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 2147483647
	START 1
	CACHE 1
	NO CYCLE;
-- DROP SEQUENCE job_status_type_job_status_type_id_seq;

CREATE SEQUENCE job_status_type_job_status_type_id_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 2147483647
	START 1
	CACHE 1
	NO CYCLE;
-- DROP SEQUENCE non_stock_item_non_stock_item_id_seq;

CREATE SEQUENCE non_stock_item_non_stock_item_id_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 9223372036854775807
	START 1
	CACHE 1
	NO CYCLE;
-- DROP SEQUENCE project_location_project_location_id_seq;

CREATE SEQUENCE project_location_project_location_id_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 2147483647
	START 1
	CACHE 1
	NO CYCLE;
-- DROP SEQUENCE project_phase_project_phase_id_seq;

CREATE SEQUENCE project_phase_project_phase_id_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 2147483647
	START 1
	CACHE 1
	NO CYCLE;
-- DROP SEQUENCE project_projectid_seq;

CREATE SEQUENCE project_projectid_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 2147483647
	START 1
	CACHE 1
	NO CYCLE;
-- DROP SEQUENCE project_type_project_type_id_seq;

CREATE SEQUENCE project_type_project_type_id_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 2147483647
	START 1
	CACHE 1
	NO CYCLE;
-- DROP SEQUENCE projectstatus_projectstatusid_seq;

CREATE SEQUENCE projectstatus_projectstatusid_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 2147483647
	START 1
	CACHE 1
	NO CYCLE;
-- DROP SEQUENCE service_item_service_item_id_seq;

CREATE SEQUENCE service_item_service_item_id_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 9223372036854775807
	START 1
	CACHE 1
	NO CYCLE;
-- DROP SEQUENCE subcontractor_subcontractor_id_seq;

CREATE SEQUENCE subcontractor_subcontractor_id_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 2147483647
	START 1
	CACHE 1
	NO CYCLE;
-- DROP SEQUENCE uom_uomid_seq;

CREATE SEQUENCE uom_uomid_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 2147483647
	START 1
	CACHE 1
	NO CYCLE;
-- DROP SEQUENCE workshop_department_workshop_department_id_seq;

CREATE SEQUENCE workshop_department_workshop_department_id_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 2147483647
	START 1
	CACHE 1
	NO CYCLE;-- public.currency definition

-- Drop table

-- DROP TABLE currency;

CREATE TABLE currency (
	currency_id int4 GENERATED ALWAYS AS IDENTITY( INCREMENT BY 1 MINVALUE 1 MAXVALUE 2147483647 START 1 CACHE 1 NO CYCLE) NOT NULL,
	currency_code varchar(3) NOT NULL,
	currency_name varchar(100) NOT NULL,
	CONSTRAINT currency_currency_code_key UNIQUE (currency_code),
	CONSTRAINT currency_pkey PRIMARY KEY (currency_id)
);


-- public.designation definition

-- Drop table

-- DROP TABLE designation;

CREATE TABLE designation (
	designationid int4 GENERATED BY DEFAULT AS IDENTITY( INCREMENT BY 1 MINVALUE 1 MAXVALUE 2147483647 START 1 CACHE 1 NO CYCLE) NOT NULL,
	designationname varchar(100) NOT NULL,
	CONSTRAINT pk_designation PRIMARY KEY (designationid),
	CONSTRAINT uq_designation_name UNIQUE (designationname)
);


-- public.employeestatus definition

-- Drop table

-- DROP TABLE employeestatus;

CREATE TABLE employeestatus (
	employeestatusid int4 GENERATED BY DEFAULT AS IDENTITY( INCREMENT BY 1 MINVALUE 1 MAXVALUE 2147483647 START 1 CACHE 1 NO CYCLE) NOT NULL,
	employeestatusname varchar(50) NOT NULL,
	CONSTRAINT pk_employee_status PRIMARY KEY (employeestatusid),
	CONSTRAINT uq_employee_status_name UNIQUE (employeestatusname)
);


-- public.gin_type definition

-- Drop table

-- DROP TABLE gin_type;

CREATE TABLE gin_type (
	gin_type_id int4 GENERATED ALWAYS AS IDENTITY( INCREMENT BY 1 MINVALUE 1 MAXVALUE 2147483647 START 1 CACHE 1 NO CYCLE) NOT NULL,
	gin_type_name varchar(50) NOT NULL,
	CONSTRAINT gin_type_gin_type_name_key UNIQUE (gin_type_name),
	CONSTRAINT gin_type_pkey PRIMARY KEY (gin_type_id)
);


-- public.issue_item_type definition

-- Drop table

-- DROP TABLE issue_item_type;

CREATE TABLE issue_item_type (
	issue_item_type_id int4 GENERATED ALWAYS AS IDENTITY( INCREMENT BY 1 MINVALUE 1 MAXVALUE 2147483647 START 1 CACHE 1 NO CYCLE) NOT NULL,
	issue_item_type_name varchar(100) NULL,
	CONSTRAINT issue_item_type_pkey PRIMARY KEY (issue_item_type_id)
);


-- public.item_type definition

-- Drop table

-- DROP TABLE item_type;

CREATE TABLE item_type (
	item_type_id int4 GENERATED ALWAYS AS IDENTITY( INCREMENT BY 1 MINVALUE 1 MAXVALUE 2147483647 START 1 CACHE 1 NO CYCLE) NOT NULL,
	item_type_name varchar(100) NOT NULL,
	CONSTRAINT item_type_pkey PRIMARY KEY (item_type_id)
);


-- public.job_status_type definition

-- Drop table

-- DROP TABLE job_status_type;

CREATE TABLE job_status_type (
	job_status_type_id serial4 NOT NULL,
	job_status_type_name varchar(100) NOT NULL,
	CONSTRAINT job_status_type_pkey PRIMARY KEY (job_status_type_id)
);


-- public.plant_recipe definition

-- Drop table

-- DROP TABLE plant_recipe;

CREATE TABLE plant_recipe (
	recipe_id uuid NOT NULL,
	recipe_name varchar(120) NOT NULL,
	project_code varchar(30) NOT NULL,
	product_type varchar(60) NULL,
	effective_date date NOT NULL,
	is_active bool DEFAULT true NOT NULL,
	created_by varchar(60) NULL,
	created_date timestamp DEFAULT now() NOT NULL,
	recipe_code varchar(40) NULL,
	CONSTRAINT plant_recipe_pkey PRIMARY KEY (recipe_id)
);
CREATE INDEX idx_plant_recipe_project ON public.plant_recipe USING btree (project_code);


-- public.project_type definition

-- Drop table

-- DROP TABLE project_type;

CREATE TABLE project_type (
	project_type_id int4 GENERATED ALWAYS AS IDENTITY( INCREMENT BY 1 MINVALUE 1 MAXVALUE 2147483647 START 1 CACHE 1 NO CYCLE) NOT NULL,
	project_type_name varchar(100) NOT NULL,
	CONSTRAINT project_type_pkey PRIMARY KEY (project_type_id),
	CONSTRAINT project_type_project_type_name_key UNIQUE (project_type_name)
);


-- public.projectstatus definition

-- Drop table

-- DROP TABLE projectstatus;

CREATE TABLE projectstatus (
	projectstatusid int4 GENERATED BY DEFAULT AS IDENTITY( INCREMENT BY 1 MINVALUE 1 MAXVALUE 2147483647 START 1 CACHE 1 NO CYCLE) NOT NULL,
	projectstatusname varchar(50) NOT NULL,
	CONSTRAINT pk_project_status PRIMARY KEY (projectstatusid),
	CONSTRAINT uq_project_status_name UNIQUE (projectstatusname)
);


-- public.quotation_request definition

-- Drop table

-- DROP TABLE quotation_request;

CREATE TABLE quotation_request (
	quotation_request_id uuid NOT NULL,
	quotation_request_code varchar(30) NOT NULL,
	mr_id uuid NULL,
	request_date timestamp NOT NULL,
	requested_by varchar(255) NOT NULL,
	due_date date NULL,
	remark varchar(500) NULL,
	status varchar(30) NOT NULL,
	created_at timestamp DEFAULT now() NOT NULL,
	updated_at timestamp DEFAULT now() NOT NULL,
	CONSTRAINT quotation_request_pkey PRIMARY KEY (quotation_request_id),
	CONSTRAINT quotation_request_quotation_request_code_key UNIQUE (quotation_request_code)
);


-- public.service_schedule_template definition

-- Drop table

-- DROP TABLE service_schedule_template;

CREATE TABLE service_schedule_template (
	template_id uuid NOT NULL,
	asset_type_code varchar(20) NOT NULL,
	"name" varchar(150) NOT NULL,
	meter_unit varchar(10) NOT NULL,
	is_active bool DEFAULT true NOT NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	updated_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	cycle_length numeric(12, 2) NULL,
	CONSTRAINT service_schedule_template_asset_type_code_meter_unit_key UNIQUE (asset_type_code, meter_unit),
	CONSTRAINT service_schedule_template_cycle_length_check CHECK ((cycle_length > (0)::numeric)),
	CONSTRAINT service_schedule_template_meter_unit_check CHECK (((meter_unit)::text = ANY ((ARRAY['KM'::character varying, 'HOURS'::character varying])::text[]))),
	CONSTRAINT service_schedule_template_pkey PRIMARY KEY (template_id)
);


-- public.subcontractor definition

-- Drop table

-- DROP TABLE subcontractor;

CREATE TABLE subcontractor (
	subcontractor_id serial4 NOT NULL,
	subcontractor_name varchar(150) NOT NULL,
	phone_number varchar(30) NULL,
	email varchar(150) NULL,
	line1 varchar(150) NULL,
	line2 varchar(150) NULL,
	city varchar(80) NULL,
	district varchar(80) NULL,
	is_active bool DEFAULT true NOT NULL,
	CONSTRAINT subcontractor_pkey PRIMARY KEY (subcontractor_id)
);


-- public.supplier definition

-- Drop table

-- DROP TABLE supplier;

CREATE TABLE supplier (
	suppliercode varchar(50) NOT NULL,
	suppliername varchar(150) NULL,
	line1 varchar(200) NULL,
	line2 varchar(200) NULL,
	city varchar(100) NULL,
	country varchar(100) NULL,
	phonenumber varchar(30) NULL,
	email varchar(150) NULL,
	status bool DEFAULT true NULL,
	telephonenumber varchar(30) NULL,
	accountnumber varchar(100) NULL,
	taxregistrationnumber varchar(150) NULL,
	description text NULL,
	bankname varchar(150) NULL,
	contactperson varchar(255) NULL,
	contactpersonnumber varchar(50) NULL,
	CONSTRAINT supplier_pkey PRIMARY KEY (suppliercode)
);


-- public.uom definition

-- Drop table

-- DROP TABLE uom;

CREATE TABLE uom (
	uom_id int4 GENERATED ALWAYS AS IDENTITY( INCREMENT BY 1 MINVALUE 1 MAXVALUE 2147483647 START 1 CACHE 1 NO CYCLE) NOT NULL,
	uom_name varchar(100) NOT NULL,
	CONSTRAINT uom_pkey PRIMARY KEY (uom_id)
);


-- public.workshop_department definition

-- Drop table

-- DROP TABLE workshop_department;

CREATE TABLE workshop_department (
	workshop_department_id serial4 NOT NULL,
	workshop_department_name varchar(60) NOT NULL,
	CONSTRAINT workshop_department_pkey PRIMARY KEY (workshop_department_id),
	CONSTRAINT workshop_department_workshop_department_name_key UNIQUE (workshop_department_name)
);


-- public.asset_code definition

-- Drop table

-- DROP TABLE asset_code;

CREATE TABLE asset_code (
	asset_code_id serial4 NOT NULL,
	asset_code_code varchar(100) NOT NULL,
	item_code_id int4 NULL,
	CONSTRAINT asset_code_asset_code_code_key UNIQUE (asset_code_code),
	CONSTRAINT asset_code_pkey PRIMARY KEY (asset_code_id)
);


-- public.employee definition

-- Drop table

-- DROP TABLE employee;

CREATE TABLE employee (
	employee_code varchar(50) NOT NULL,
	nic varchar(20) NOT NULL,
	dob date NULL,
	email varchar(150) NULL,
	contact_number varchar(30) NULL,
	designationid int4 NULL,
	employeestatusid int4 NULL,
	full_name varchar(255) NULL,
	employee_type varchar(50) NULL,
	temporary_y_number varchar(50) NULL,
	epf_number varchar(50) NULL,
	joined_date date NULL,
	permanent_appointment_date date NULL,
	department varchar(100) NULL,
	reporting_supervisor_code varchar(50) NULL,
	name_with_initials varchar(255) NULL,
	gender varchar(20) NULL,
	marital_status varchar(30) NULL,
	nationality varchar(50) NULL,
	alt_contact_number varchar(50) NULL,
	residential_address text NULL,
	emergency_contact_person varchar(255) NULL,
	emergency_contact_number varchar(50) NULL,
	emergency_contact_relationship varchar(100) NULL,
	bank_name varchar(100) NULL,
	bank_branch varchar(100) NULL,
	bank_account_number varchar(50) NULL,
	account_holder_name varchar(255) NULL,
	basic_salary numeric(15, 2) NULL,
	profile_photo varchar(500) NULL,
	nic_copy varchar(500) NULL,
	epf_documents varchar(500) NULL,
	remarks text NULL,
	created_at timestamp NULL,
	updated_at timestamp NULL,
	CONSTRAINT employee_email_key UNIQUE (email),
	CONSTRAINT employee_nic_key UNIQUE (nic),
	CONSTRAINT employee_pkey PRIMARY KEY (employee_code),
	CONSTRAINT fk_employee_designation FOREIGN KEY (designationid) REFERENCES designation(designationid),
	CONSTRAINT fk_employee_status FOREIGN KEY (employeestatusid) REFERENCES employeestatus(employeestatusid)
);


-- public.item_category definition

-- Drop table

-- DROP TABLE item_category;

CREATE TABLE item_category (
	item_category_id int4 GENERATED ALWAYS AS IDENTITY( INCREMENT BY 1 MINVALUE 1 MAXVALUE 2147483647 START 1 CACHE 1 NO CYCLE) NOT NULL,
	item_type_id int4 NOT NULL,
	item_category_code varchar(50) NOT NULL,
	item_category_name varchar(150) NOT NULL,
	code_slug varchar(100) NOT NULL,
	name_slug varchar(200) NOT NULL,
	CONSTRAINT item_category_pkey PRIMARY KEY (item_category_id),
	CONSTRAINT uq_item_category_type_code_slug UNIQUE (item_type_id, code_slug),
	CONSTRAINT uq_item_category_type_name_slug UNIQUE (item_type_id, name_slug),
	CONSTRAINT fk_item_category_item_type FOREIGN KEY (item_type_id) REFERENCES item_type(item_type_id) ON DELETE RESTRICT ON UPDATE CASCADE
);


-- public.item_subcategory definition

-- Drop table

-- DROP TABLE item_subcategory;

CREATE TABLE item_subcategory (
	item_subcategory_id int4 GENERATED ALWAYS AS IDENTITY( INCREMENT BY 1 MINVALUE 1 MAXVALUE 2147483647 START 1 CACHE 1 NO CYCLE) NOT NULL,
	item_category_id int4 NOT NULL,
	item_subcategory_code varchar(50) NOT NULL,
	item_subcategory_name varchar(150) NOT NULL,
	code_slug varchar(100) NOT NULL,
	name_slug varchar(200) NOT NULL,
	starting_sequence int4 DEFAULT 0 NOT NULL,
	CONSTRAINT item_subcategory_pkey PRIMARY KEY (item_subcategory_id),
	CONSTRAINT uq_item_subcategory_code_slug UNIQUE (item_category_id, code_slug),
	CONSTRAINT uq_item_subcategory_name_slug UNIQUE (item_category_id, name_slug),
	CONSTRAINT fk_item_subcategory_category FOREIGN KEY (item_category_id) REFERENCES item_category(item_category_id) ON DELETE RESTRICT ON UPDATE CASCADE
);


-- public.item_subsubcategory definition

-- Drop table

-- DROP TABLE item_subsubcategory;

CREATE TABLE item_subsubcategory (
	item_subsubcategory_id int4 GENERATED ALWAYS AS IDENTITY( INCREMENT BY 1 MINVALUE 1 MAXVALUE 2147483647 START 1 CACHE 1 NO CYCLE) NOT NULL,
	item_subcategory_id int4 NOT NULL,
	item_subsubcategory_code varchar(50) NOT NULL,
	item_subsubcategory_name varchar(150) NOT NULL,
	code_slug varchar(100) NOT NULL,
	name_slug varchar(200) NOT NULL,
	CONSTRAINT item_subsubcategory_pkey PRIMARY KEY (item_subsubcategory_id),
	CONSTRAINT uq_item_subsubcategory_code_slug UNIQUE (item_subcategory_id, code_slug),
	CONSTRAINT uq_item_subsubcategory_name_slug UNIQUE (item_subcategory_id, name_slug),
	CONSTRAINT fk_item_subsubcategory_sub_category FOREIGN KEY (item_subcategory_id) REFERENCES item_subcategory(item_subcategory_id) ON DELETE RESTRICT ON UPDATE CASCADE
);


-- public.plant_production definition

-- Drop table

-- DROP TABLE plant_production;

CREATE TABLE plant_production (
	production_id uuid NOT NULL,
	production_code varchar(40) NOT NULL,
	project_code varchar(30) NOT NULL,
	production_date date NOT NULL,
	recipe_id uuid NULL,
	status varchar(20) DEFAULT 'DRAFT'::character varying NOT NULL,
	remarks varchar(500) NULL,
	submitted_by varchar(60) NULL,
	submitted_date timestamp NULL,
	approved_by varchar(60) NULL,
	approved_date timestamp NULL,
	rejected_by varchar(60) NULL,
	rejected_date timestamp NULL,
	rejection_reason varchar(500) NULL,
	reversed_by varchar(60) NULL,
	reversed_date timestamp NULL,
	reversal_reason varchar(500) NULL,
	created_by varchar(60) NULL,
	created_date timestamp DEFAULT now() NOT NULL,
	CONSTRAINT plant_production_pkey PRIMARY KEY (production_id),
	CONSTRAINT plant_production_production_code_key UNIQUE (production_code),
	CONSTRAINT plant_production_status_check CHECK (((status)::text = ANY ((ARRAY['DRAFT'::character varying, 'SUBMITTED'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying, 'REVERSED'::character varying])::text[]))),
	CONSTRAINT plant_production_recipe_id_fkey FOREIGN KEY (recipe_id) REFERENCES plant_recipe(recipe_id) ON DELETE SET NULL
);
CREATE INDEX idx_plant_production_project ON public.plant_production USING btree (project_code, production_date);
CREATE INDEX idx_plant_production_status ON public.plant_production USING btree (status);


-- public.plant_production_expense definition

-- Drop table

-- DROP TABLE plant_production_expense;

CREATE TABLE plant_production_expense (
	production_expense_id uuid NOT NULL,
	production_id uuid NOT NULL,
	description varchar(200) NOT NULL,
	amount numeric(14, 2) NOT NULL,
	CONSTRAINT plant_production_expense_amount_check CHECK ((amount > (0)::numeric)),
	CONSTRAINT plant_production_expense_pkey PRIMARY KEY (production_expense_id),
	CONSTRAINT plant_production_expense_production_id_fkey FOREIGN KEY (production_id) REFERENCES plant_production(production_id) ON DELETE CASCADE
);
CREATE INDEX idx_plant_production_expense_production ON public.plant_production_expense USING btree (production_id);


-- public.project definition

-- Drop table

-- DROP TABLE project;

CREATE TABLE project (
	projectid int4 GENERATED BY DEFAULT AS IDENTITY( INCREMENT BY 1 MINVALUE 1 MAXVALUE 2147483647 START 1 CACHE 1 NO CYCLE) NOT NULL,
	project_code varchar(50) NOT NULL,
	projectname varchar(150) NOT NULL,
	startdate date NULL,
	enddate date NULL,
	value numeric(15, 2) NULL,
	contractnumber varchar(100) NULL,
	client varchar(150) NULL,
	area varchar(100) NULL,
	district varchar(100) NULL,
	bomlink varchar(500) NULL,
	boqlink varchar(500) NULL,
	projectstatusid int4 NULL,
	project_type_id int4 NULL,
	description varchar(255) NULL,
	actual_end_date date NULL,
	pm_code varchar(50) NULL,
	site_eng_code varchar(50) NULL,
	asst_eng_code varchar(50) NULL,
	admin_code varchar(50) NULL,
	sk_code varchar(50) NULL,
	qs_code varchar(50) NULL,
	client_contact_person varchar(255) NULL,
	client_contact_number varchar(50) NULL,
	client_email varchar(255) NULL,
	remarks text NULL,
	created_at timestamp NULL,
	updated_at timestamp NULL,
	finishvalue numeric(15, 2) NULL,
	contactnumber varchar(30) NULL,
	plant_type varchar(40) NULL,
	CONSTRAINT project_pkey PRIMARY KEY (projectid),
	CONSTRAINT project_projectcode_key UNIQUE (project_code),
	CONSTRAINT fk_project_admin FOREIGN KEY (admin_code) REFERENCES employee(employee_code),
	CONSTRAINT fk_project_asst_eng FOREIGN KEY (asst_eng_code) REFERENCES employee(employee_code),
	CONSTRAINT fk_project_pm FOREIGN KEY (pm_code) REFERENCES employee(employee_code),
	CONSTRAINT fk_project_project_type FOREIGN KEY (project_type_id) REFERENCES project_type(project_type_id),
	CONSTRAINT fk_project_qs FOREIGN KEY (qs_code) REFERENCES employee(employee_code),
	CONSTRAINT fk_project_site_eng FOREIGN KEY (site_eng_code) REFERENCES employee(employee_code),
	CONSTRAINT fk_project_sk FOREIGN KEY (sk_code) REFERENCES employee(employee_code),
	CONSTRAINT fk_project_status FOREIGN KEY (projectstatusid) REFERENCES projectstatus(projectstatusid)
);


-- public.project_location definition

-- Drop table

-- DROP TABLE project_location;

CREATE TABLE project_location (
	project_location_id serial4 NOT NULL,
	project_id int4 NOT NULL,
	"location" varchar(150) NOT NULL,
	CONSTRAINT project_location_pkey PRIMARY KEY (project_location_id),
	CONSTRAINT project_location_project_id_fkey FOREIGN KEY (project_id) REFERENCES project(projectid) ON DELETE CASCADE
);
CREATE INDEX idx_project_location_project_id ON public.project_location USING btree (project_id);


-- public.project_phase definition

-- Drop table

-- DROP TABLE project_phase;

CREATE TABLE project_phase (
	project_phase_id serial4 NOT NULL,
	project_id int4 NULL,
	project_phase_code varchar(100) NOT NULL,
	project_phase_name varchar(255) NULL,
	description varchar(500) NULL,
	start_date date NULL,
	end_date date NULL,
	status_id int4 NULL,
	parent_project_phase_id int4 NULL,
	CONSTRAINT project_phase_pkey PRIMARY KEY (project_phase_id),
	CONSTRAINT project_phase_project_id_project_phase_code_key UNIQUE (project_id, project_phase_code),
	CONSTRAINT fk_project_phase_project FOREIGN KEY (project_id) REFERENCES project(projectid),
	CONSTRAINT fk_project_phase_status FOREIGN KEY (status_id) REFERENCES projectstatus(projectstatusid),
	CONSTRAINT project_phase_parent_project_phase_id_fkey FOREIGN KEY (parent_project_phase_id) REFERENCES project_phase(project_phase_id) ON DELETE CASCADE
);
CREATE INDEX idx_project_phase_parent_id ON public.project_phase USING btree (parent_project_phase_id);


-- public.quotation definition

-- Drop table

-- DROP TABLE quotation;

CREATE TABLE quotation (
	quotation_id uuid NOT NULL,
	quotation_code varchar(60) NOT NULL,
	quotation_request_id uuid NOT NULL,
	supplier_code varchar(255) NOT NULL,
	quotation_date date NOT NULL,
	valid_until date NULL,
	currency_id int4 NULL,
	payment_term varchar(120) NULL,
	delivery_term varchar(120) NULL,
	remark varchar(500) NULL,
	total_value numeric(18, 2) NULL,
	status varchar(30) NOT NULL,
	received_date timestamp NULL,
	created_at timestamp DEFAULT now() NOT NULL,
	updated_at timestamp DEFAULT now() NOT NULL,
	CONSTRAINT quotation_pkey PRIMARY KEY (quotation_id),
	CONSTRAINT quotation_quotation_code_key UNIQUE (quotation_code),
	CONSTRAINT quotation_quotation_request_id_supplier_code_key UNIQUE (quotation_request_id, supplier_code),
	CONSTRAINT quotation_currency_id_fkey FOREIGN KEY (currency_id) REFERENCES currency(currency_id),
	CONSTRAINT quotation_quotation_request_id_fkey FOREIGN KEY (quotation_request_id) REFERENCES quotation_request(quotation_request_id) ON DELETE CASCADE,
	CONSTRAINT quotation_supplier_code_fkey FOREIGN KEY (supplier_code) REFERENCES supplier(suppliercode)
);


-- public.quotation_request_supplier definition

-- Drop table

-- DROP TABLE quotation_request_supplier;

CREATE TABLE quotation_request_supplier (
	quotation_request_id uuid NOT NULL,
	supplier_code varchar(255) NOT NULL,
	CONSTRAINT quotation_request_supplier_pkey PRIMARY KEY (quotation_request_id, supplier_code),
	CONSTRAINT quotation_request_supplier_quotation_request_id_fkey FOREIGN KEY (quotation_request_id) REFERENCES quotation_request(quotation_request_id) ON DELETE CASCADE,
	CONSTRAINT quotation_request_supplier_supplier_code_fkey FOREIGN KEY (supplier_code) REFERENCES supplier(suppliercode)
);


-- public.service_schedule_service definition

-- Drop table

-- DROP TABLE service_schedule_service;

CREATE TABLE service_schedule_service (
	service_id uuid NOT NULL,
	template_id uuid NOT NULL,
	"name" varchar(200) NOT NULL,
	at_value numeric(12, 2) NOT NULL,
	remarks varchar(500) NULL,
	sort_order int4 DEFAULT 0 NOT NULL,
	CONSTRAINT service_schedule_service_at_value_check CHECK ((at_value > (0)::numeric)),
	CONSTRAINT service_schedule_service_pkey PRIMARY KEY (service_id),
	CONSTRAINT service_schedule_service_template_id_fkey FOREIGN KEY (template_id) REFERENCES service_schedule_template(template_id) ON DELETE CASCADE
);
CREATE INDEX idx_service_schedule_service_template ON public.service_schedule_service USING btree (template_id);


-- public.service_schedule_service_part definition

-- Drop table

-- DROP TABLE service_schedule_service_part;

CREATE TABLE service_schedule_service_part (
	part_id uuid NOT NULL,
	service_id uuid NOT NULL,
	item_code varchar(50) NULL,
	part_name varchar(200) NOT NULL,
	quantity numeric(12, 2) DEFAULT 1 NOT NULL,
	unit varchar(30) NULL,
	CONSTRAINT service_schedule_service_part_pkey PRIMARY KEY (part_id),
	CONSTRAINT service_schedule_service_part_service_id_fkey FOREIGN KEY (service_id) REFERENCES service_schedule_service(service_id) ON DELETE CASCADE
);
CREATE INDEX idx_service_schedule_part_service ON public.service_schedule_service_part USING btree (service_id);


-- public.stock_adjustment definition

-- Drop table

-- DROP TABLE stock_adjustment;

CREATE TABLE stock_adjustment (
	stock_adjustment_id uuid NOT NULL,
	stock_adjustment_code varchar(50) NOT NULL,
	project_code varchar(50) NOT NULL,
	adjustment_date timestamp NOT NULL,
	reason varchar(255) NULL,
	approved_by varchar(50) NULL,
	approved_date timestamp NULL,
	is_approved bool DEFAULT false NOT NULL,
	remarks varchar(255) NULL,
	CONSTRAINT stock_adjustment_pkey PRIMARY KEY (stock_adjustment_id),
	CONSTRAINT stock_adjustment_stock_adjustment_code_key UNIQUE (stock_adjustment_code),
	CONSTRAINT fk_stock_adjustment_approved_by FOREIGN KEY (approved_by) REFERENCES employee(employee_code),
	CONSTRAINT fk_stock_adjustment_project FOREIGN KEY (project_code) REFERENCES project(project_code)
);


-- public.subcontractor_project definition

-- Drop table

-- DROP TABLE subcontractor_project;

CREATE TABLE subcontractor_project (
	subcontractor_id int4 NOT NULL,
	project_code varchar(50) NOT NULL,
	contract_link varchar(500) NULL,
	CONSTRAINT subcontractor_project_pkey PRIMARY KEY (subcontractor_id, project_code),
	CONSTRAINT subcontractor_project_project_code_fkey FOREIGN KEY (project_code) REFERENCES project(project_code),
	CONSTRAINT subcontractor_project_subcontractor_id_fkey FOREIGN KEY (subcontractor_id) REFERENCES subcontractor(subcontractor_id) ON DELETE CASCADE
);
CREATE INDEX idx_subcontractor_project_project_code ON public.subcontractor_project USING btree (project_code);


-- public.assigned_projects definition

-- Drop table

-- DROP TABLE assigned_projects;

CREATE TABLE assigned_projects (
	employee_code varchar(50) NOT NULL,
	project_id int4 NOT NULL,
	CONSTRAINT assigned_projects_pkey PRIMARY KEY (employee_code, project_id),
	CONSTRAINT fk_assigned_projects_employee FOREIGN KEY (employee_code) REFERENCES employee(employee_code),
	CONSTRAINT fk_assigned_projects_project FOREIGN KEY (project_id) REFERENCES project(projectid)
);


-- public.credentials definition

-- Drop table

-- DROP TABLE credentials;

CREATE TABLE credentials (
	credential_id int4 GENERATED ALWAYS AS IDENTITY( INCREMENT BY 1 MINVALUE 1 MAXVALUE 2147483647 START 1 CACHE 1 NO CYCLE) NOT NULL,
	employee_code varchar(50) NOT NULL,
	username varchar(100) NOT NULL,
	"password" varchar(255) NOT NULL,
	CONSTRAINT credentials_employee_code_unique UNIQUE (employee_code),
	CONSTRAINT credentials_pkey PRIMARY KEY (credential_id),
	CONSTRAINT credentials_username_unique UNIQUE (username),
	CONSTRAINT uq_credentials_username_password UNIQUE (username, password),
	CONSTRAINT fk_credentials_employee FOREIGN KEY (employee_code) REFERENCES employee(employee_code)
);


-- public.login_history definition

-- Drop table

-- DROP TABLE login_history;

CREATE TABLE login_history (
	login_history_id uuid DEFAULT gen_random_uuid() NOT NULL,
	user_name varchar(100) NOT NULL,
	employee_code varchar(50) NULL,
	login_status varchar(30) NOT NULL,
	login_time timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	logout_time timestamp NULL,
	ip_address varchar(45) NULL,
	user_agent varchar(500) NULL,
	CONSTRAINT login_history_pkey PRIMARY KEY (login_history_id),
	CONSTRAINT login_history_status_check CHECK (((login_status)::text = ANY ((ARRAY['SUCCESS'::character varying, 'FAILED_BAD_PASSWORD'::character varying, 'FAILED_UNKNOWN_USER'::character varying])::text[]))),
	CONSTRAINT fk_login_history_employee FOREIGN KEY (employee_code) REFERENCES employee(employee_code)
);
CREATE INDEX idx_login_history_employee_time ON public.login_history USING btree (employee_code, login_time DESC);
CREATE INDEX idx_login_history_login_time ON public.login_history USING btree (login_time DESC);


-- public.item_brand definition

-- Drop table

-- DROP TABLE item_brand;

CREATE TABLE item_brand (
	item_brand_id int4 GENERATED ALWAYS AS IDENTITY( INCREMENT BY 1 MINVALUE 1 MAXVALUE 2147483647 START 1 CACHE 1 NO CYCLE) NOT NULL,
	item_subsubcategory_id int4 NOT NULL,
	item_brand_code varchar(50) NOT NULL,
	item_brand_name varchar(150) NOT NULL,
	code_slug varchar(100) NOT NULL,
	name_slug varchar(200) NOT NULL,
	CONSTRAINT item_brand_pkey PRIMARY KEY (item_brand_id),
	CONSTRAINT uq_item_brand_code_slug UNIQUE (item_subsubcategory_id, code_slug),
	CONSTRAINT uq_item_brand_name_slug UNIQUE (item_subsubcategory_id, name_slug),
	CONSTRAINT fk_item_brand_subsubcategory FOREIGN KEY (item_subsubcategory_id) REFERENCES item_subsubcategory(item_subsubcategory_id) ON DELETE RESTRICT ON UPDATE CASCADE
);


-- public.item_model definition

-- Drop table

-- DROP TABLE item_model;

CREATE TABLE item_model (
	item_model_id int4 GENERATED ALWAYS AS IDENTITY( INCREMENT BY 1 MINVALUE 1 MAXVALUE 2147483647 START 1 CACHE 1 NO CYCLE) NOT NULL,
	item_brand_id int4 NOT NULL,
	item_model_code varchar(50) NOT NULL,
	item_model_name varchar(150) NOT NULL,
	code_slug varchar(100) NOT NULL,
	name_slug varchar(200) NOT NULL,
	CONSTRAINT item_model_pkey PRIMARY KEY (item_model_id),
	CONSTRAINT uq_item_model_code_slug UNIQUE (item_brand_id, code_slug),
	CONSTRAINT uq_item_model_name_slug UNIQUE (item_brand_id, name_slug),
	CONSTRAINT fk_item_model_brand FOREIGN KEY (item_brand_id) REFERENCES item_brand(item_brand_id) ON DELETE RESTRICT ON UPDATE CASCADE
);


-- public.item_optional1 definition

-- Drop table

-- DROP TABLE item_optional1;

CREATE TABLE item_optional1 (
	item_optional1_id int4 GENERATED ALWAYS AS IDENTITY( INCREMENT BY 1 MINVALUE 1 MAXVALUE 2147483647 START 1 CACHE 1 NO CYCLE) NOT NULL,
	item_model_id int4 NOT NULL,
	item_optional1_code varchar(50) NOT NULL,
	item_optional1_name varchar(150) NOT NULL,
	code_slug varchar(100) NOT NULL,
	name_slug varchar(200) NOT NULL,
	CONSTRAINT item_optional1_pkey PRIMARY KEY (item_optional1_id),
	CONSTRAINT uq_item_optional1_code_slug UNIQUE (item_model_id, code_slug),
	CONSTRAINT uq_item_optional_1_name_slug UNIQUE (item_model_id, name_slug),
	CONSTRAINT fk_item_optional1_model FOREIGN KEY (item_model_id) REFERENCES item_model(item_model_id) ON DELETE RESTRICT ON UPDATE CASCADE
);


-- public.item_optional2 definition

-- Drop table

-- DROP TABLE item_optional2;

CREATE TABLE item_optional2 (
	item_optional2_id int4 GENERATED ALWAYS AS IDENTITY( INCREMENT BY 1 MINVALUE 1 MAXVALUE 2147483647 START 1 CACHE 1 NO CYCLE) NOT NULL,
	item_optional1_id int4 NOT NULL,
	item_optional2_code varchar(50) NOT NULL,
	item_optional2_name varchar(150) NOT NULL,
	code_slug varchar(100) NOT NULL,
	name_slug varchar(200) NOT NULL,
	CONSTRAINT item_optional2_pkey PRIMARY KEY (item_optional2_id),
	CONSTRAINT uq_item_optional2_code_slug UNIQUE (item_optional1_id, code_slug),
	CONSTRAINT uq_item_optional2_name_slug UNIQUE (item_optional1_id, name_slug),
	CONSTRAINT fk_item_optional2_optional1 FOREIGN KEY (item_optional1_id) REFERENCES item_optional1(item_optional1_id) ON DELETE RESTRICT ON UPDATE CASCADE
);


-- public.item_optional3 definition

-- Drop table

-- DROP TABLE item_optional3;

CREATE TABLE item_optional3 (
	item_optional3_id int4 GENERATED ALWAYS AS IDENTITY( INCREMENT BY 1 MINVALUE 1 MAXVALUE 2147483647 START 1 CACHE 1 NO CYCLE) NOT NULL,
	item_optional2_id int4 NOT NULL,
	item_optional3_code varchar(50) NOT NULL,
	item_optional3_name varchar(150) NOT NULL,
	code_slug varchar(100) NOT NULL,
	name_slug varchar(200) NOT NULL,
	CONSTRAINT item_optional3_pkey PRIMARY KEY (item_optional3_id),
	CONSTRAINT uq_item_optional3_code_slug UNIQUE (item_optional2_id, code_slug),
	CONSTRAINT uq_item_optional3_name_slug UNIQUE (item_optional2_id, name_slug),
	CONSTRAINT fk_item_optional3_optional2 FOREIGN KEY (item_optional2_id) REFERENCES item_optional2(item_optional2_id) ON DELETE RESTRICT ON UPDATE CASCADE
);


-- public.mr definition

-- Drop table

-- DROP TABLE mr;

CREATE TABLE mr (
	mr_id uuid DEFAULT gen_random_uuid() NOT NULL,
	mr_code varchar(100) NOT NULL,
	requesting_project_code varchar(50) NOT NULL,
	destination_project_code varchar(50) NOT NULL,
	requested_date timestamp NOT NULL,
	requested_by varchar(50) NOT NULL,
	checked_date timestamp NULL,
	checked_by varchar(50) NULL,
	approved_date timestamp NULL,
	approved_by varchar(50) NULL,
	remark varchar(500) NULL,
	is_approved bool DEFAULT false NOT NULL,
	CONSTRAINT mr_mr_code_key UNIQUE (mr_code),
	CONSTRAINT mr_pkey PRIMARY KEY (mr_id),
	CONSTRAINT fk_mr_approved_by FOREIGN KEY (approved_by) REFERENCES employee(employee_code),
	CONSTRAINT fk_mr_checked_by FOREIGN KEY (checked_by) REFERENCES employee(employee_code),
	CONSTRAINT fk_mr_destination_project FOREIGN KEY (destination_project_code) REFERENCES project(project_code),
	CONSTRAINT fk_mr_requested_by FOREIGN KEY (requested_by) REFERENCES employee(employee_code),
	CONSTRAINT fk_mr_requesting_project FOREIGN KEY (requesting_project_code) REFERENCES project(project_code)
);


-- public.po definition

-- Drop table

-- DROP TABLE po;

CREATE TABLE po (
	po_id uuid NOT NULL,
	po_code varchar(50) NOT NULL,
	po_date date NULL,
	supplier_code varchar(50) NULL,
	project_code varchar(50) NULL,
	bill_to_project_code varchar(50) NULL,
	freight varchar(100) NULL,
	order_due_date date NULL,
	payment_term varchar(100) NULL,
	supplier_ref_no varchar(100) NULL,
	currency_id int4 NULL,
	vat_reg_no varchar(100) NULL,
	svat_no varchar(100) NULL,
	vat_percentage numeric(10, 2) DEFAULT 18 NULL,
	total_value numeric(15, 2) NULL,
	requested_date timestamp NULL,
	requested_by varchar(50) NULL,
	approved_date timestamp NULL,
	approved_by varchar(50) NULL,
	is_approved bool DEFAULT false NOT NULL,
	mr_id uuid NULL,
	mr_requesting_project_code varchar NULL,
	remarks varchar(1000) NULL,
	status varchar(20) DEFAULT 'ACTIVE'::character varying NOT NULL,
	status_reason varchar(500) NULL,
	status_changed_by varchar(50) NULL,
	status_changed_date timestamp NULL,
	approval_status varchar(20) DEFAULT 'PENDING'::character varying NOT NULL,
	payment_type varchar(20) DEFAULT 'Credit'::character varying NOT NULL,
	deliver_location varchar(300) NULL,
	sscl_applicable bool DEFAULT false NOT NULL,
	sscl_percentage numeric(5, 2) DEFAULT 0 NOT NULL,
	sscl_amount numeric(14, 2) DEFAULT 0 NOT NULL,
	vat_amount numeric(14, 2) DEFAULT 0 NOT NULL,
	delivery_location varchar(255) NULL,
	CONSTRAINT po_pkey PRIMARY KEY (po_id),
	CONSTRAINT po_po_code_key UNIQUE (po_code),
	CONSTRAINT fk_po_approved_by FOREIGN KEY (approved_by) REFERENCES employee(employee_code),
	CONSTRAINT fk_po_bill_to_project FOREIGN KEY (bill_to_project_code) REFERENCES project(project_code),
	CONSTRAINT fk_po_currency FOREIGN KEY (currency_id) REFERENCES currency(currency_id),
	CONSTRAINT fk_po_mr_requesting_project FOREIGN KEY (mr_requesting_project_code) REFERENCES project(project_code),
	CONSTRAINT fk_po_project FOREIGN KEY (project_code) REFERENCES project(project_code),
	CONSTRAINT fk_po_requested_by FOREIGN KEY (requested_by) REFERENCES employee(employee_code),
	CONSTRAINT fk_po_supplier FOREIGN KEY (supplier_code) REFERENCES supplier(suppliercode)
);
CREATE INDEX idx_po_approval_status ON public.po USING btree (approval_status);
CREATE INDEX idx_po_payment_type ON public.po USING btree (payment_type);
CREATE INDEX idx_po_status ON public.po USING btree (status);


-- public.po_material_request definition

-- Drop table

-- DROP TABLE po_material_request;

CREATE TABLE po_material_request (
	po_id uuid NOT NULL,
	mr_id uuid NOT NULL,
	linked_date timestamp DEFAULT now() NOT NULL,
	linked_by varchar(120) NULL,
	CONSTRAINT po_material_request_pkey PRIMARY KEY (po_id, mr_id),
	CONSTRAINT po_material_request_mr_id_fkey FOREIGN KEY (mr_id) REFERENCES mr(mr_id),
	CONSTRAINT po_material_request_po_id_fkey FOREIGN KEY (po_id) REFERENCES po(po_id)
);
CREATE INDEX idx_po_material_request_mr ON public.po_material_request USING btree (mr_id);


-- public.supplier_payment definition

-- Drop table

-- DROP TABLE supplier_payment;

CREATE TABLE supplier_payment (
	supplier_payment_id uuid NOT NULL,
	payment_code varchar(60) NOT NULL,
	supplier_code varchar(50) NOT NULL,
	project_code varchar(50) NULL,
	po_id uuid NULL,
	po_code varchar(40) NULL,
	payment_date date NOT NULL,
	payment_amount numeric(14, 2) NOT NULL,
	payment_method varchar(30) NOT NULL,
	payment_reference_no varchar(100) NULL,
	remarks varchar(500) NULL,
	recorded_by varchar(120) NULL,
	recorded_date timestamp DEFAULT now() NOT NULL,
	CONSTRAINT supplier_payment_payment_code_key UNIQUE (payment_code),
	CONSTRAINT supplier_payment_pkey PRIMARY KEY (supplier_payment_id),
	CONSTRAINT supplier_payment_po_id_fkey FOREIGN KEY (po_id) REFERENCES po(po_id),
	CONSTRAINT supplier_payment_project_code_fkey FOREIGN KEY (project_code) REFERENCES project(project_code),
	CONSTRAINT supplier_payment_supplier_code_fkey FOREIGN KEY (supplier_code) REFERENCES supplier(suppliercode)
);
CREATE INDEX idx_supplier_payment_date ON public.supplier_payment USING btree (payment_date);
CREATE INDEX idx_supplier_payment_method ON public.supplier_payment USING btree (payment_method);
CREATE INDEX idx_supplier_payment_po ON public.supplier_payment USING btree (po_id);
CREATE INDEX idx_supplier_payment_po_code ON public.supplier_payment USING btree (po_code);
CREATE INDEX idx_supplier_payment_project ON public.supplier_payment USING btree (project_code);
CREATE INDEX idx_supplier_payment_supplier ON public.supplier_payment USING btree (supplier_code);


-- public.gin definition

-- Drop table

-- DROP TABLE gin;

CREATE TABLE gin (
	gin_id uuid NOT NULL,
	gin_code varchar(50) NOT NULL,
	gin_type_id int4 NOT NULL,
	issued_date timestamp NULL,
	issued_project_code varchar(50) NULL,
	received_project_code varchar(50) NULL,
	received_by varchar(50) NULL,
	vehicle_no varchar(50) NULL,
	approved_by varchar(50) NULL,
	approved_date timestamp NULL,
	is_authorized bool DEFAULT false NOT NULL,
	expected_return_date date NULL,
	receiver_name varchar(255) NULL,
	receiver_nic varchar(50) NULL,
	sub_contractor_id int4 NULL,
	mr_id uuid NULL,
	vehicle_asset_code varchar(50) NULL,
	for_asset_code varchar(50) NULL,
	issued_by varchar(50) NULL,
	gate_verified_by varchar(255) NULL,
	gate_verified_date timestamp NULL,
	is_gate_verified bool DEFAULT false NOT NULL,
	arrival_gate_verified_by varchar(255) NULL,
	arrival_gate_verified_date timestamp NULL,
	is_arrival_gate_verified bool DEFAULT false NOT NULL,
	CONSTRAINT gin_gin_code_key UNIQUE (gin_code),
	CONSTRAINT gin_pkey PRIMARY KEY (gin_id),
	CONSTRAINT fk_gin_approved_by FOREIGN KEY (approved_by) REFERENCES employee(employee_code),
	CONSTRAINT fk_gin_for_asset_code FOREIGN KEY (for_asset_code) REFERENCES asset_code(asset_code_code),
	CONSTRAINT fk_gin_issued_by FOREIGN KEY (issued_by) REFERENCES employee(employee_code),
	CONSTRAINT fk_gin_issued_project FOREIGN KEY (issued_project_code) REFERENCES project(project_code),
	CONSTRAINT fk_gin_mr FOREIGN KEY (mr_id) REFERENCES mr(mr_id),
	CONSTRAINT fk_gin_received_person FOREIGN KEY (received_by) REFERENCES employee(employee_code),
	CONSTRAINT fk_gin_received_project FOREIGN KEY (received_project_code) REFERENCES project(project_code),
	CONSTRAINT fk_gin_type FOREIGN KEY (gin_type_id) REFERENCES gin_type(gin_type_id)
);


-- public.grn definition

-- Drop table

-- DROP TABLE grn;

CREATE TABLE grn (
	grn_id uuid NOT NULL,
	grn_code varchar(50) NOT NULL,
	from_project_code varchar(50) NULL,
	is_supplier_grn bool DEFAULT false NOT NULL,
	supplier_code varchar(50) NULL,
	invoice_date date NULL,
	to_project_code varchar(50) NULL,
	checked_date timestamp NULL,
	checked_by varchar(50) NULL,
	approved_date timestamp NULL,
	approved_by varchar(50) NULL,
	grn_date date NULL,
	po_number int4 NULL,
	is_approved bool DEFAULT false NOT NULL,
	gin_id uuid NULL,
	po_code varchar(40) NULL,
	stock_return_id uuid NULL,
	vehicle_asset_code varchar(50) NULL,
	vehicle_no varchar(50) NULL,
	gate_verified_by varchar(255) NULL,
	gate_verified_date timestamp NULL,
	is_gate_verified bool DEFAULT false NOT NULL,
	invoice_number varchar(100) NULL,
	delivery_note_number varchar(100) NULL,
	CONSTRAINT grn_grn_code_key UNIQUE (grn_code),
	CONSTRAINT grn_pkey PRIMARY KEY (grn_id),
	CONSTRAINT fk_grn_approved_by FOREIGN KEY (approved_by) REFERENCES employee(employee_code),
	CONSTRAINT fk_grn_checked_by FOREIGN KEY (checked_by) REFERENCES employee(employee_code),
	CONSTRAINT fk_grn_from_project FOREIGN KEY (from_project_code) REFERENCES project(project_code),
	CONSTRAINT fk_grn_gin FOREIGN KEY (gin_id) REFERENCES gin(gin_id),
	CONSTRAINT fk_grn_supplier FOREIGN KEY (supplier_code) REFERENCES supplier(suppliercode),
	CONSTRAINT fk_grn_to_project FOREIGN KEY (to_project_code) REFERENCES project(project_code)
);
CREATE INDEX idx_grn_po_code ON public.grn USING btree (po_code);
CREATE INDEX idx_grn_stock_return_id ON public.grn USING btree (stock_return_id);


-- public.item_code definition

-- Drop table

-- DROP TABLE item_code;

CREATE TABLE item_code (
	item_code_id int4 GENERATED ALWAYS AS IDENTITY( INCREMENT BY 1 MINVALUE 1 MAXVALUE 2147483647 START 1 CACHE 1 NO CYCLE) NOT NULL,
	item_code_code varchar(100) NOT NULL,
	item_code_name varchar(200) NOT NULL,
	item_category_id int4 NULL,
	item_subcategory_id int4 NULL,
	item_subsubcategory_id int4 NULL,
	item_brand_id int4 NULL,
	item_model_id int4 NULL,
	item_optional1_id int4 NULL,
	item_optional2_id int4 NULL,
	item_optional3_id int4 NULL,
	CONSTRAINT item_code_pkey PRIMARY KEY (item_code_id),
	CONSTRAINT uq_item_code_code UNIQUE (item_code_code),
	CONSTRAINT fk_item_code_brand FOREIGN KEY (item_brand_id) REFERENCES item_brand(item_brand_id) ON DELETE RESTRICT ON UPDATE CASCADE,
	CONSTRAINT fk_item_code_category FOREIGN KEY (item_category_id) REFERENCES item_category(item_category_id) ON DELETE RESTRICT ON UPDATE CASCADE,
	CONSTRAINT fk_item_code_model FOREIGN KEY (item_model_id) REFERENCES item_model(item_model_id) ON DELETE RESTRICT ON UPDATE CASCADE,
	CONSTRAINT fk_item_code_optional_1 FOREIGN KEY (item_optional1_id) REFERENCES item_optional1(item_optional1_id) ON DELETE RESTRICT ON UPDATE CASCADE,
	CONSTRAINT fk_item_code_optional_2 FOREIGN KEY (item_optional2_id) REFERENCES item_optional2(item_optional2_id) ON DELETE RESTRICT ON UPDATE CASCADE,
	CONSTRAINT fk_item_code_optional_3 FOREIGN KEY (item_optional3_id) REFERENCES item_optional3(item_optional3_id) ON DELETE RESTRICT ON UPDATE CASCADE,
	CONSTRAINT fk_item_code_sub_category FOREIGN KEY (item_subcategory_id) REFERENCES item_subcategory(item_subcategory_id) ON DELETE RESTRICT ON UPDATE CASCADE,
	CONSTRAINT fk_item_code_sub_sub_category FOREIGN KEY (item_subsubcategory_id) REFERENCES item_subsubcategory(item_subsubcategory_id) ON DELETE RESTRICT ON UPDATE CASCADE
);

-- asset_code.item_code_id -> item_code.item_code_id: added here (not on asset_code's own
-- CREATE TABLE) because item_code is defined later in this file.
ALTER TABLE asset_code
	ADD CONSTRAINT fk_asset_code_item_code FOREIGN KEY (item_code_id) REFERENCES item_code(item_code_id);


-- public.mr_item definition

-- Drop table

-- DROP TABLE mr_item;

CREATE TABLE mr_item (
	mr_item_id uuid DEFAULT gen_random_uuid() NOT NULL,
	mr_id uuid NOT NULL,
	item_code_code varchar(100) NOT NULL,
	description varchar(255) NULL,
	"size" varchar(100) NULL,
	uom_id int4 NOT NULL,
	quantity int4 NOT NULL,
	priority varchar(30) NULL,
	required_date date NOT NULL,
	CONSTRAINT chk_mr_item_quantity CHECK ((quantity > 0)),
	CONSTRAINT mr_item_pkey PRIMARY KEY (mr_item_id),
	CONSTRAINT fk_mr_item_item_code FOREIGN KEY (item_code_code) REFERENCES item_code(item_code_code),
	CONSTRAINT fk_mr_item_mr FOREIGN KEY (mr_id) REFERENCES mr(mr_id) ON DELETE CASCADE,
	CONSTRAINT fk_mr_item_uom FOREIGN KEY (uom_id) REFERENCES uom(uom_id)
);


-- public.non_stock_item definition

-- Drop table

-- DROP TABLE non_stock_item;

CREATE TABLE non_stock_item (
	non_stock_item_id bigserial NOT NULL,
	item_code_id int8 NOT NULL,
	uom_id int4 NOT NULL,
	is_active bool DEFAULT true NOT NULL,
	remarks varchar(500) NULL,
	CONSTRAINT non_stock_item_item_code_id_key UNIQUE (item_code_id),
	CONSTRAINT non_stock_item_pkey PRIMARY KEY (non_stock_item_id),
	CONSTRAINT non_stock_item_item_code_id_fkey FOREIGN KEY (item_code_id) REFERENCES item_code(item_code_id) ON DELETE RESTRICT,
	CONSTRAINT non_stock_item_uom_id_fkey FOREIGN KEY (uom_id) REFERENCES uom(uom_id)
);
CREATE INDEX idx_non_stock_item_uom_id ON public.non_stock_item USING btree (uom_id);


-- public.plant_production_input definition

-- Drop table

-- DROP TABLE plant_production_input;

CREATE TABLE plant_production_input (
	production_input_id uuid NOT NULL,
	production_id uuid NOT NULL,
	item_code varchar(50) NOT NULL,
	uom_id int4 NOT NULL,
	planned_quantity numeric(14, 3) NULL,
	consumed_quantity numeric(14, 3) NOT NULL,
	CONSTRAINT plant_production_input_pkey PRIMARY KEY (production_input_id),
	CONSTRAINT plant_production_input_item_code_fkey FOREIGN KEY (item_code) REFERENCES item_code(item_code_code),
	CONSTRAINT plant_production_input_production_id_fkey FOREIGN KEY (production_id) REFERENCES plant_production(production_id) ON DELETE CASCADE
);
CREATE INDEX idx_plant_production_input_production ON public.plant_production_input USING btree (production_id);


-- public.plant_production_output definition

-- Drop table

-- DROP TABLE plant_production_output;

CREATE TABLE plant_production_output (
	production_output_id uuid NOT NULL,
	production_id uuid NOT NULL,
	item_code varchar(50) NOT NULL,
	uom_id int4 NOT NULL,
	planned_quantity numeric(14, 3) NULL,
	produced_quantity numeric(14, 3) NOT NULL,
	is_waste bool DEFAULT false NOT NULL,
	CONSTRAINT plant_production_output_pkey PRIMARY KEY (production_output_id),
	CONSTRAINT plant_production_output_item_code_fkey FOREIGN KEY (item_code) REFERENCES item_code(item_code_code),
	CONSTRAINT plant_production_output_production_id_fkey FOREIGN KEY (production_id) REFERENCES plant_production(production_id) ON DELETE CASCADE
);
CREATE INDEX idx_plant_production_output_production ON public.plant_production_output USING btree (production_id);


-- public.plant_recipe_input definition

-- Drop table

-- DROP TABLE plant_recipe_input;

CREATE TABLE plant_recipe_input (
	recipe_input_id uuid NOT NULL,
	recipe_id uuid NOT NULL,
	item_code varchar(50) NOT NULL,
	quantity numeric(14, 3) NOT NULL,
	uom_id int4 NOT NULL,
	CONSTRAINT plant_recipe_input_pkey PRIMARY KEY (recipe_input_id),
	CONSTRAINT plant_recipe_input_item_code_fkey FOREIGN KEY (item_code) REFERENCES item_code(item_code_code),
	CONSTRAINT plant_recipe_input_recipe_id_fkey FOREIGN KEY (recipe_id) REFERENCES plant_recipe(recipe_id) ON DELETE CASCADE
);
CREATE INDEX idx_plant_recipe_input_recipe ON public.plant_recipe_input USING btree (recipe_id);


-- public.plant_recipe_output definition

-- Drop table

-- DROP TABLE plant_recipe_output;

CREATE TABLE plant_recipe_output (
	recipe_output_id uuid NOT NULL,
	recipe_id uuid NOT NULL,
	item_code varchar(50) NOT NULL,
	quantity numeric(14, 3) NOT NULL,
	uom_id int4 NOT NULL,
	is_primary bool DEFAULT false NOT NULL,
	is_waste bool DEFAULT false NULL,
	CONSTRAINT plant_recipe_output_pkey PRIMARY KEY (recipe_output_id),
	CONSTRAINT plant_recipe_output_item_code_fkey FOREIGN KEY (item_code) REFERENCES item_code(item_code_code),
	CONSTRAINT plant_recipe_output_recipe_id_fkey FOREIGN KEY (recipe_id) REFERENCES plant_recipe(recipe_id) ON DELETE CASCADE
);
CREATE INDEX idx_plant_recipe_output_recipe ON public.plant_recipe_output USING btree (recipe_id);


-- public.po_item definition

-- Drop table

-- DROP TABLE po_item;

CREATE TABLE po_item (
	po_item_id uuid NOT NULL,
	po_id uuid NOT NULL,
	item_code varchar(50) NOT NULL,
	description varchar(255) NULL,
	part_no varchar(100) NULL,
	uom_id int4 NULL,
	quantity int4 NOT NULL,
	unit_price numeric(15, 2) NULL,
	amount numeric(15, 2) NULL,
	supplier_name varchar(255) NULL,
	CONSTRAINT po_items_pkey PRIMARY KEY (po_item_id),
	CONSTRAINT fk_po_items_item_code FOREIGN KEY (item_code) REFERENCES item_code(item_code_code),
	CONSTRAINT fk_po_items_po FOREIGN KEY (po_id) REFERENCES po(po_id) ON DELETE CASCADE,
	CONSTRAINT fk_po_items_uom FOREIGN KEY (uom_id) REFERENCES uom(uom_id)
);


-- public.project_store definition

-- Drop table

-- DROP TABLE project_store;

CREATE TABLE project_store (
	project_code varchar(50) NOT NULL,
	item_code varchar(50) NOT NULL,
	quantity_on_hand numeric(15, 3) DEFAULT 0 NOT NULL,
	last_received_date date NULL,
	last_issued_date date NULL,
	reorder_level numeric(12, 2) DEFAULT 0 NOT NULL,
	reorder_quantity numeric(12, 2) DEFAULT 0 NOT NULL,
	bin_location varchar(50) NULL,
	CONSTRAINT pk_project_store PRIMARY KEY (project_code, item_code),
	CONSTRAINT fk_project_store_item_code FOREIGN KEY (item_code) REFERENCES item_code(item_code_code),
	CONSTRAINT fk_project_store_project FOREIGN KEY (project_code) REFERENCES project(project_code)
);


-- public.quotation_request_item definition

-- Drop table

-- DROP TABLE quotation_request_item;

CREATE TABLE quotation_request_item (
	quotation_request_item_id uuid NOT NULL,
	quotation_request_id uuid NOT NULL,
	mr_item_id uuid NULL,
	item_code_code varchar(255) NOT NULL,
	description varchar(250) NULL,
	uom_id int4 NULL,
	quantity int4 NOT NULL,
	CONSTRAINT quotation_request_item_pkey PRIMARY KEY (quotation_request_item_id),
	CONSTRAINT quotation_request_item_item_code_code_fkey FOREIGN KEY (item_code_code) REFERENCES item_code(item_code_code),
	CONSTRAINT quotation_request_item_mr_item_id_fkey FOREIGN KEY (mr_item_id) REFERENCES mr_item(mr_item_id),
	CONSTRAINT quotation_request_item_quotation_request_id_fkey FOREIGN KEY (quotation_request_id) REFERENCES quotation_request(quotation_request_id) ON DELETE CASCADE,
	CONSTRAINT quotation_request_item_uom_id_fkey FOREIGN KEY (uom_id) REFERENCES uom(uom_id)
);
CREATE INDEX idx_quotation_request_item_request ON public.quotation_request_item USING btree (quotation_request_id);


-- public.service_item definition

-- Drop table

-- DROP TABLE service_item;

CREATE TABLE service_item (
	service_item_id bigserial NOT NULL,
	item_code_id int8 NOT NULL,
	is_active bool DEFAULT true NOT NULL,
	remarks varchar(500) NULL,
	CONSTRAINT service_item_item_code_id_key UNIQUE (item_code_id),
	CONSTRAINT service_item_pkey PRIMARY KEY (service_item_id),
	CONSTRAINT service_item_item_code_id_fkey FOREIGN KEY (item_code_id) REFERENCES item_code(item_code_id) ON DELETE RESTRICT
);


-- public.stock_adjustment_item definition

-- Drop table

-- DROP TABLE stock_adjustment_item;

CREATE TABLE stock_adjustment_item (
	stock_adjustment_item_id uuid NOT NULL,
	stock_adjustment_id uuid NOT NULL,
	item_code varchar(50) NULL,
	description varchar(255) NULL,
	uom_id int4 NULL,
	adjustment_quantity int4 NOT NULL,
	unit_price numeric(15, 2) NULL,
	adjustment_value numeric(15, 2) NULL,
	remarks varchar(255) NULL,
	length_m numeric(10, 3) NULL,
	width_m numeric(10, 3) NULL,
	asset_code varchar(50) NULL,
	CONSTRAINT stock_adjustment_item_pkey PRIMARY KEY (stock_adjustment_item_id),
	CONSTRAINT fk_stock_adjustment_item_adjustment FOREIGN KEY (stock_adjustment_id) REFERENCES stock_adjustment(stock_adjustment_id) ON DELETE CASCADE,
	CONSTRAINT fk_stock_adjustment_item_item_code FOREIGN KEY (item_code) REFERENCES item_code(item_code_code),
	CONSTRAINT fk_stock_adjustment_item_uom FOREIGN KEY (uom_id) REFERENCES uom(uom_id)
);
CREATE INDEX idx_stock_adjustment_item_asset_code ON public.stock_adjustment_item USING btree (asset_code) WHERE (asset_code IS NOT NULL);


-- public.stock_batch definition

-- Drop table

-- DROP TABLE stock_batch;

CREATE TABLE stock_batch (
	stock_batch_id uuid NOT NULL,
	batch_code varchar(40) NOT NULL,
	item_code varchar(50) NOT NULL,
	origin_date date NOT NULL,
	source_type varchar(40) NOT NULL,
	source_id uuid NULL,
	source_reference varchar(60) NULL,
	original_qty numeric(14, 3) NOT NULL,
	unit_cost numeric(14, 4) NOT NULL,
	length_value numeric(10, 3) NULL,
	width_value numeric(10, 3) NULL,
	piece_count int4 NULL,
	CONSTRAINT stock_batch_batch_code_key UNIQUE (batch_code),
	CONSTRAINT stock_batch_piece_count_check CHECK (((piece_count IS NULL) OR (piece_count > 0))),
	CONSTRAINT stock_batch_pkey PRIMARY KEY (stock_batch_id),
	CONSTRAINT stock_batch_source_type_check CHECK (((source_type)::text = ANY ((ARRAY['SUPPLIER_GRN'::character varying, 'INTERNAL_GRN'::character varying, 'STOCK_RETURN'::character varying, 'STOCK_ADJUSTMENT'::character varying, 'INTRA_PROJECT_ISSUE_RETURN'::character varying, 'PLANT_PRODUCTION'::character varying, 'CUT_RETURN'::character varying])::text[]))),
	CONSTRAINT stock_batch_width_requires_length_check CHECK (((width_value IS NULL) OR (length_value IS NOT NULL))),
	CONSTRAINT stock_batch_item_code_fkey FOREIGN KEY (item_code) REFERENCES item_code(item_code_code)
);
CREATE INDEX idx_stock_batch_item ON public.stock_batch USING btree (item_code, origin_date, stock_batch_id);


-- public.stock_batch_location definition

-- Drop table

-- DROP TABLE stock_batch_location;

CREATE TABLE stock_batch_location (
	stock_batch_id uuid NOT NULL,
	project_code varchar(30) NOT NULL,
	qty_remaining numeric(14, 3) NOT NULL,
	piece_count_remaining int4 NULL,
	CONSTRAINT stock_batch_location_piece_count_remaining_check CHECK (((piece_count_remaining IS NULL) OR (piece_count_remaining >= 0))),
	CONSTRAINT stock_batch_location_pkey PRIMARY KEY (stock_batch_id, project_code),
	CONSTRAINT stock_batch_location_qty_remaining_check CHECK ((qty_remaining >= (0)::numeric)),
	CONSTRAINT stock_batch_location_stock_batch_id_fkey FOREIGN KEY (stock_batch_id) REFERENCES stock_batch(stock_batch_id) ON DELETE RESTRICT
);
CREATE INDEX idx_stock_batch_location_project ON public.stock_batch_location USING btree (project_code);


-- public.stock_batch_sequence definition

-- Drop table

-- DROP TABLE stock_batch_sequence;

CREATE TABLE stock_batch_sequence (
	item_code varchar(50) NOT NULL,
	origin_date date NOT NULL,
	last_seq int4 DEFAULT 0 NOT NULL,
	CONSTRAINT stock_batch_sequence_pkey PRIMARY KEY (item_code, origin_date),
	CONSTRAINT stock_batch_sequence_item_code_fkey FOREIGN KEY (item_code) REFERENCES item_code(item_code_code)
);


-- public.stock_return definition

-- Drop table

-- DROP TABLE stock_return;

CREATE TABLE stock_return (
	stock_return_id uuid NOT NULL,
	stock_return_code varchar(50) NOT NULL,
	from_project_code varchar(50) NOT NULL,
	to_project_code varchar(50) NULL,
	gin_id uuid NULL,
	reason varchar(255) NULL,
	remark varchar(255) NULL,
	return_date timestamp NULL,
	return_by varchar(50) NULL,
	approved_date timestamp NULL,
	approved_by varchar(50) NULL,
	is_approved bool DEFAULT false NOT NULL,
	return_type varchar(20) DEFAULT 'INTERNAL'::character varying NOT NULL,
	po_code varchar(40) NULL,
	supplier_code varchar(40) NULL,
	gate_verified_by varchar(255) NULL,
	gate_verified_date timestamp NULL,
	is_gate_verified bool DEFAULT false NOT NULL,
	arrival_gate_verified_by varchar(255) NULL,
	arrival_gate_verified_date timestamp NULL,
	is_arrival_gate_verified bool DEFAULT false NOT NULL,
	received_by varchar(50) NULL,
	vehicle_asset_code varchar(50) NULL,
	vehicle_no varchar(50) NULL,
	for_asset_code varchar(50) NULL,
	invoice_number varchar(100) NULL,
	delivery_note_number varchar(100) NULL,
	CONSTRAINT stock_return_pkey PRIMARY KEY (stock_return_id),
	CONSTRAINT stock_return_return_type_check CHECK (((return_type)::text = ANY ((ARRAY['INTERNAL'::character varying, 'SUPPLIER'::character varying])::text[]))),
	CONSTRAINT stock_return_stock_return_code_key UNIQUE (stock_return_code),
	CONSTRAINT fk_stock_return_approved_by FOREIGN KEY (approved_by) REFERENCES employee(employee_code),
	CONSTRAINT fk_stock_return_for_asset_code FOREIGN KEY (for_asset_code) REFERENCES asset_code(asset_code_code),
	CONSTRAINT fk_stock_return_from_project FOREIGN KEY (from_project_code) REFERENCES project(project_code),
	CONSTRAINT fk_stock_return_gin FOREIGN KEY (gin_id) REFERENCES gin(gin_id),
	CONSTRAINT fk_stock_return_return_by FOREIGN KEY (return_by) REFERENCES employee(employee_code),
	CONSTRAINT fk_stock_return_to_project FOREIGN KEY (to_project_code) REFERENCES project(project_code)
);
CREATE INDEX idx_stock_return_po_code ON public.stock_return USING btree (po_code);


-- public.stock_return_item definition

-- Drop table

-- DROP TABLE stock_return_item;

CREATE TABLE stock_return_item (
	stock_return_item_id uuid NOT NULL,
	stock_return_id uuid NOT NULL,
	item_code varchar(50) NULL,
	description varchar(255) NULL,
	"size" varchar(100) NULL,
	uom_id int4 NULL,
	quantity int4 NOT NULL,
	remarks varchar(255) NULL,
	unit_price numeric(12, 2) NULL,
	amount numeric(14, 2) NULL,
	asset_code varchar(50) NULL,
	CONSTRAINT stock_return_item_pkey PRIMARY KEY (stock_return_item_id),
	CONSTRAINT fk_stock_return_item_item_code FOREIGN KEY (item_code) REFERENCES item_code(item_code_code),
	CONSTRAINT fk_stock_return_item_return FOREIGN KEY (stock_return_id) REFERENCES stock_return(stock_return_id) ON DELETE CASCADE,
	CONSTRAINT fk_stock_return_item_uom FOREIGN KEY (uom_id) REFERENCES uom(uom_id)
);
CREATE INDEX idx_stock_return_item_asset_code ON public.stock_return_item USING btree (asset_code) WHERE (asset_code IS NOT NULL);



-- public.asset_location definition

-- Drop table

-- DROP TABLE asset_location;

CREATE TABLE asset_location (
	asset_location_id uuid NOT NULL,
	asset_code varchar(100) NOT NULL,
	new_location varchar(100) NOT NULL,
	changed_by varchar(100) NOT NULL,
	changed_date date NOT NULL,
	reason varchar(500) NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	updated_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	is_active bool DEFAULT true NULL,
	assigned_employee varchar(100) NULL,
	from_location varchar(150) NULL,
	movement_type varchar(30) NULL,
	source_doc_type varchar(40) NULL,
	source_doc_id uuid NULL,
	CONSTRAINT asset_location_pkey PRIMARY KEY (asset_location_id),
	CONSTRAINT fk_asset_location_asset FOREIGN KEY (asset_code) REFERENCES asset_code(asset_code_code),
	CONSTRAINT fk_asset_location_assigned_employee FOREIGN KEY (assigned_employee) REFERENCES employee(employee_code),
	CONSTRAINT fk_asset_location_employee FOREIGN KEY (changed_by) REFERENCES employee(employee_code),
	CONSTRAINT fk_asset_location_project FOREIGN KEY (new_location) REFERENCES project(project_code)
);
CREATE INDEX idx_asset_location_source_doc ON public.asset_location USING btree (source_doc_type, source_doc_id);


-- public.consumable_item definition

-- Drop table

-- DROP TABLE consumable_item;

CREATE TABLE consumable_item (
	consumable_item_id int4 GENERATED ALWAYS AS IDENTITY( INCREMENT BY 1 MINVALUE 1 MAXVALUE 2147483647 START 1 CACHE 1 NO CYCLE) NOT NULL,
	item_code_id int4 NOT NULL,
	uom_id int4 NOT NULL,
	unit_price numeric(15, 2) DEFAULT 0.00 NOT NULL,
	current_stock int4 DEFAULT 0 NOT NULL,
	is_active bool DEFAULT true NOT NULL,
	remarks varchar(500) NULL,
	part_number varchar(100) NULL,
	CONSTRAINT chk_consumable_item_current_stock CHECK ((current_stock >= 0)),
	CONSTRAINT chk_consumable_item_unit_price CHECK ((unit_price >= (0)::numeric)),
	CONSTRAINT consumable_item_pkey PRIMARY KEY (consumable_item_id),
	CONSTRAINT uq_consumable_item_code UNIQUE (item_code_id),
	CONSTRAINT fk_consumable_item_item_code FOREIGN KEY (item_code_id) REFERENCES item_code(item_code_id) ON DELETE RESTRICT ON UPDATE CASCADE,
	CONSTRAINT fk_consumable_item_uom FOREIGN KEY (uom_id) REFERENCES uom(uom_id) ON DELETE RESTRICT ON UPDATE CASCADE
);


-- public.fuel_issue definition

-- Drop table

-- DROP TABLE fuel_issue;

CREATE TABLE fuel_issue (
	fuel_issue_id uuid DEFAULT gen_random_uuid() NOT NULL,
	fuel_issue_code varchar(100) NOT NULL,
	issued_date timestamp NULL,
	project_code varchar(100) NULL,
	issued_by varchar(100) NULL,
	received_by varchar(100) NULL,
	remarks varchar(255) NULL,
	fuel_type varchar(100) NULL,
	quantity numeric(12, 2) NULL,
	meter_reading int4 NULL,
	is_issued bool DEFAULT false NULL,
	is_received bool DEFAULT false NULL,
	is_active bool DEFAULT true NULL,
	asset_code varchar(50) NULL,
	fuel_issue_date timestamp NULL,
	is_filled bool DEFAULT false NULL,
	CONSTRAINT fuel_issue_fuel_issue_code_key UNIQUE (fuel_issue_code),
	CONSTRAINT fuel_issue_pkey PRIMARY KEY (fuel_issue_id),
	CONSTRAINT fk_fuel_issue_asset FOREIGN KEY (asset_code) REFERENCES asset_code(asset_code_code),
	CONSTRAINT fk_fuel_issue_issued_by FOREIGN KEY (issued_by) REFERENCES employee(employee_code),
	CONSTRAINT fk_fuel_issue_project FOREIGN KEY (project_code) REFERENCES project(project_code),
	CONSTRAINT fk_fuel_issue_received_by FOREIGN KEY (received_by) REFERENCES employee(employee_code)
);


-- public.gin_item definition

-- Drop table

-- DROP TABLE gin_item;

CREATE TABLE gin_item (
	gin_item_id uuid NOT NULL,
	gin_id uuid NOT NULL,
	item_code varchar(50) NULL,
	description varchar(255) NULL,
	"size" varchar(100) NULL,
	uom_id int4 NULL,
	quantity int4 NULL,
	remarks varchar(255) NULL,
	issue_item_type_id int4 NULL,
	unit_price numeric(12, 2) NULL,
	amount numeric(14, 2) NULL,
	length_m numeric(10, 3) NULL,
	width_m numeric(10, 3) NULL,
	asset_code varchar(50) NULL,
	CONSTRAINT gin_item_pkey PRIMARY KEY (gin_item_id),
	CONSTRAINT fk_gin_item_asset_code FOREIGN KEY (asset_code) REFERENCES asset_code(asset_code_code),
	CONSTRAINT fk_gin_item_gin FOREIGN KEY (gin_id) REFERENCES gin(gin_id) ON DELETE CASCADE,
	CONSTRAINT fk_gin_item_issue_item_type FOREIGN KEY (issue_item_type_id) REFERENCES issue_item_type(issue_item_type_id),
	CONSTRAINT fk_gin_item_item_code FOREIGN KEY (item_code) REFERENCES item_code(item_code_code),
	CONSTRAINT fk_gin_item_uom FOREIGN KEY (uom_id) REFERENCES uom(uom_id)
);
CREATE INDEX idx_gin_item_asset_code ON public.gin_item USING btree (asset_code) WHERE (asset_code IS NOT NULL);


-- public.grn_item definition

-- Drop table

-- DROP TABLE grn_item;

CREATE TABLE grn_item (
	grn_item_id uuid DEFAULT gen_random_uuid() NOT NULL,
	grn_id uuid NOT NULL,
	item_code varchar(50) NULL,
	description varchar(255) NULL,
	supplier_name varchar(255) NULL,
	"size" varchar(100) NULL,
	uom_id int4 NULL,
	quantity numeric(18, 3) NOT NULL,
	unit_price int4 NULL,
	amount numeric(18, 2) NULL,
	conversion_factor numeric(18, 6) NULL,
	po_equivalent_qty numeric(18, 6) NULL,
	length_m numeric(10, 3) NULL,
	width_m numeric(10, 3) NULL,
	asset_code varchar(50) NULL,
	expiry_date date NULL,
	CONSTRAINT grn_items_pkey PRIMARY KEY (grn_item_id),
	CONSTRAINT fk_grn_item_asset_code FOREIGN KEY (asset_code) REFERENCES asset_code(asset_code_code),
	CONSTRAINT fk_grn_items_grn FOREIGN KEY (grn_id) REFERENCES grn(grn_id) ON DELETE CASCADE,
	CONSTRAINT fk_grn_items_item_code FOREIGN KEY (item_code) REFERENCES item_code(item_code_code),
	CONSTRAINT fk_grn_items_uom FOREIGN KEY (uom_id) REFERENCES uom(uom_id)
);
CREATE INDEX idx_grn_item_asset_code ON public.grn_item USING btree (asset_code) WHERE (asset_code IS NOT NULL);


-- public.inventory_item definition

-- Drop table

-- DROP TABLE inventory_item;

CREATE TABLE inventory_item (
	inventory_item_id int4 GENERATED ALWAYS AS IDENTITY( INCREMENT BY 1 MINVALUE 1 MAXVALUE 2147483647 START 1 CACHE 1 NO CYCLE) NOT NULL,
	item_code_id int4 NOT NULL,
	uom_id int4 NOT NULL,
	quantity_on_hand numeric(15, 2) DEFAULT 0.00 NULL,
	maximum_stock_level numeric(15, 2) NULL,
	average_unit_price numeric(15, 2) NULL,
	last_purchase_price numeric(15, 2) NULL,
	remarks varchar(500) NULL,
	is_active bool DEFAULT true NOT NULL,
	CONSTRAINT inventory_item_pkey PRIMARY KEY (inventory_item_id),
	CONSTRAINT fk_inventory_item_item_code FOREIGN KEY (item_code_id) REFERENCES item_code(item_code_id),
	CONSTRAINT fk_inventory_item_uom FOREIGN KEY (uom_id) REFERENCES uom(uom_id)
);


-- public.meter_reading definition

-- Drop table

-- DROP TABLE meter_reading;

CREATE TABLE meter_reading (
	meter_reading_id uuid DEFAULT gen_random_uuid() NOT NULL,
	asset_code varchar(100) NOT NULL,
	meter_type varchar(100) NULL,
	reading_date date NOT NULL,
	reading_value numeric(15, 2) NULL,
	previous_reading numeric(15, 2) NULL,
	usage_value numeric(15, 2) NULL,
	remarks varchar(255) NULL,
	recorded_by varchar(100) NULL,
	submitted_at timestamp NULL,
	edited_at timestamp NULL,
	CONSTRAINT meter_reading_pkey PRIMARY KEY (meter_reading_id),
	CONSTRAINT fk_meter_reading_asset FOREIGN KEY (asset_code) REFERENCES asset_code(asset_code_code),
	CONSTRAINT fk_meter_reading_employee FOREIGN KEY (recorded_by) REFERENCES employee(employee_code)
);


-- public.quotation_item definition

-- Drop table

-- DROP TABLE quotation_item;

CREATE TABLE quotation_item (
	quotation_item_id uuid NOT NULL,
	quotation_id uuid NOT NULL,
	quotation_request_item_id uuid NOT NULL,
	quantity int4 NOT NULL,
	unit_price numeric(18, 2) NOT NULL,
	amount numeric(18, 2) NOT NULL,
	remark varchar(500) NULL,
	is_selected bool DEFAULT false NOT NULL,
	CONSTRAINT quotation_item_pkey PRIMARY KEY (quotation_item_id),
	CONSTRAINT quotation_item_quotation_id_fkey FOREIGN KEY (quotation_id) REFERENCES quotation(quotation_id) ON DELETE CASCADE,
	CONSTRAINT quotation_item_quotation_request_item_id_fkey FOREIGN KEY (quotation_request_item_id) REFERENCES quotation_request_item(quotation_request_item_id) ON DELETE CASCADE
);
CREATE INDEX idx_quotation_item_quotation ON public.quotation_item USING btree (quotation_id);


-- public.service_request definition

-- Drop table

-- DROP TABLE service_request;

CREATE TABLE service_request (
	service_request_id uuid NOT NULL,
	requested_date timestamp NOT NULL,
	project_code varchar(50) NOT NULL,
	asset_code varchar(100) NOT NULL,
	operator_name varchar(150) NULL,
	phone_number varchar(30) NULL,
	maintenance_works varchar(500) NULL,
	is_approved bool DEFAULT false NOT NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	updated_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	service_request_code varchar(60) NULL,
	submitted_by varchar(50) NULL,
	CONSTRAINT service_request_pkey PRIMARY KEY (service_request_id),
	CONSTRAINT fk_service_request_asset FOREIGN KEY (asset_code) REFERENCES asset_code(asset_code_code),
	CONSTRAINT fk_service_request_project FOREIGN KEY (project_code) REFERENCES project(project_code),
	CONSTRAINT service_request_submitted_by_fkey FOREIGN KEY (submitted_by) REFERENCES employee(employee_code)
);
CREATE UNIQUE INDEX uq_service_request_code ON public.service_request USING btree (service_request_code) WHERE (service_request_code IS NOT NULL);


-- public.stock_action_allocation definition

-- Drop table

-- DROP TABLE stock_action_allocation;

CREATE TABLE stock_action_allocation (
	stock_action_allocation_id uuid NOT NULL,
	stock_batch_id uuid NOT NULL,
	action_type varchar(24) NOT NULL,
	action_id uuid NOT NULL,
	action_item_id uuid NULL,
	item_code varchar(50) NOT NULL,
	qty_taken numeric(14, 3) NOT NULL,
	unit_cost numeric(14, 4) NOT NULL,
	CONSTRAINT stock_action_allocation_action_type_check CHECK (((action_type)::text = ANY ((ARRAY['GIN'::character varying, 'INTRA_PROJECT_ISSUE'::character varying, 'STOCK_ADJUSTMENT'::character varying, 'STOCK_RETURN'::character varying, 'PLANT_PRODUCTION'::character varying, 'JOB_CARD_ISSUE'::character varying, 'FUEL_ISSUE'::character varying])::text[]))),
	CONSTRAINT stock_action_allocation_pkey PRIMARY KEY (stock_action_allocation_id),
	CONSTRAINT stock_action_allocation_item_code_fkey FOREIGN KEY (item_code) REFERENCES item_code(item_code_code),
	CONSTRAINT stock_action_allocation_stock_batch_id_fkey FOREIGN KEY (stock_batch_id) REFERENCES stock_batch(stock_batch_id) ON DELETE RESTRICT
);
CREATE INDEX idx_stock_action_allocation_action ON public.stock_action_allocation USING btree (action_type, action_id);
CREATE INDEX idx_stock_action_allocation_batch ON public.stock_action_allocation USING btree (stock_batch_id);


-- public.asset definition

-- Drop table

-- DROP TABLE asset;

CREATE TABLE asset (
	asset_code varchar(20) NOT NULL,
	asset_code_id int8 NOT NULL,
	asset_class varchar(50) NOT NULL,
	description varchar(255) NULL,
	serial_number varchar(100) NULL,
	project_or_department varchar(150) NULL,
	ownership_type varchar(20) NULL,
	status varchar(30) NOT NULL,
	"condition" varchar(20) NULL,
	remarks varchar(500) NULL,
	document_name varchar(255) NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	updated_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	CONSTRAINT asset_pkey PRIMARY KEY (asset_code),
	CONSTRAINT fk_asset_asset_code FOREIGN KEY (asset_code_id) REFERENCES asset_code(asset_code_id) ON DELETE RESTRICT
);
CREATE INDEX idx_asset_asset_class ON public.asset USING btree (asset_class);
CREATE INDEX idx_asset_asset_code_id ON public.asset USING btree (asset_code_id);


-- public.asset_document_remark definition

-- Drop table

-- DROP TABLE asset_document_remark;

CREATE TABLE asset_document_remark (
	asset_code varchar(20) NOT NULL,
	remark text DEFAULT ''::text NOT NULL,
	updated_at timestamp DEFAULT now() NOT NULL,
	CONSTRAINT asset_document_remark_pkey PRIMARY KEY (asset_code),
	CONSTRAINT asset_document_remark_asset_code_fkey FOREIGN KEY (asset_code) REFERENCES asset(asset_code) ON DELETE CASCADE
);


-- public.asset_pack_item definition

-- Drop table

-- DROP TABLE asset_pack_item;

CREATE TABLE asset_pack_item (
	pack_item_id bigserial NOT NULL,
	asset_code varchar(20) NOT NULL,
	"name" varchar(255) NOT NULL,
	description varchar(500) NULL,
	serial_number varchar(100) NULL,
	quantity int4 DEFAULT 1 NOT NULL,
	is_with_asset bool DEFAULT true NOT NULL,
	remarks varchar(500) NULL,
	is_active bool DEFAULT true NOT NULL,
	created_at timestamp DEFAULT now() NOT NULL,
	updated_at timestamp DEFAULT now() NOT NULL,
	CONSTRAINT asset_pack_item_pkey PRIMARY KEY (pack_item_id),
	CONSTRAINT asset_pack_item_asset_code_fkey FOREIGN KEY (asset_code) REFERENCES asset(asset_code) ON DELETE CASCADE
);
CREATE INDEX idx_asset_pack_item_asset ON public.asset_pack_item USING btree (asset_code);


-- public.asset_pack_transaction definition

-- Drop table

-- DROP TABLE asset_pack_transaction;

CREATE TABLE asset_pack_transaction (
	pack_transaction_id bigserial NOT NULL,
	doc_type varchar(30) NOT NULL,
	doc_id uuid NOT NULL,
	asset_code varchar(20) NOT NULL,
	pack_item_id int8 NOT NULL,
	included bool NOT NULL,
	created_at timestamp DEFAULT now() NOT NULL,
	CONSTRAINT asset_pack_transaction_pkey PRIMARY KEY (pack_transaction_id),
	CONSTRAINT asset_pack_transaction_pack_item_id_fkey FOREIGN KEY (pack_item_id) REFERENCES asset_pack_item(pack_item_id) ON DELETE CASCADE
);
CREATE INDEX idx_asset_pack_txn_doc ON public.asset_pack_transaction USING btree (doc_type, doc_id);
CREATE INDEX idx_asset_pack_txn_item ON public.asset_pack_transaction USING btree (pack_item_id);


-- public.asset_spare_part definition

-- Drop table

-- DROP TABLE asset_spare_part;

CREATE TABLE asset_spare_part (
	asset_spare_part_id bigserial NOT NULL,
	asset_code varchar(20) NOT NULL,
	item_code_id int8 NOT NULL,
	part_role varchar(255) NOT NULL,
	part_number varchar(100) NULL,
	serial_number varchar(100) NULL,
	quantity_per_unit numeric(12, 3) DEFAULT 1 NOT NULL,
	remarks varchar(500) NULL,
	is_active bool DEFAULT true NOT NULL,
	created_at timestamp DEFAULT now() NOT NULL,
	updated_at timestamp DEFAULT now() NOT NULL,
	CONSTRAINT asset_spare_part_pkey PRIMARY KEY (asset_spare_part_id),
	CONSTRAINT asset_spare_part_asset_code_fkey FOREIGN KEY (asset_code) REFERENCES asset(asset_code) ON DELETE CASCADE,
	CONSTRAINT asset_spare_part_item_code_id_fkey FOREIGN KEY (item_code_id) REFERENCES item_code(item_code_id) ON DELETE RESTRICT
);
CREATE UNIQUE INDEX asset_spare_part_unique ON public.asset_spare_part USING btree (asset_code, item_code_id, part_role, COALESCE(serial_number, ''::character varying));
CREATE INDEX idx_asset_spare_part_asset ON public.asset_spare_part USING btree (asset_code);
CREATE INDEX idx_asset_spare_part_item ON public.asset_spare_part USING btree (item_code_id);


-- public.building_asset_detail definition

-- Drop table

-- DROP TABLE building_asset_detail;

CREATE TABLE building_asset_detail (
	asset_code varchar(20) NOT NULL,
	building_type varchar(50) NOT NULL,
	address_location varchar(255) NOT NULL,
	land_area numeric(14, 2) NULL,
	floor_area_sqft numeric(14, 2) NULL,
	number_of_floors int4 NULL,
	year_built int4 NULL,
	construction_type varchar(100) NULL,
	ownership_type varchar(50) NULL,
	title_deed_number varchar(100) NULL,
	registration_number varchar(100) NULL,
	encumbrances varchar(255) NULL,
	valuation_amount numeric(16, 2) NULL,
	valuation_date date NULL,
	insurance_company varchar(150) NULL,
	insurance_policy_number varchar(100) NULL,
	insurance_expiry_date date NULL,
	occupancy_status varchar(30) NULL,
	responsible_department varchar(150) NULL,
	purchase_date date NULL,
	purchase_value numeric(16, 2) NULL,
	supporting_documents varchar(255) NULL,
	CONSTRAINT building_asset_detail_pkey PRIMARY KEY (asset_code),
	CONSTRAINT fk_building_asset FOREIGN KEY (asset_code) REFERENCES asset(asset_code) ON DELETE CASCADE
);


-- public.electrical_equipment_asset_detail definition

-- Drop table

-- DROP TABLE electrical_equipment_asset_detail;

CREATE TABLE electrical_equipment_asset_detail (
	asset_code varchar(20) NOT NULL,
	equipment_type varchar(50) NOT NULL,
	make varchar(100) NULL,
	model varchar(100) NULL,
	manufacturer_serial_number varchar(100) NULL,
	country_of_manufacture varchar(100) NULL,
	year_of_manufacture int4 NULL,
	installation_type varchar(20) NULL,
	rated_power numeric(10, 2) NULL,
	horsepower numeric(10, 2) NULL,
	rated_voltage numeric(10, 2) NULL,
	rated_current numeric(10, 2) NULL,
	frequency numeric(10, 2) NULL,
	number_of_phases varchar(20) NULL,
	speed_rpm numeric(10, 2) NULL,
	power_factor numeric(6, 3) NULL,
	efficiency_class varchar(50) NULL,
	insulation_class varchar(50) NULL,
	protection_rating varchar(50) NULL,
	connection_type varchar(100) NULL,
	duty_type varchar(50) NULL,
	equipment_capacity varchar(50) NULL,
	capacity_unit varchar(50) NULL,
	input_rating varchar(50) NULL,
	output_rating varchar(50) NULL,
	pressure_rating varchar(50) NULL,
	flow_rate varchar(50) NULL,
	cooling_method varchar(50) NULL,
	transformer_rating_kva numeric(10, 2) NULL,
	generator_rating_kva numeric(10, 2) NULL,
	pump_head_m numeric(10, 2) NULL,
	installation_date date NULL,
	installation_location varchar(150) NULL,
	panel_circuit_reference varchar(100) NULL,
	connected_load varchar(100) NULL,
	responsible_department varchar(150) NULL,
	CONSTRAINT electrical_equipment_asset_detail_pkey PRIMARY KEY (asset_code),
	CONSTRAINT electrical_equipment_asset_detail_asset_code_fkey FOREIGN KEY (asset_code) REFERENCES asset(asset_code) ON DELETE CASCADE
);


-- public.furniture_asset_detail definition

-- Drop table

-- DROP TABLE furniture_asset_detail;

CREATE TABLE furniture_asset_detail (
	asset_code varchar(20) NOT NULL,
	furniture_type varchar(100) NULL,
	make varchar(100) NULL,
	model varchar(100) NULL,
	material varchar(100) NULL,
	colour varchar(50) NULL,
	length numeric(10, 2) NULL,
	width numeric(10, 2) NULL,
	height numeric(10, 2) NULL,
	supplier varchar(150) NULL,
	invoice_number varchar(100) NULL,
	purchase_date date NULL,
	warranty_period varchar(50) NULL,
	CONSTRAINT furniture_asset_detail_pkey PRIMARY KEY (asset_code),
	CONSTRAINT furniture_asset_detail_asset_number_fkey FOREIGN KEY (asset_code) REFERENCES asset(asset_code) ON DELETE CASCADE
);


-- public.it_equipment_asset_detail definition

-- Drop table

-- DROP TABLE it_equipment_asset_detail;

CREATE TABLE it_equipment_asset_detail (
	asset_code varchar(20) NOT NULL,
	equipment_type varchar(50) NOT NULL,
	make varchar(100) NULL,
	model varchar(100) NULL,
	manufacturer_serial_number varchar(100) NULL,
	asset_tag varchar(100) NULL,
	device_name_hostname varchar(150) NULL,
	year_of_manufacture int4 NULL,
	colour varchar(50) NULL,
	processor_main_specification varchar(150) NULL,
	ram_capacity varchar(50) NULL,
	storage_capacity varchar(50) NULL,
	operating_system varchar(100) NULL,
	screen_size varchar(50) NULL,
	network_mac_address varchar(100) NULL,
	ip_address varchar(50) NULL,
	power_rating_capacity varchar(50) NULL,
	voltage numeric(10, 2) NULL,
	included_accessories varchar(500) NULL,
	assigned_department varchar(150) NULL,
	assigned_project_location varchar(150) NULL,
	purchase_date date NULL,
	warranty_expiry_date date NULL,
	supplier varchar(150) NULL,
	purchase_value numeric(14, 2) NULL,
	CONSTRAINT it_equipment_asset_detail_pkey PRIMARY KEY (asset_code),
	CONSTRAINT it_equipment_asset_detail_asset_code_fkey FOREIGN KEY (asset_code) REFERENCES asset(asset_code) ON DELETE CASCADE
);


-- public.job_card definition

-- Drop table

-- DROP TABLE job_card;

CREATE TABLE job_card (
	job_card_id uuid DEFAULT gen_random_uuid() NOT NULL,
	job_card_code varchar(50) NOT NULL,
	created_date timestamp NULL,
	job_status_type_id int4 NULL,
	updated_at timestamp NULL,
	job_checked_by varchar(50) NULL,
	job_checked_date timestamp NULL,
	remarks varchar(500) NULL,
	is_finished bool DEFAULT false NULL,
	"cost" numeric(15, 2) NULL,
	job_type varchar NOT NULL,
	parent_job_card_id uuid NULL,
	department varchar(100) NULL,
	service_request_id uuid NULL,
	requesting_project_code varchar(60) NULL,
	asset_code varchar(60) NULL,
	make varchar(100) NULL,
	"type" varchar(100) NULL,
	meter_reading numeric(12, 2) NULL,
	informed_by varchar(120) NULL,
	phone_number varchar(30) NULL,
	is_delivered bool DEFAULT false NOT NULL,
	project_code varchar(60) NULL,
	expected_hours_to_complete numeric(12, 2) NULL,
	CONSTRAINT job_card_job_card_code_key UNIQUE (job_card_code),
	CONSTRAINT job_card_pkey PRIMARY KEY (job_card_id),
	CONSTRAINT fk_job_card_checked_by FOREIGN KEY (job_checked_by) REFERENCES employee(employee_code),
	CONSTRAINT fk_job_card_status_type FOREIGN KEY (job_status_type_id) REFERENCES job_status_type(job_status_type_id),
	CONSTRAINT job_card_parent_job_card_id_fkey FOREIGN KEY (parent_job_card_id) REFERENCES job_card(job_card_id) ON DELETE CASCADE,
	CONSTRAINT job_card_service_request_id_fkey FOREIGN KEY (service_request_id) REFERENCES service_request(service_request_id)
);
CREATE INDEX idx_job_card_parent_job_card_id ON public.job_card USING btree (parent_job_card_id);
CREATE INDEX idx_job_card_service_request_id ON public.job_card USING btree (service_request_id);


-- public.job_card_service definition

-- Drop table

-- DROP TABLE job_card_service;

CREATE TABLE job_card_service (
	job_card_id uuid NOT NULL,
	service_id uuid NOT NULL,
	CONSTRAINT job_card_service_pkey PRIMARY KEY (job_card_id, service_id),
	CONSTRAINT job_card_service_job_card_id_fkey FOREIGN KEY (job_card_id) REFERENCES job_card(job_card_id) ON DELETE CASCADE,
	CONSTRAINT job_card_service_service_id_fkey FOREIGN KEY (service_id) REFERENCES service_schedule_service(service_id) ON DELETE CASCADE
);


-- public.job_check_list definition

-- Drop table

-- DROP TABLE job_check_list;

CREATE TABLE job_check_list (
	job_card_id uuid NOT NULL,
	q1 bool NULL,
	q2 bool NULL,
	q3 bool NULL,
	q4 bool NULL,
	q5 bool NULL,
	q6 bool NULL,
	q7 bool NULL,
	q8 bool NULL,
	q9 bool NULL,
	q10 bool NULL,
	q11 bool NULL,
	q12 bool NULL,
	q1_remark varchar(500) NULL,
	q2_remark varchar(500) NULL,
	q3_remark varchar(500) NULL,
	q4_remark varchar(500) NULL,
	q5_remark varchar(500) NULL,
	q6_remark varchar(500) NULL,
	q7_remark varchar(500) NULL,
	q8_remark varchar(500) NULL,
	q9_remark varchar(500) NULL,
	q10_remark varchar(500) NULL,
	q11_remark varchar(500) NULL,
	q12_remark varchar(500) NULL,
	CONSTRAINT job_check_list_pkey PRIMARY KEY (job_card_id),
	CONSTRAINT fk_job_check_list_job_card FOREIGN KEY (job_card_id) REFERENCES job_card(job_card_id) ON DELETE CASCADE
);


-- public.job_cost_entry definition

-- Drop table

-- DROP TABLE job_cost_entry;

CREATE TABLE job_cost_entry (
	job_cost_entry_id uuid DEFAULT gen_random_uuid() NOT NULL,
	job_card_id uuid NOT NULL,
	description varchar(500) NULL,
	amount numeric(15, 2) NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	updated_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	CONSTRAINT job_cost_entry_pkey PRIMARY KEY (job_cost_entry_id),
	CONSTRAINT fk_job_cost_entry_job_card FOREIGN KEY (job_card_id) REFERENCES job_card(job_card_id)
);


-- public.job_issue_item definition

-- Drop table

-- DROP TABLE job_issue_item;

CREATE TABLE job_issue_item (
	job_issue_item_id uuid DEFAULT gen_random_uuid() NOT NULL,
	job_card_id uuid NULL,
	item_code varchar(100) NULL,
	part_number varchar(100) NULL,
	serial_number varchar(100) NULL,
	description varchar(500) NULL,
	unit_price numeric(15, 2) NULL,
	quantity numeric(15, 2) NULL,
	issued_by varchar(50) NULL,
	issued_date timestamp NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	updated_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	project_code varchar(60) NULL,
	CONSTRAINT job_issue_item_pkey PRIMARY KEY (job_issue_item_id),
	CONSTRAINT fk_job_issue_item_employee FOREIGN KEY (issued_by) REFERENCES employee(employee_code),
	CONSTRAINT fk_job_issue_item_item_code FOREIGN KEY (item_code) REFERENCES item_code(item_code_code),
	CONSTRAINT fk_job_issue_item_job_card FOREIGN KEY (job_card_id) REFERENCES job_card(job_card_id)
);


-- public.job_issue_item_return definition

-- Drop table

-- DROP TABLE job_issue_item_return;

CREATE TABLE job_issue_item_return (
	job_issue_item_return_id uuid NOT NULL,
	job_card_id uuid NOT NULL,
	job_issue_item_id uuid NOT NULL,
	item_code varchar(50) NOT NULL,
	quantity numeric(14, 3) NOT NULL,
	unit_price numeric(14, 4) NULL,
	returned_by varchar(50) NULL,
	returned_date timestamp NOT NULL,
	remarks varchar(200) NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	updated_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	project_code varchar(60) NULL,
	CONSTRAINT job_issue_item_return_pkey PRIMARY KEY (job_issue_item_return_id),
	CONSTRAINT job_issue_item_return_quantity_check CHECK ((quantity > (0)::numeric)),
	CONSTRAINT job_issue_item_return_job_card_id_fkey FOREIGN KEY (job_card_id) REFERENCES job_card(job_card_id) ON DELETE CASCADE,
	CONSTRAINT job_issue_item_return_job_issue_item_id_fkey FOREIGN KEY (job_issue_item_id) REFERENCES job_issue_item(job_issue_item_id),
	CONSTRAINT job_issue_item_return_returned_by_fkey FOREIGN KEY (returned_by) REFERENCES employee(employee_code)
);
CREATE INDEX idx_job_issue_item_return_job_card_id ON public.job_issue_item_return USING btree (job_card_id);
CREATE INDEX idx_job_issue_item_return_job_issue_item_id ON public.job_issue_item_return USING btree (job_issue_item_id);


-- public.job_worker definition

-- Drop table

-- DROP TABLE job_worker;

CREATE TABLE job_worker (
	job_worker_id uuid DEFAULT gen_random_uuid() NOT NULL,
	job_card_id uuid NOT NULL,
	employee_code varchar(50) NOT NULL,
	hours_worked numeric(7, 2) DEFAULT 0 NOT NULL,
	CONSTRAINT job_worker_pkey PRIMARY KEY (job_worker_id),
	CONSTRAINT fk_job_worker_employee FOREIGN KEY (employee_code) REFERENCES employee(employee_code),
	CONSTRAINT fk_job_worker_job_card FOREIGN KEY (job_card_id) REFERENCES job_card(job_card_id)
);


-- public.lab_equipment_asset_detail definition

-- Drop table

-- DROP TABLE lab_equipment_asset_detail;

CREATE TABLE lab_equipment_asset_detail (
	asset_code varchar(20) NOT NULL,
	equipment_type varchar(50) NOT NULL,
	make varchar(100) NULL,
	model varchar(100) NULL,
	manufacturer_serial_number varchar(100) NULL,
	country_of_manufacture varchar(100) NULL,
	year_of_manufacture int4 NULL,
	capacity varchar(50) NULL,
	capacity_unit varchar(50) NULL,
	accuracy varchar(50) NULL,
	operating_temperature_range varchar(50) NULL,
	power_rating numeric(10, 2) NULL,
	voltage numeric(10, 2) NULL,
	calibration_date date NULL,
	calibration_due_date date NULL,
	calibration_certificate_number varchar(100) NULL,
	calibration_certificate_document varchar(255) NULL,
	safety_certification varchar(150) NULL,
	included_accessories varchar(500) NULL,
	supplier varchar(150) NULL,
	purchase_date date NULL,
	purchase_value numeric(14, 2) NULL,
	warranty_expiry_date date NULL,
	supporting_documents varchar(255) NULL,
	CONSTRAINT lab_equipment_asset_detail_pkey PRIMARY KEY (asset_code),
	CONSTRAINT fk_lab_equipment_asset FOREIGN KEY (asset_code) REFERENCES asset(asset_code) ON DELETE CASCADE
);


-- public.land_asset_detail definition

-- Drop table

-- DROP TABLE land_asset_detail;

CREATE TABLE land_asset_detail (
	asset_code varchar(20) NOT NULL,
	land_type varchar(50) NOT NULL,
	address_location varchar(255) NOT NULL,
	land_area numeric(14, 2) NULL,
	area_unit varchar(20) NULL,
	survey_plan_number varchar(100) NULL,
	boundary_description varchar(500) NULL,
	gps_latitude numeric(10, 6) NULL,
	gps_longitude numeric(10, 6) NULL,
	ownership_type varchar(50) NULL,
	title_deed_number varchar(100) NULL,
	registration_number varchar(100) NULL,
	encumbrances varchar(255) NULL,
	valuation_amount numeric(16, 2) NULL,
	valuation_date date NULL,
	purchase_date date NULL,
	purchase_value numeric(16, 2) NULL,
	supporting_documents varchar(255) NULL,
	CONSTRAINT land_asset_detail_pkey PRIMARY KEY (asset_code),
	CONSTRAINT fk_land_asset FOREIGN KEY (asset_code) REFERENCES asset(asset_code) ON DELETE CASCADE
);


-- public.machinery_asset_detail definition

-- Drop table

-- DROP TABLE machinery_asset_detail;

CREATE TABLE machinery_asset_detail (
	asset_code varchar(20) NOT NULL,
	machinery_type varchar(50) NOT NULL,
	make varchar(100) NULL,
	model varchar(100) NULL,
	country_of_manufacture varchar(100) NULL,
	year_of_manufacture int4 NULL,
	manufacturer_serial_number varchar(100) NULL,
	chassis_frame_number varchar(100) NULL,
	registration_number varchar(50) NULL,
	colour varchar(50) NULL,
	engine_number varchar(100) NULL,
	engine_make varchar(100) NULL,
	engine_model varchar(100) NULL,
	fuel_type varchar(255) NULL,
	engine_capacity_cc int4 NULL,
	number_of_cylinders int4 NULL,
	engine_power_hp numeric(10, 2) NULL,
	engine_power_kw numeric(10, 2) NULL,
	rated_rpm numeric(10, 2) NULL,
	fuel_tank_capacity_l numeric(10, 2) NULL,
	average_fuel_consumption numeric(10, 2) NULL,
	fuel_consumption_unit varchar(20) NULL,
	meter_type varchar(30) NULL,
	initial_meter_reading varchar(50) NULL,
	operating_capacity varchar(50) NULL,
	operating_capacity_unit varchar(50) NULL,
	machine_weight_kg numeric(10, 2) NULL,
	load_lift_capacity varchar(50) NULL,
	bucket_blade_capacity varchar(50) NULL,
	running_system varchar(20) NULL,
	number_of_tyres int4 NULL,
	front_tyre_size varchar(50) NULL,
	rear_tyre_size varchar(50) NULL,
	track_size_width varchar(50) NULL,
	tyre_track_brand varchar(100) NULL,
	battery_brand varchar(100) NULL,
	battery_model varchar(100) NULL,
	battery_voltage numeric(10, 2) NULL,
	battery_capacity_ah numeric(10, 2) NULL,
	number_of_batteries int4 NULL,
	CONSTRAINT machinery_asset_detail_pkey PRIMARY KEY (asset_code),
	CONSTRAINT machinery_asset_detail_asset_code_fkey FOREIGN KEY (asset_code) REFERENCES asset(asset_code) ON DELETE CASCADE
);


-- public.other_asset_detail definition

-- Drop table

-- DROP TABLE other_asset_detail;

CREATE TABLE other_asset_detail (
	asset_code varchar(20) NOT NULL,
	item_description varchar(255) NOT NULL,
	specification varchar(500) NULL,
	make varchar(100) NULL,
	model varchar(100) NULL,
	manufacturer_serial_number varchar(100) NULL,
	quantity int4 NULL,
	unit_of_measure varchar(50) NULL,
	supplier varchar(150) NULL,
	purchase_date date NULL,
	purchase_value numeric(14, 2) NULL,
	warranty_expiry_date date NULL,
	supporting_documents varchar(255) NULL,
	CONSTRAINT other_asset_detail_pkey PRIMARY KEY (asset_code),
	CONSTRAINT fk_other_asset FOREIGN KEY (asset_code) REFERENCES asset(asset_code) ON DELETE CASCADE
);


-- public.plant_equipment_asset_detail definition

-- Drop table

-- DROP TABLE plant_equipment_asset_detail;

CREATE TABLE plant_equipment_asset_detail (
	asset_code varchar(20) NOT NULL,
	plant_type varchar(50) NOT NULL,
	plant_name varchar(150) NOT NULL,
	plant_code_asset_number varchar(100) NULL,
	make varchar(100) NULL,
	model varchar(100) NULL,
	manufacturer varchar(150) NULL,
	manufacturer_serial_number varchar(100) NULL,
	country_of_manufacture varchar(100) NULL,
	year_of_manufacture int4 NULL,
	plant_configuration varchar(30) NULL,
	production_capacity numeric(14, 2) NULL,
	capacity_unit varchar(50) NULL,
	main_power_source varchar(100) NULL,
	fuel_type varchar(255) NULL,
	assigned_project varchar(150) NULL,
	installation_date date NULL,
	commissioning_date date NULL,
	purchase_date date NULL,
	supplier varchar(150) NULL,
	purchase_value numeric(14, 2) NULL,
	warranty_expiry_date date NULL,
	responsible_employee_department varchar(150) NULL,
	gps_latitude numeric(10, 6) NULL,
	gps_longitude numeric(10, 6) NULL,
	plant_images varchar(255) NULL,
	supporting_documents varchar(255) NULL,
	CONSTRAINT plant_equipment_asset_detail_pkey PRIMARY KEY (asset_code),
	CONSTRAINT plant_equipment_asset_detail_asset_code_fkey FOREIGN KEY (asset_code) REFERENCES asset(asset_code) ON DELETE CASCADE
);


-- public.power_tool_asset_detail definition

-- Drop table

-- DROP TABLE power_tool_asset_detail;

CREATE TABLE power_tool_asset_detail (
	asset_code varchar(20) NOT NULL,
	tool_type varchar(50) NOT NULL,
	make varchar(100) NULL,
	model varchar(100) NULL,
	manufacturer_serial_number varchar(100) NULL,
	country_of_manufacture varchar(100) NULL,
	year_of_manufacture int4 NULL,
	power_source varchar(20) NOT NULL,
	intended_use varchar(255) NULL,
	rated_power numeric(10, 2) NULL,
	voltage numeric(10, 2) NULL,
	current_amps numeric(10, 2) NULL,
	frequency numeric(10, 2) NULL,
	speed_rpm numeric(10, 2) NULL,
	capacity_tool_size varchar(50) NULL,
	capacity_unit varchar(50) NULL,
	chuck_disc_blade_size varchar(50) NULL,
	weight_kg numeric(10, 2) NULL,
	battery_type varchar(50) NULL,
	battery_voltage numeric(10, 2) NULL,
	battery_capacity_ah numeric(10, 2) NULL,
	number_of_batteries int4 NULL,
	charger_model_serial_number varchar(100) NULL,
	fuel_type varchar(255) NULL,
	engine_capacity_cc int4 NULL,
	fuel_tank_capacity_l numeric(10, 2) NULL,
	engine_number varchar(100) NULL,
	safety_class_protection_rating varchar(100) NULL,
	included_accessories varchar(500) NULL,
	carrying_case_number varchar(100) NULL,
	CONSTRAINT power_tool_asset_detail_pkey PRIMARY KEY (asset_code),
	CONSTRAINT power_tool_asset_detail_asset_code_fkey FOREIGN KEY (asset_code) REFERENCES asset(asset_code) ON DELETE CASCADE
);


-- public.reusable_tool_asset_detail definition

-- Drop table

-- DROP TABLE reusable_tool_asset_detail;

CREATE TABLE reusable_tool_asset_detail (
	asset_code varchar(20) NOT NULL,
	tool_type varchar(50) NOT NULL,
	make varchar(100) NULL,
	model varchar(100) NULL,
	manufacturer_serial_number varchar(100) NULL,
	material varchar(100) NULL,
	size_dimensions varchar(100) NULL,
	weight_kg numeric(10, 2) NULL,
	quantity_in_set int4 NULL,
	storage_location varchar(150) NULL,
	condition_notes varchar(500) NULL,
	included_accessories varchar(500) NULL,
	supplier varchar(150) NULL,
	purchase_date date NULL,
	purchase_value numeric(14, 2) NULL,
	warranty_expiry_date date NULL,
	supporting_documents varchar(255) NULL,
	CONSTRAINT reusable_tool_asset_detail_pkey PRIMARY KEY (asset_code),
	CONSTRAINT fk_reusable_tool_asset FOREIGN KEY (asset_code) REFERENCES asset(asset_code) ON DELETE CASCADE
);


-- public.service_receive_note definition

-- Drop table

-- DROP TABLE service_receive_note;

CREATE TABLE service_receive_note (
	service_receive_note_id uuid NOT NULL,
	service_receive_note_code varchar(50) NOT NULL,
	job_card_id uuid NOT NULL,
	job_card_code varchar(50) NOT NULL,
	requesting_project_code varchar(50) NOT NULL,
	asset_code varchar(50) NULL,
	item_code varchar(50) NULL,
	quantity numeric(12, 2) DEFAULT 1 NOT NULL,
	received_date timestamp NOT NULL,
	received_by varchar(100) NULL,
	remarks varchar(500) NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	updated_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	service_request_id uuid NULL,
	CONSTRAINT service_receive_note_pkey PRIMARY KEY (service_receive_note_id),
	CONSTRAINT service_receive_note_service_receive_note_code_key UNIQUE (service_receive_note_code),
	CONSTRAINT service_receive_note_job_card_id_fkey FOREIGN KEY (job_card_id) REFERENCES job_card(job_card_id),
	CONSTRAINT service_receive_note_service_request_id_fkey FOREIGN KEY (service_request_id) REFERENCES service_request(service_request_id)
);
CREATE INDEX idx_service_receive_note_job_card ON public.service_receive_note USING btree (job_card_id);
CREATE INDEX idx_service_receive_note_requesting_project ON public.service_receive_note USING btree (requesting_project_code);


-- public.survey_instrument_asset_detail definition

-- Drop table

-- DROP TABLE survey_instrument_asset_detail;

CREATE TABLE survey_instrument_asset_detail (
	asset_code varchar(20) NOT NULL,
	instrument_type varchar(50) NOT NULL,
	make varchar(100) NULL,
	model varchar(100) NULL,
	manufacturer_serial_number varchar(100) NULL,
	country_of_manufacture varchar(100) NULL,
	year_of_manufacture int4 NULL,
	measurement_range varchar(100) NULL,
	accuracy varchar(50) NULL,
	unit_of_measurement varchar(50) NULL,
	power_source varchar(20) NULL,
	battery_type varchar(50) NULL,
	calibration_date date NULL,
	calibration_due_date date NULL,
	calibration_certificate_number varchar(100) NULL,
	calibration_certificate_document varchar(255) NULL,
	carrying_case_included bool NULL,
	included_accessories varchar(500) NULL,
	supplier varchar(150) NULL,
	purchase_date date NULL,
	purchase_value numeric(14, 2) NULL,
	warranty_expiry_date date NULL,
	supporting_documents varchar(255) NULL,
	CONSTRAINT survey_instrument_asset_detail_pkey PRIMARY KEY (asset_code),
	CONSTRAINT fk_survey_instrument_asset FOREIGN KEY (asset_code) REFERENCES asset(asset_code) ON DELETE CASCADE
);


-- public.three_p_service definition

-- Drop table

-- DROP TABLE three_p_service;

CREATE TABLE three_p_service (
	three_p_service_id uuid DEFAULT gen_random_uuid() NOT NULL,
	job_card_id uuid NOT NULL,
	item varchar(255) NULL,
	serial_number varchar(100) NULL,
	issued_date timestamp NULL,
	status varchar(50) NULL,
	service_provider_name varchar(255) NULL,
	received_date timestamp NULL,
	remarks varchar(500) NULL,
	service_charge numeric(15, 2) NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	updated_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	asset_code varchar(50) NULL,
	item_code varchar(50) NULL,
	quantity numeric(12, 2) DEFAULT 1 NOT NULL,
	CONSTRAINT three_p_service_pkey PRIMARY KEY (three_p_service_id),
	CONSTRAINT fk_three_p_service_job_card FOREIGN KEY (job_card_id) REFERENCES job_card(job_card_id)
);


-- public.vehicle_asset_detail definition

-- Drop table

-- DROP TABLE vehicle_asset_detail;

CREATE TABLE vehicle_asset_detail (
	asset_code varchar(20) NOT NULL,
	vehicle_class varchar(50) NOT NULL,
	registration_number varchar(50) NOT NULL,
	year_of_manufacture int4 NOT NULL,
	colour varchar(50) NULL,
	weight_kg numeric(12, 2) NULL,
	make varchar(100) NOT NULL,
	model varchar(100) NOT NULL,
	country_of_manufacture varchar(100) NULL,
	manufacturer_serial_number varchar(100) NULL,
	chassis_number varchar(100) NOT NULL,
	body_type varchar(50) NULL,
	seating_loading_capacity varchar(50) NULL,
	engine_number varchar(100) NOT NULL,
	engine_make varchar(100) NULL,
	engine_model varchar(100) NULL,
	engine_capacity_cc int4 NULL,
	fuel_type varchar(255) NOT NULL,
	number_of_cylinders int4 NULL,
	fuel_tank_capacity_l numeric(10, 2) NULL,
	average_fuel_consumption numeric(10, 2) NULL,
	fuel_consumption_unit varchar(20) NULL,
	horsepower numeric(10, 2) NULL,
	power_kw numeric(10, 2) NULL,
	voltage numeric(10, 2) NULL,
	current_amps numeric(10, 2) NULL,
	rpm int4 NULL,
	number_of_tyres int4 NULL,
	front_tyre_size varchar(50) NULL,
	rear_tyre_size varchar(50) NULL,
	spare_tyre_size varchar(50) NULL,
	tyre_make_brand varchar(100) NULL,
	battery_make_brand varchar(100) NULL,
	battery_model varchar(100) NULL,
	battery_serial_number varchar(100) NULL,
	battery_voltage numeric(10, 2) NULL,
	battery_capacity_ah numeric(10, 2) NULL,
	number_of_batteries int4 NULL,
	supplier varchar(150) NULL,
	purchase_order_number varchar(100) NULL,
	invoice_receipt_number varchar(100) NULL,
	purchase_date date NULL,
	purchase_value numeric(14, 2) NULL,
	currency varchar(10) NULL,
	received_date date NULL,
	warranty_expiry_date date NULL,
	registration_date date NULL,
	registration_expiry_date date NULL,
	registration_document varchar(255) NULL,
	insurance_company varchar(150) NULL,
	insurance_policy_number varchar(100) NULL,
	insurance_start_date date NULL,
	insurance_expiry_date date NULL,
	revenue_licence_number varchar(100) NULL,
	revenue_licence_expiry_date date NULL,
	emission_test_expiry_date date NULL,
	insurance_licence_documents varchar(255) NULL,
	assigned_date date NULL,
	owning_department varchar(150) NULL,
	cost_centre_account_code varchar(100) NULL,
	account_description varchar(255) NULL,
	vehicle_photograph varchar(255) NULL,
	registration_certificate varchar(255) NULL,
	insurance_document varchar(255) NULL,
	revenue_licence_document varchar(255) NULL,
	purchase_invoice_document varchar(255) NULL,
	warranty_document varchar(255) NULL,
	other_supporting_documents varchar(255) NULL,
	CONSTRAINT vehicle_asset_detail_pkey PRIMARY KEY (asset_code),
	CONSTRAINT fk_vehicle_asset FOREIGN KEY (asset_code) REFERENCES asset(asset_code) ON DELETE CASCADE
);
CREATE INDEX idx_vehicle_chassis_number ON public.vehicle_asset_detail USING btree (chassis_number);
CREATE INDEX idx_vehicle_engine_number ON public.vehicle_asset_detail USING btree (engine_number);
CREATE INDEX idx_vehicle_registration_number ON public.vehicle_asset_detail USING btree (registration_number);


-- public.defect definition

-- Drop table

-- DROP TABLE defect;

CREATE TABLE defect (
	defect_id uuid DEFAULT gen_random_uuid() NOT NULL,
	job_card_id uuid NOT NULL,
	defect_description varchar(500) NOT NULL,
	CONSTRAINT defect_pkey PRIMARY KEY (defect_id),
	CONSTRAINT fk_defect_job_card FOREIGN KEY (job_card_id) REFERENCES job_card(job_card_id)
);


-- public.intra_project_issue definition

-- Drop table

-- DROP TABLE intra_project_issue;

CREATE TABLE intra_project_issue (
	intra_project_issue_id uuid NOT NULL,
	intra_project_issue_code varchar(40) NOT NULL,
	issued_by varchar(50) NOT NULL,
	issued_date timestamp NOT NULL,
	issued_project_code varchar(50) NOT NULL,
	received_by varchar(50) NULL,
	received_date timestamp NULL,
	approved_by varchar(50) NULL,
	approved_date timestamp NULL,
	is_authorized bool DEFAULT false NOT NULL,
	is_issued bool DEFAULT true NOT NULL,
	is_received bool DEFAULT false NOT NULL,
	gin_type_id int4 NULL,
	sub_contractor_id int4 NULL,
	receiver_name varchar(150) NULL,
	receiver_nic varchar(50) NULL,
	expected_return_date date NULL,
	issue_type varchar(20) DEFAULT 'GENERAL'::character varying NOT NULL,
	subcontractor_id int4 NULL,
	received_project_phase_id int4 NULL,
	job_card_id uuid NULL,
	for_asset_code varchar(20) NULL,
	CONSTRAINT intra_project_issue_code_key UNIQUE (intra_project_issue_code),
	CONSTRAINT intra_project_issue_for_asset_code_fkey FOREIGN KEY (for_asset_code) REFERENCES asset(asset_code),
	CONSTRAINT intra_project_issue_pkey PRIMARY KEY (intra_project_issue_id),
	CONSTRAINT intra_project_issue_type_check CHECK (((issue_type)::text = ANY ((ARRAY['GENERAL'::character varying, 'SUBCONTRACTOR'::character varying, 'PERSONAL'::character varying, 'LOAN'::character varying, 'JOB_CARD'::character varying])::text[]))),
	CONSTRAINT intra_project_issue_approved_by_fkey FOREIGN KEY (approved_by) REFERENCES employee(employee_code),
	CONSTRAINT intra_project_issue_gin_type_id_fkey FOREIGN KEY (gin_type_id) REFERENCES gin_type(gin_type_id),
	CONSTRAINT intra_project_issue_issued_by_fkey FOREIGN KEY (issued_by) REFERENCES employee(employee_code),
	CONSTRAINT intra_project_issue_issued_project_code_fkey FOREIGN KEY (issued_project_code) REFERENCES project(project_code),
	CONSTRAINT intra_project_issue_job_card_id_fkey FOREIGN KEY (job_card_id) REFERENCES job_card(job_card_id),
	CONSTRAINT intra_project_issue_received_by_fkey FOREIGN KEY (received_by) REFERENCES employee(employee_code),
	CONSTRAINT intra_project_issue_received_project_phase_id_fkey FOREIGN KEY (received_project_phase_id) REFERENCES project_phase(project_phase_id),
	CONSTRAINT intra_project_issue_sub_contractor_id_fkey FOREIGN KEY (sub_contractor_id) REFERENCES subcontractor(subcontractor_id),
	CONSTRAINT intra_project_issue_subcontractor_id_fkey FOREIGN KEY (subcontractor_id) REFERENCES subcontractor(subcontractor_id)
);
CREATE INDEX idx_intra_project_issue_issue_type ON public.intra_project_issue USING btree (issue_type);
CREATE INDEX idx_intra_project_issue_issued_project_code ON public.intra_project_issue USING btree (issued_project_code);
CREATE INDEX idx_intra_project_issue_job_card_id ON public.intra_project_issue USING btree (job_card_id);
CREATE INDEX idx_intra_project_issue_received_project_phase_id ON public.intra_project_issue USING btree (received_project_phase_id);
CREATE INDEX idx_intra_project_issue_subcontractor_id ON public.intra_project_issue USING btree (subcontractor_id);


-- public.intra_project_issue_item definition

-- Drop table

-- DROP TABLE intra_project_issue_item;

CREATE TABLE intra_project_issue_item (
	intra_project_issue_item_id uuid NOT NULL,
	intra_project_issue_id uuid NOT NULL,
	item_code varchar(50) NULL,
	description varchar(200) NULL,
	"size" varchar(50) NULL,
	uom_id int4 NULL,
	quantity numeric(12, 2) NOT NULL,
	remarks varchar(200) NULL,
	unit_price numeric(12, 2) NULL,
	amount numeric(14, 2) NULL,
	return_classification varchar(20) NULL,
	issue_item_type_id int4 NULL,
	length_m numeric(10, 3) NULL,
	width_m numeric(10, 3) NULL,
	asset_code varchar(50) NULL,
	CONSTRAINT intra_project_issue_item_pkey PRIMARY KEY (intra_project_issue_item_id),
	CONSTRAINT intra_project_issue_item_intra_project_issue_id_fkey FOREIGN KEY (intra_project_issue_id) REFERENCES intra_project_issue(intra_project_issue_id) ON DELETE CASCADE,
	CONSTRAINT intra_project_issue_item_issue_item_type_id_fkey FOREIGN KEY (issue_item_type_id) REFERENCES issue_item_type(issue_item_type_id),
	CONSTRAINT intra_project_issue_item_uom_id_fkey FOREIGN KEY (uom_id) REFERENCES uom(uom_id)
);
CREATE INDEX idx_intra_project_issue_item_asset_code ON public.intra_project_issue_item USING btree (asset_code) WHERE (asset_code IS NOT NULL);
CREATE INDEX idx_intra_project_issue_item_issue_id ON public.intra_project_issue_item USING btree (intra_project_issue_id);
CREATE INDEX idx_intra_project_issue_item_item_code ON public.intra_project_issue_item USING btree (item_code);


-- public.intra_project_issue_return definition

-- Drop table

-- DROP TABLE intra_project_issue_return;

CREATE TABLE intra_project_issue_return (
	intra_project_issue_return_id uuid NOT NULL,
	intra_project_issue_return_code varchar(40) NOT NULL,
	intra_project_issue_id uuid NULL,
	return_date date NOT NULL,
	returned_by varchar(50) NULL,
	remarks varchar(200) NULL,
	issued_project_code varchar(50) NOT NULL,
	return_type varchar(20) NOT NULL,
	approved_by varchar(50) NULL,
	approved_date date NULL,
	is_approved bool DEFAULT false NOT NULL,
	subcontractor_id int4 NULL,
	employee_code varchar(50) NULL,
	job_card_id uuid NULL,
	from_asset_code varchar(20) NULL,
	CONSTRAINT intra_project_issue_return_code_key UNIQUE (intra_project_issue_return_code),
	CONSTRAINT intra_project_issue_return_from_asset_code_fkey FOREIGN KEY (from_asset_code) REFERENCES asset(asset_code),
	CONSTRAINT intra_project_issue_return_pkey PRIMARY KEY (intra_project_issue_return_id),
	CONSTRAINT intra_project_issue_return_type_check CHECK (((return_type)::text = ANY ((ARRAY['GENERAL'::character varying, 'SUBCONTRACTOR'::character varying, 'PERSONAL'::character varying, 'LOAN'::character varying, 'JOB_CARD'::character varying])::text[]))),
	CONSTRAINT fk_intra_return_approved_by FOREIGN KEY (approved_by) REFERENCES employee(employee_code),
	CONSTRAINT fk_intra_return_project FOREIGN KEY (issued_project_code) REFERENCES project(project_code),
	CONSTRAINT fk_intra_return_returned_by FOREIGN KEY (returned_by) REFERENCES employee(employee_code),
	CONSTRAINT intra_project_issue_return_employee_code_fkey FOREIGN KEY (employee_code) REFERENCES employee(employee_code),
	CONSTRAINT intra_project_issue_return_intra_project_issue_id_fkey FOREIGN KEY (intra_project_issue_id) REFERENCES intra_project_issue(intra_project_issue_id),
	CONSTRAINT intra_project_issue_return_job_card_id_fkey FOREIGN KEY (job_card_id) REFERENCES job_card(job_card_id),
	CONSTRAINT intra_project_issue_return_subcontractor_id_fkey FOREIGN KEY (subcontractor_id) REFERENCES subcontractor(subcontractor_id)
);
CREATE INDEX idx_intra_project_issue_return_issue_id ON public.intra_project_issue_return USING btree (intra_project_issue_id);
CREATE INDEX idx_intra_project_issue_return_issued_project_code ON public.intra_project_issue_return USING btree (issued_project_code);
CREATE INDEX idx_intra_project_issue_return_job_card_id ON public.intra_project_issue_return USING btree (job_card_id);


-- public.intra_project_issue_return_item definition

-- Drop table

-- DROP TABLE intra_project_issue_return_item;

CREATE TABLE intra_project_issue_return_item (
	intra_project_issue_return_item_id uuid NOT NULL,
	intra_project_issue_return_id uuid NOT NULL,
	intra_project_issue_item_id uuid NOT NULL,
	item_code varchar(60) NULL,
	description varchar(250) NULL,
	uom_id int4 NULL,
	quantity numeric(14, 3) NOT NULL,
	unit_price numeric(14, 2) NULL,
	amount numeric(14, 2) NULL,
	remarks varchar(300) NULL,
	"size" varchar(50) NULL,
	length_m numeric(10, 3) NULL,
	width_m numeric(10, 3) NULL,
	asset_code varchar(50) NULL,
	CONSTRAINT intra_project_issue_return_item_pkey PRIMARY KEY (intra_project_issue_return_item_id),
	CONSTRAINT intra_project_issue_return_it_intra_project_issue_return_i_fkey FOREIGN KEY (intra_project_issue_return_id) REFERENCES intra_project_issue_return(intra_project_issue_return_id),
	CONSTRAINT intra_project_issue_return_ite_intra_project_issue_item_id_fkey FOREIGN KEY (intra_project_issue_item_id) REFERENCES intra_project_issue_item(intra_project_issue_item_id)
);
CREATE INDEX idx_intra_project_issue_return_item_asset_code ON public.intra_project_issue_return_item USING btree (asset_code) WHERE (asset_code IS NOT NULL);
CREATE INDEX idx_intra_project_issue_return_item_issue_item_id ON public.intra_project_issue_return_item USING btree (intra_project_issue_item_id);
CREATE INDEX idx_intra_project_issue_return_item_return_id ON public.intra_project_issue_return_item USING btree (intra_project_issue_return_id);


-- public.stock_batch_split definition

-- Drop table

-- DROP TABLE stock_batch_split;

CREATE TABLE stock_batch_split (
	stock_batch_split_id uuid NOT NULL,
	intra_project_issue_return_id uuid NOT NULL,
	source_stock_batch_id uuid NOT NULL,
	source_piece_count_consumed int4 NOT NULL,
	source_qty_consumed numeric(14, 3) NOT NULL,
	result_stock_batch_id uuid NULL,
	result_qty_produced numeric(14, 3) DEFAULT 0 NOT NULL,
	wastage_qty numeric(14, 3) DEFAULT 0 NOT NULL,
	CONSTRAINT stock_batch_split_pkey PRIMARY KEY (stock_batch_split_id),
	CONSTRAINT stock_batch_split_result_qty_produced_check CHECK ((result_qty_produced >= (0)::numeric)),
	CONSTRAINT stock_batch_split_source_piece_count_consumed_check CHECK ((source_piece_count_consumed > 0)),
	CONSTRAINT stock_batch_split_source_qty_consumed_check CHECK ((source_qty_consumed > (0)::numeric)),
	CONSTRAINT stock_batch_split_wastage_qty_check CHECK ((wastage_qty >= (0)::numeric)),
	CONSTRAINT stock_batch_split_intra_project_issue_return_id_fkey FOREIGN KEY (intra_project_issue_return_id) REFERENCES intra_project_issue_return(intra_project_issue_return_id) ON DELETE RESTRICT,
	CONSTRAINT stock_batch_split_result_stock_batch_id_fkey FOREIGN KEY (result_stock_batch_id) REFERENCES stock_batch(stock_batch_id) ON DELETE RESTRICT,
	CONSTRAINT stock_batch_split_source_stock_batch_id_fkey FOREIGN KEY (source_stock_batch_id) REFERENCES stock_batch(stock_batch_id) ON DELETE RESTRICT
);
CREATE INDEX idx_stock_batch_split_return ON public.stock_batch_split USING btree (intra_project_issue_return_id);
CREATE INDEX idx_stock_batch_split_source_batch ON public.stock_batch_split USING btree (source_stock_batch_id);

-- These CREATE FUNCTION ... LANGUAGE c statements (pg_dump output of the pgcrypto
-- extension's internals) need superuser and fail with "permission denied for language c"
-- for a normal role. Installing the extension provides the same functions
-- (gen_random_uuid, pgp_sym_encrypt, digest, crypt, ...) without needing that privilege.
CREATE EXTENSION IF NOT EXISTS pgcrypto;
