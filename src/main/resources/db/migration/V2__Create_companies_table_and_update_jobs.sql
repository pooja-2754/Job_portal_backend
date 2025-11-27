-- Create Companies table
CREATE TABLE IF NOT EXISTS companies (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    logo_url VARCHAR(500),
    website VARCHAR(500),
    description TEXT,
    industry VARCHAR(100),
    company_size VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for Companies table
CREATE INDEX IF NOT EXISTS idx_companies_name ON companies(name);
CREATE INDEX IF NOT EXISTS idx_companies_industry ON companies(industry);

-- Add new columns to jobs table for improved structure
ALTER TABLE jobs 
ADD COLUMN IF NOT EXISTS company_id BIGINT,
ADD COLUMN IF NOT EXISTS slug VARCHAR(255) UNIQUE,
ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'PUBLISHED' CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')),
ADD COLUMN IF NOT EXISTS workplace_type VARCHAR(20) DEFAULT 'ONSITE' CHECK (workplace_type IN ('ONSITE', 'REMOTE', 'HYBRID')),
ADD COLUMN IF NOT EXISTS experience_level VARCHAR(20) CHECK (experience_level IN ('JUNIOR', 'MID', 'SENIOR', 'LEAD')),
ADD COLUMN IF NOT EXISTS salary_min DECIMAL(12,2),
ADD COLUMN IF NOT EXISTS salary_max DECIMAL(12,2),
ADD COLUMN IF NOT EXISTS salary_currency VARCHAR(10) DEFAULT 'USD',
ADD COLUMN IF NOT EXISTS salary_period VARCHAR(20) DEFAULT 'YEARLY' CHECK (salary_period IN ('HOURLY', 'MONTHLY', 'YEARLY')),
ADD COLUMN IF NOT EXISTS salary_is_negotiable BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS city VARCHAR(100),
ADD COLUMN IF NOT EXISTS state VARCHAR(100),
ADD COLUMN IF NOT EXISTS country VARCHAR(100),
ADD COLUMN IF NOT EXISTS zip_code VARCHAR(20),
ADD COLUMN IF NOT EXISTS latitude DECIMAL(10,8),
ADD COLUMN IF NOT EXISTS longitude DECIMAL(11,8),
ADD COLUMN IF NOT EXISTS skills TEXT[],
ADD COLUMN IF NOT EXISTS view_count BIGINT DEFAULT 0,
ADD COLUMN IF NOT EXISTS apply_url VARCHAR(500),
ADD COLUMN IF NOT EXISTS responsibilities TEXT,
ADD COLUMN IF NOT EXISTS benefits TEXT;

-- Add foreign key constraint for company_id
ALTER TABLE jobs 
ADD CONSTRAINT fk_jobs_company 
FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE SET NULL;

-- Create indexes for new job columns
CREATE INDEX IF NOT EXISTS idx_jobs_company_id ON jobs(company_id);
CREATE INDEX IF NOT EXISTS idx_jobs_slug ON jobs(slug);
CREATE INDEX IF NOT EXISTS idx_jobs_status ON jobs(status);
CREATE INDEX IF NOT EXISTS idx_jobs_workplace_type ON jobs(workplace_type);
CREATE INDEX IF NOT EXISTS idx_jobs_experience_level ON jobs(experience_level);
CREATE INDEX IF NOT EXISTS idx_jobs_salary_min ON jobs(salary_min);
CREATE INDEX IF NOT EXISTS idx_jobs_salary_max ON jobs(salary_max);
CREATE INDEX IF NOT EXISTS idx_jobs_city ON jobs(city);
CREATE INDEX IF NOT EXISTS idx_jobs_state ON jobs(state);
CREATE INDEX IF NOT EXISTS idx_jobs_country ON jobs(country);
CREATE INDEX IF NOT EXISTS idx_jobs_skills ON jobs USING GIN(skills);

-- Create trigger to automatically update updated_at column for companies table
DROP TRIGGER IF EXISTS update_companies_updated_at ON companies;
CREATE TRIGGER update_companies_updated_at
    BEFORE UPDATE ON companies
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Function to generate unique slug from job title and company name
CREATE OR REPLACE FUNCTION generate_job_slug() RETURNS TRIGGER AS $$
DECLARE
    base_slug TEXT;
    slug TEXT;
    counter INTEGER := 1;
BEGIN
    base_slug := lower(regexp_replace(NEW.title, '[^a-zA-Z0-9\s]', '', 'g'));
    base_slug := regexp_replace(base_slug, '\s+', '-', 'g');
    
    IF NEW.company_id IS NOT NULL THEN
        SELECT INTO base_slug base_slug || '-' || lower(regexp_replace(c.name, '[^a-zA-Z0-9\s]', '', 'g'))
        FROM companies c WHERE c.id = NEW.company_id;
        base_slug := regexp_replace(base_slug, '\s+', '-', 'g');
    END IF;
    
    slug := base_slug;
    
    WHILE EXISTS (SELECT 1 FROM jobs WHERE slug = slug AND id != NEW.id) LOOP
        slug := base_slug || '-' || counter;
        counter := counter + 1;
    END LOOP;
    
    NEW.slug := slug;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger to automatically generate slug
DROP TRIGGER IF EXISTS generate_job_slug_trigger ON jobs;
CREATE TRIGGER generate_job_slug_trigger
    BEFORE INSERT OR UPDATE ON jobs
    FOR EACH ROW
    WHEN (NEW.slug IS NULL OR NEW.slug != OLD.slug)
    EXECUTE FUNCTION generate_job_slug();