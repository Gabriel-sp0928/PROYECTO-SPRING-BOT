-- Add profile columns to users and ensure professional users exist
ALTER TABLE users ADD COLUMN IF NOT EXISTS document_number VARCHAR(100);
ALTER TABLE users ADD COLUMN IF NOT EXISTS address VARCHAR(500);
ALTER TABLE users ADD COLUMN IF NOT EXISTS payment_method VARCHAR(100);

-- This migration only ensures the schema; user creation is handled by Java initializer
-- to allow passwords to be encoded with the application's PasswordEncoder.
