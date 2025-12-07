CREATE TABLE resumes (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    file_url VARCHAR(1000) NOT NULL,
    preview_url VARCHAR(1000),
    cloudinary_id VARCHAR(255) NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_resumes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Add indexes for frequently queried columns
CREATE INDEX idx_resumes_cloudinary_id ON resumes(cloudinary_id);
CREATE INDEX idx_resumes_name ON resumes(name);
CREATE INDEX idx_resumes_user_id ON resumes(user_id);