-- Remove recruiter_id column from jobs table
-- First, we need to handle the foreign key constraint if it exists
ALTER TABLE jobs DROP CONSTRAINT IF EXISTS fk_jobs_recruiter;

-- Remove the recruiter_id column
ALTER TABLE jobs DROP COLUMN IF EXISTS recruiter_id;

-- Ensure company_id is NOT NULL since it will be the only owner
ALTER TABLE jobs ALTER COLUMN company_id SET NOT NULL;

-- Update indexes - remove any recruiter-related indexes
DROP INDEX IF EXISTS idx_jobs_recruiter_id;

-- Create a new index for company_id if it doesn't exist
CREATE INDEX IF NOT EXISTS idx_jobs_company_id_not_null ON jobs(company_id) WHERE company_id IS NOT NULL;

-- Add comment to clarify the relationship
COMMENT ON COLUMN jobs.company_id IS 'Foreign key to companies table - companies are now the primary owners of jobs';