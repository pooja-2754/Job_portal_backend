-- Data migration script to convert existing recruiters to companies
-- This script should be run after all schema changes are complete

-- Step 1: Create companies from existing recruiters
-- We'll use a temporary sequence to handle potential conflicts
INSERT INTO companies (name, email, password, logo_url, website, description, industry, company_size, verification_status, created_at, updated_at)
SELECT 
    COALESCE(c.name, u.name || ' Company') as name,
    u.email,
    u.password,
    c.logo_url,
    c.website,
    c.description,
    c.industry,
    c.company_size,
    COALESCE(c.verification_status, 'PENDING') as verification_status,
    u.created_at,
    u.updated_at
FROM users u
LEFT JOIN companies c ON u.id = c.owner_id
WHERE u.role = 'RECRUITER'
ON CONFLICT (email) DO NOTHING;

-- Step 2: Update job ownership from recruiters to companies
-- First, create a temporary mapping table to track recruiter-to-company conversions
CREATE TEMP TABLE recruiter_company_mapping AS
SELECT u.id as recruiter_id, comp.id as company_id
FROM users u
JOIN companies comp ON u.email = comp.email
WHERE u.role = 'RECRUITER';

-- Update jobs table to reference companies instead of recruiters
UPDATE jobs 
SET company_id = rcm.company_id
FROM recruiter_company_mapping rcm
WHERE jobs.recruiter_id = rcm.recruiter_id;

-- Step 3: Handle any jobs that might have null company_id after migration
-- Set a default company or mark them as inactive if no mapping found
UPDATE jobs 
SET company_id = (
    SELECT MIN(id) FROM companies 
    WHERE email NOT IN (SELECT email FROM users WHERE role = 'RECRUITER')
), 
is_active = false
WHERE company_id IS NULL AND recruiter_id IN (SELECT id FROM users WHERE role = 'RECRUITER');

-- Step 4: Clean up - remove recruiter users
-- Only remove after successful migration
DELETE FROM users WHERE role = 'RECRUITER';

-- Step 5: Drop the temporary mapping table
DROP TABLE IF EXISTS recruiter_company_mapping;

-- Step 6: Update sequences if needed
-- Reset the company ID sequence to avoid conflicts
SELECT setval('companies_id_seq', (SELECT MAX(id) FROM companies));

-- Step 7: Add verification for migrated companies
-- Set verification status based on existing data or default to PENDING
UPDATE companies 
SET verification_status = CASE 
    WHEN verification_status = 'VERIFIED' THEN 'VERIFIED'
    WHEN verification_status = 'REJECTED' THEN 'REJECTED'
    ELSE 'PENDING'
END,
verified_at = CASE 
    WHEN verification_status = 'VERIFIED' THEN COALESCE(verified_at, CURRENT_TIMESTAMP)
    ELSE NULL
END
WHERE email IN (SELECT email FROM users WHERE role = 'RECRUITER');

-- Add migration log entry for tracking
INSERT INTO migration_log (migration_name, executed_at, status)
VALUES ('Migrate_recruiters_to_companies', CURRENT_TIMESTAMP, 'COMPLETED')
ON CONFLICT (migration_name) DO NOTHING;

-- Create migration_log table if it doesn't exist
CREATE TABLE IF NOT EXISTS migration_log (
    migration_name VARCHAR(255) PRIMARY KEY,
    executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) NOT NULL
);

-- Log the migration
INSERT INTO migration_log (migration_name, executed_at, status)
VALUES ('Migrate_recruiters_to_companies', CURRENT_TIMESTAMP, 'COMPLETED')
ON CONFLICT (migration_name) DO NOTHING;