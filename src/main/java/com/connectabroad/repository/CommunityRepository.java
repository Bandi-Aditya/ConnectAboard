package com.connectabroad.repository;

import com.connectabroad.entity.Community;
import com.connectabroad.entity.CommunityStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CommunityRepository extends JpaRepository<Community, Long> {

    Page<Community> findByStatusOrderByCreatedAtDesc(CommunityStatus status, Pageable pageable);

    @Query("SELECT c FROM Community c WHERE " +
           "(:keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.category) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.location) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:status IS NULL OR c.status = :status) " +
           "ORDER BY c.createdAt DESC")
    Page<Community> findAdminCommunities(@Param("keyword") String keyword, @Param("status") CommunityStatus status, Pageable pageable);

    @Query("SELECT CAST(c.createdAt AS date) as date, COUNT(c) as count " +
           "FROM Community c WHERE c.createdAt >= :startDate " +
           "GROUP BY CAST(c.createdAt AS date) ORDER BY CAST(c.createdAt AS date)")
    List<Object[]> countNewCommunitiesPerDay(@Param("startDate") LocalDateTime startDate);
}
