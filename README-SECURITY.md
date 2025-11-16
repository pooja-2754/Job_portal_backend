# Security Setup Guide

## Environment Variables Setup

This project uses environment variables to manage sensitive configuration. Follow these steps to set up your development environment securely.

### 1. Create Environment File

Copy the sample environment file:
```bash
cp .env.sample .env
```

### 2. Configure Your Environment Variables

Edit the `.env` file with your actual credentials:

```bash
# Database Configuration
DB_URL=jdbc:postgresql://your-db-host:5432/your-database-name
DB_USERNAME=your-db-username
DB_PASSWORD=your-db-password

# JWT Configuration
JWT_SECRET=your-jwt-secret-key-here
JWT_EXPIRATION=86400000

# Application Configuration
SPRING_PROFILES_ACTIVE=dev
SERVER_PORT=8080
```

### 3. Load Environment Variables

#### Option A: Using IDE (IntelliJ/Eclipse)
1. Go to Run/Debug Configuration
2. Add environment variables from your `.env` file

#### Option B: Using Command Line
```bash
# For Windows
set DB_URL=jdbc:postgresql://your-db-host:5432/your-database-name
set DB_USERNAME=your-db-username
set DB_PASSWORD=your-db-password
set JWT_SECRET=your-jwt-secret-key-here

# For Linux/Mac
export DB_URL=jdbc:postgresql://your-db-host:5432/your-database-name
export DB_USERNAME=your-db-username
export DB_PASSWORD=your-db-password
export JWT_SECRET=your-jwt-secret-key-here
```

#### Option C: Using Maven
```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-DDB_URL=your-url -DDB_USERNAME=your-user -DDB_PASSWORD=your-password"
```

### 4. Production Deployment

For production deployments:
1. Never commit `.env` files to version control
2. Use your cloud provider's secret management service
3. Set environment variables directly in your deployment configuration
4. Use strong, unique passwords and JWT secrets
5. Regularly rotate your secrets

### Security Best Practices

1. **Never commit sensitive data**: The `.gitignore` file is configured to prevent accidental commits of sensitive files
2. **Use strong secrets**: Generate long, random strings for JWT secrets
3. **Environment-specific configs**: Use different configurations for development, staging, and production
4. **Regular audits**: Periodically review and update your secrets
5. **Limit access**: Only authorized personnel should have access to production credentials

### Git Security Checklist

- [ ] `.env` file is in `.gitignore`
- [ ] No hardcoded credentials in source code
- [ ] Database URLs use environment variables
- [ ] JWT secrets are not committed
- [ ] API keys are stored securely
- [ ] Sensitive configuration files are ignored

### Generating Secure Secrets

Use these commands to generate secure secrets:

```bash
# Generate JWT Secret (Linux/Mac)
openssl rand -base64 64

# Generate JWT Secret (Windows)
powershell -Command "[System.Convert]::ToBase64String((1..64 | ForEach-Object {Get-Random -Minimum 0 -Maximum 256}))"
```

### Troubleshooting

If you encounter connection issues:
1. Verify your `.env` file is properly configured
2. Check that environment variables are loaded correctly
3. Ensure database is accessible from your network
4. Validate database credentials are correct