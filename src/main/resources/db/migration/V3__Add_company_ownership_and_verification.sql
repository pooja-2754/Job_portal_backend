-- Add new columns to companies table for ownership and verification
ALTER TABLE companies 
ADD COLUMN IF NOT EXISTS verification_status VARCHAR(20) DEFAULT 'PENDING' CHECK (verification_status IN ('PENDING', 'VERIFIED', 'REJECTED')),
ADD COLUMN IF NOT EXISTS verified_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS owner_id BIGINT;

-- Create company_recruiters table for managing recruiter permissions
CREATE TABLE IF NOT EXISTS company_recruiters (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,
    recruiter_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'RECRUITER' CHECK (role IN ('RECRUITER', 'ADMIN')),
    permission VARCHAR(20) NOT NULL DEFAULT 'FULL_ACCESS' CHECK (permission IN ('FULL_ACCESS', 'POST_ONLY', 'VIEW_ONLY')),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(company_id, recruiter_id)
);

-- Add foreign key constraints
ALTER TABLE companies 
ADD CONSTRAINT fk_companies_owner 
FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE SET NULL;

ALTER TABLE company_recruiters 
ADD CONSTRAINT fk_company_recruiters_company 
FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
ADD CONSTRAINT fk_company_recruiters_recruiter 
FOREIGN KEY (recruiter_id) REFERENCES users(id) ON DELETE CASCADE;

-- Create indexes for new columns and tables
CREATE INDEX IF NOT EXISTS idx_companies_verification_status ON companies(verification_status);
CREATE INDEX IF NOT EXISTS idx_companies_owner_id ON companies(owner_id);
CREATE INDEX IF NOT EXISTS idx_company_recruiters_company_id ON company_recruiters(company_id);
CREATE INDEX IF NOT EXISTS idx_company_recruiters_recruiter_id ON company_recruiters(recruiter_id);
CREATE INDEX IF NOT EXISTS idx_company_recruiters_is_active ON company_recruiters(is_active);

-- Create trigger to automatically update updated_at column for company_recruiters table
DROP TRIGGER IF EXISTS update_company_recruiters_updated_at ON company_recruiters;
CREATE TRIGGER update_company_recruiters_updated_at
    BEFORE UPDATE ON company_recruiters
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Function to automatically assign ownership when a company is created
CREATE OR REPLACE FUNCTION assign_company_owner() RETURNS TRIGGER AS $$
BEGIN
    -- If no owner is specified, try to find the first recruiter associated with the company
    IF NEW.owner_id IS NULL THEN
        SELECT INTO NEW.owner_id cr.recruiter_id
        FROM company_recruiters cr
        WHERE cr.company_id = NEW.id AND cr.is_active = TRUE
        ORDER BY cr.created_at ASC
        LIMIT 1;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger to automatically assign company owner
DROP TRIGGER IF EXISTS assign_company_owner_trigger ON companies;
CREATE TRIGGER assign_company_owner_trigger
    BEFORE INSERT OR UPDATE ON companies
    FOR EACH ROW
    WHEN (NEW.owner_id IS NULL OR (OLD.owner_id IS NOT NULL AND NEW.owner_id IS NULL))
    EXECUTE FUNCTION assign_company_owner();