-- Add primary_resume_id column to users table
ALTER TABLE users ADD COLUMN primary_resume_id BIGINT;

-- Add foreign key constraint linking to resumes table
ALTER TABLE users ADD CONSTRAINT fk_users_primary_resume 
FOREIGN KEY (primary_resume_id) REFERENCES resumes(id);

-- Create index for better performance on primary resume queries
CREATE INDEX idx_users_primary_resume_id ON users(primary_resume_id);