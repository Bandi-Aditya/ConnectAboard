package com.connectabroad.repository;

import com.connectabroad.entity.Role;
import com.connectabroad.entity.User;
import com.connectabroad.entity.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);

    long countByStatus(UserStatus status);

    long countByCreatedAtAfter(LocalDateTime dateTime);

    @Query("SELECT u FROM User u WHERE " +
           "(:keyword IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:status IS NULL OR u.status = :status)")
    Page<User> findAdminUsers(@Param("keyword") String keyword,
                              @Param("role") Role role,
                              @Param("status") UserStatus status,
                              Pageable pageable);

    @Query("SELECT CAST(u.createdAt AS date) as date, COUNT(u) as count " +
           "FROM User u WHERE u.createdAt >= :startDate " +
           "GROUP BY CAST(u.createdAt AS date) ORDER BY CAST(u.createdAt AS date)")
    List<Object[]> countNewUsersPerDay(@Param("startDate") LocalDateTime startDate);
}
