package com.pooja.jobportal.service;

import com.pooja.jobportal.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service for logging administrative actions for security and compliance
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    /**
     * Log company verification action
     */
    public void logCompanyVerification(User admin, Long companyId, String action, String details) {
        String auditMessage = String.format(
            "ADMIN ACTION - User: %s (ID: %d) performed '%s' on Company ID: %d at %s. Details: %s",
            admin.getEmail(),
            admin.getId(),
            action,
            companyId,
            LocalDateTime.now(),
            details
        );
        
        log.info(auditMessage);
        // In a real implementation, you would save this to a dedicated audit_log table
        // auditLogRepository.save(createAuditLogEntry(admin, companyId, action, details));
    }

    /**
     * Log company rejection action
     */
    public void logCompanyRejection(User admin, Long companyId, String reason) {
        logCompanyVerification(admin, companyId, "REJECT", reason);
    }

    /**
     * Log company approval action
     */
    public void logCompanyApproval(User admin, Long companyId) {
        logCompanyVerification(admin, companyId, "APPROVE", "Company verification approved");
    }

    /**
     * Log role change action (for future implementation)
     */
    public void logRoleChange(User admin, Long targetUserId, String oldRole, String newRole) {
        String auditMessage = String.format(
            "ADMIN ACTION - User: %s (ID: %d) changed role for User ID: %d from '%s' to '%s' at %s",
            admin.getEmail(),
            admin.getId(),
            targetUserId,
            oldRole,
            newRole,
            LocalDateTime.now()
        );
        
        log.info(auditMessage);
    }

    /**
     * Log unauthorized access attempts
     */
    public void logUnauthorizedAccess(String email, String resource, String action) {
        String auditMessage = String.format(
            "SECURITY VIOLATION - User: %s attempted unauthorized '%s' on resource: %s at %s",
            email,
            action,
            resource,
            LocalDateTime.now()
        );
        
        log.warn(auditMessage);
        // In production, you might want to trigger alerts for repeated violations
    }
}