package com.connectabroad.specification;

import com.connectabroad.entity.EmploymentType;
import com.connectabroad.entity.Job;
import com.connectabroad.entity.JobStatus;
import com.connectabroad.entity.WorkMode;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class JobSpecification {

    public static Specification<Job> filterJobs(
            String keyword,
            String country,
            String city,
            EmploymentType employmentType,
            WorkMode workMode,
            String experience,
            String skill,
            JobStatus status) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            if (country != null && !country.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("country")), country.trim().toLowerCase()));
            }

            if (city != null && !city.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("city")), "%" + city.trim().toLowerCase() + "%"));
            }

            if (employmentType != null) {
                predicates.add(criteriaBuilder.equal(root.get("employmentType"), employmentType));
            }

            if (workMode != null) {
                predicates.add(criteriaBuilder.equal(root.get("workMode"), workMode));
            }

            if (experience != null && !experience.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("experienceRequired")), "%" + experience.trim().toLowerCase() + "%"));
            }

            if (skill != null && !skill.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("requiredSkills")), "%" + skill.trim().toLowerCase() + "%"));
            }

            if (keyword != null && !keyword.trim().isEmpty()) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate titleMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), pattern);
                Predicate companyMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("companyName")), pattern);
                Predicate descMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern);
                Predicate skillMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("requiredSkills")), pattern);

                predicates.add(criteriaBuilder.or(titleMatch, companyMatch, descMatch, skillMatch));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
