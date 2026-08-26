package com.connectabroad.repository;

import com.connectabroad.entity.College;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CollegeRepository extends JpaRepository<College, Long> {

    Optional<College> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
