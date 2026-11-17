-- Records who created a GRN / stock adjustment (employee code of the signed-in user at creation time),
-- so the printed form can show "Created by" next to "Approved by". Re-runnable.
-- Existing rows stay NULL (creator was never recorded) and print a blank name line.

ALTER TABLE grn              ADD COLUMN IF NOT EXISTS created_by varchar(50) NULL;
ALTER TABLE stock_adjustment ADD COLUMN IF NOT EXISTS created_by varchar(50) NULL;
