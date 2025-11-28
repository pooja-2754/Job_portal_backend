-- Remove owner_id column from companies table
-- First, drop the foreign key constraint if it exists
ALTER TABLE companies DROP CONSTRAINT IF EXISTS fk_companies_owner;

-- Remove the owner_id column
ALTER TABLE companies DROP COLUMN IF EXISTS owner_id;

-- Remove the index for owner_id if it exists
DROP INDEX IF EXISTS idx_companies_owner_id;

-- Remove the trigger that automatically assigns company owner since companies are now self-owned
DROP TRIGGER IF EXISTS assign_company_owner_trigger ON companies;

-- Remove the function that assigns company owner
DROP FUNCTION IF EXISTS assign_company_owner();

-- Add comment to clarify that companies are now self-owned entities
COMMENT ON TABLE companies IS 'Companies table - companies are now self-contained entities with their own authentication and ownership';