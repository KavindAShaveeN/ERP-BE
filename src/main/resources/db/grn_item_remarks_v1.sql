-- Item-wise remarks on GRN lines (separate from Part No / size).
ALTER TABLE grn_item ADD COLUMN IF NOT EXISTS remarks varchar(500);
