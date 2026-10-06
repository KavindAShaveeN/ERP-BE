-- mr.mr_code, supplier_payment.payment_code, and grn.grn_code had no UNIQUE constraint at
-- all — under concurrent submission (two tabs, two users, a slow network retry) two rows
-- could silently get the same document code with no error, since nothing in the DB rejected
-- it. On the dev DB, this had already happened once: two distinct GRN rows both held
-- 'SUP-PBP-0001' (grn_id d5ef32b6... [approved] and 0907176a... [not yet approved]). The
-- unapproved row was renumbered to 'SUP-PBP-0003' (the next free sequence for project PBP)
-- before adding the constraint below.
ALTER TABLE mr ADD CONSTRAINT mr_mr_code_key UNIQUE (mr_code);
ALTER TABLE supplier_payment ADD CONSTRAINT supplier_payment_payment_code_key UNIQUE (payment_code);
ALTER TABLE grn ADD CONSTRAINT grn_grn_code_key UNIQUE (grn_code);
