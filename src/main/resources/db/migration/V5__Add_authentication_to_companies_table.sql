-- Add authentication fields to companies table
ALTER TABLE companies 
ADD COLUMN IF NOT EXISTS email VARCHAR(255) UNIQUE,
ADD COLUMN IF NOT EXISTS password VARCHAR(255) NOT NULL DEFAULT '';

-- Add indexes for new authentication fields
CREATE INDEX IF NOT EXISTS idx_companies_email ON companies(email);

-- Add constraint to ensure email is not null for new records
ALTER TABLE companies 
ADD CONSTRAINT chk_companies_email_not_null 
CHECK (email IS NOT NULL AND email != '');

-- Add constraint to ensure password is not null for new records
ALTER TABLE companies 
ADD CONSTRAINT chk_companies_password_not_null 
CHECK (password IS NOT NULL AND password != '');

-- Create trigger to automatically update updated_at column for companies table
-- (This might already exist, but we'll ensure it's there)
DROP TRIGGER IF EXISTS update_companies_updated_at ON companies;
CREATE TRIGGER update_companies_updated_at
    BEFORE UPDATE ON companies
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();