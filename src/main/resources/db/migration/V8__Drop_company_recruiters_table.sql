-- Drop the company_recruiters table as it's no longer needed
-- Companies are now self-contained entities and don't need separate recruiter permissions

-- Drop the table and all its dependencies
DROP TABLE IF EXISTS company_recruiters CASCADE;

-- Remove any triggers related to company_recruiters
DROP TRIGGER IF EXISTS update_company_recruiters_updated_at ON company_recruiters;

-- Add a comment explaining the removal
COMMENT ON TABLE companies IS 'Companies table - company_recruiters table removed as companies are now self-contained entities';