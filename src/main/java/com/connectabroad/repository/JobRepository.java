package com.connectabroad.repository;

import com.connectabroad.entity.Job;
import com.connectabroad.entity.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {
    Page<Job> findByPostedByIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Page<Job> findByPostedByIdAndStatusOrderByCreatedAtDesc(Long userId, JobStatus status, Pageable pageable);
}
