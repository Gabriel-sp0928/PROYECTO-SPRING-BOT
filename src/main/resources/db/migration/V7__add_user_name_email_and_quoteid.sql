-- Add name and email to users and add quote_id to sales to match entities
ALTER TABLE users ADD COLUMN IF NOT EXISTS email VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS name VARCHAR(255);

-- Backfill sensible defaults
UPDATE users SET email = username || '@example.local' WHERE email IS NULL;
UPDATE users SET name = username WHERE name IS NULL;

-- Set NOT NULL (H2 supports ALTER COLUMN SET NOT NULL)
ALTER TABLE users ALTER COLUMN email SET NOT NULL;
ALTER TABLE users ALTER COLUMN name SET NOT NULL;

-- Add unique index on email if not exists
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- Add quote_id to sales so entity can reference quotes
ALTER TABLE sales ADD COLUMN IF NOT EXISTS quote_id BIGINT;
ALTER TABLE sales ADD CONSTRAINT IF NOT EXISTS fk_sale_quote FOREIGN KEY (quote_id) REFERENCES quotes(id) ON DELETE SET NULL;

-- Backfill nothing for quote_id (can't map reliably)
