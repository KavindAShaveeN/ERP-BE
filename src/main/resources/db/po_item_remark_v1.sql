-- Item-wise remark on purchase order lines.
ALTER TABLE po_item ADD COLUMN IF NOT EXISTS remark varchar(500);
