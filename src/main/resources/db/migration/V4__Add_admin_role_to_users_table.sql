-- Add ADMIN role to the users table role constraint
-- First drop the existing constraint
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;

-- Add the updated constraint that includes ADMIN role
ALTER TABLE users 
ADD CONSTRAINT users_role_check 
CHECK (role IN ('JOB_SEEKER', 'RECRUITER', 'ADMIN'));

-- Create a default admin user (optional - can be removed if not needed)
-- This creates an admin user with email admin@jobportal.com and password admin123
-- In production, this should be created through a secure process
INSERT INTO users (name, email, password, role, created_at, updated_at) 
VALUES (
    'System Administrator', 
    'admin@jobportal.com', 
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', -- password: admin123
    'ADMIN', 
    CURRENT_TIMESTAMP, 
    CURRENT_TIMESTAMP
) ON CONFLICT (email) DO NOTHING;