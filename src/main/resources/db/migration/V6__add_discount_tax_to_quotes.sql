-- Add discount and tax columns to quotes to match entity
ALTER TABLE quotes ADD COLUMN IF NOT EXISTS discount DECIMAL(19,2) DEFAULT 0;
ALTER TABLE quotes ADD COLUMN IF NOT EXISTS tax DECIMAL(19,2) DEFAULT 0;

-- Backfill existing rows with zero values (idempotent)
UPDATE quotes SET discount = 0 WHERE discount IS NULL;
UPDATE quotes SET tax = 0 WHERE tax IS NULL;
