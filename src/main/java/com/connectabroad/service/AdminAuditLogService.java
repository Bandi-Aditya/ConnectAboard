package com.connectabroad.service;

import com.connectabroad.dto.admin.AuditLogResponse;
import com.connectabroad.dto.response.PageResponse;
import com.connectabroad.entity.AdminAuditLog;
import com.connectabroad.entity.User;
import com.connectabroad.repository.AdminAuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AdminAuditLogService {

    private final AdminAuditLogRepository auditLogRepository;

    public AdminAuditLogService(AdminAuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void logAction(User admin, String action, String targetType, Long targetId, String description) {
        AdminAuditLog log = new AdminAuditLog(admin, action, targetType, targetId, description);
        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> getAuditLogs(String keyword, Pageable pageable) {
        Page<AdminAuditLog> page = auditLogRepository.searchAuditLogs(keyword, pageable);
        return PageResponse.from(page.map(this::mapToAuditLogResponse));
    }

    private AuditLogResponse mapToAuditLogResponse(AdminAuditLog log) {
        return new AuditLogResponse(
                log.getId(),
                log.getAdmin() != null ? log.getAdmin().getName() : "System",
                log.getAdmin() != null ? log.getAdmin().getEmail() : "system@connectabroad.com",
                log.getAction(),
                log.getTargetType(),
                log.getTargetId(),
                log.getDescription(),
                log.getCreatedAt()
        );
    }
}
