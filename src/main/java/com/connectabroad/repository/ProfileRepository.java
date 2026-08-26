package com.connectabroad.repository;

import com.connectabroad.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long>, JpaSpecificationExecutor<Profile> {

    Optional<Profile> findByUserId(Long userId);

    Optional<Profile> findByUserEmail(String email);

    boolean existsByUserId(Long userId);

    boolean existsByUserEmail(String email);
}
