-- Add email and name columns to users and populate them (H2-compatible)
ALTER TABLE users ADD COLUMN IF NOT EXISTS email VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS name VARCHAR(255);

-- Populate email and name for seeded users using MERGE (idempotent)
-- Populate emails/names for users via SQL removed. User creation and secure password hashing
-- are handled by the Java `DataInitializer` to avoid plaintext passwords in migrations.

-- Ensure columns are NOT NULL and email is unique
ALTER TABLE users ALTER COLUMN email SET NOT NULL;
ALTER TABLE users ALTER COLUMN name SET NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_email ON users(email);
