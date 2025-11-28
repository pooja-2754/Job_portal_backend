-- Update users table to remove RECRUITER role
-- First drop the existing role constraint
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;

-- Add the updated constraint that excludes RECRUITER role
ALTER TABLE users 
ADD CONSTRAINT users_role_check 
CHECK (role IN ('JOB_SEEKER', 'ADMIN'));

-- Remove all recruiter users after migration to companies
-- This will be done in the data migration script, but we'll prepare for it here
-- Note: This DELETE will be executed in the data migration script after companies are created

-- Add comment to clarify the new role structure
COMMENT ON COLUMN users.role IS 'User role: JOB_SEEKER or ADMIN. RECRUITER role removed - recruiters are now companies';