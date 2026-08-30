package com.connectabroad.repository;

import com.connectabroad.entity.AdminAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminAuditLogRepository extends JpaRepository<AdminAuditLog, Long> {

    Page<AdminAuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT a FROM AdminAuditLog a WHERE " +
           "(:keyword IS NULL OR LOWER(a.action) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(a.admin.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(a.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY a.createdAt DESC")
    Page<AdminAuditLog> searchAuditLogs(@Param("keyword") String keyword, Pageable pageable);
}
