package com.connectabroad.repository;

import com.connectabroad.entity.Job;
import com.connectabroad.entity.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {
    Page<Job> findByPostedByIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Page<Job> findByPostedByIdAndStatusOrderByCreatedAtDesc(Long userId, JobStatus status, Pageable pageable);

    @Query("SELECT j FROM Job j WHERE " +
           "(:keyword IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(j.companyName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(j.city) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(j.country) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY j.createdAt DESC")
    Page<Job> findAdminJobs(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT CAST(j.createdAt AS date) as date, COUNT(j) as count " +
           "FROM Job j WHERE j.createdAt >= :startDate " +
           "GROUP BY CAST(j.createdAt AS date) ORDER BY CAST(j.createdAt AS date)")
    List<Object[]> countNewJobsPerDay(@Param("startDate") LocalDateTime startDate);
}
