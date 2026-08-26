package com.connectabroad.specification;

import com.connectabroad.entity.College;
import com.connectabroad.entity.Profile;
import com.connectabroad.entity.User;
import com.connectabroad.entity.UserType;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ProfileSpecification {

    public static Specification<Profile> excludeUserId(Long currentUserId) {
        return (root, query, cb) -> {
            if (currentUserId == null) return cb.conjunction();
            return cb.notEqual(root.get("user").get("id"), currentUserId);
        };
    }

    public static Specification<Profile> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(keyword)) return cb.conjunction();
            String pattern = "%" + keyword.trim().toLowerCase() + "%";

            Join<Profile, User> userJoin = root.join("user", JoinType.LEFT);
            Join<Profile, College> collegeJoin = root.join("college", JoinType.LEFT);

            Predicate nameMatch = cb.like(cb.lower(userJoin.get("name")), pattern);
            Predicate collegeMatch = cb.like(cb.lower(collegeJoin.get("name")), pattern);
            Predicate professionMatch = cb.like(cb.lower(root.get("profession")), pattern);
            Predicate currentCountryMatch = cb.like(cb.lower(root.get("currentCountry")), pattern);
            Predicate currentCityMatch = cb.like(cb.lower(root.get("currentCity")), pattern);
            Predicate targetCountryMatch = cb.like(cb.lower(root.get("targetCountry")), pattern);
            Predicate targetCityMatch = cb.like(cb.lower(root.get("targetCity")), pattern);
            Predicate hometownMatch = cb.like(cb.lower(root.get("hometown")), pattern);
            Predicate skillsMatch = cb.like(cb.lower(root.get("skills")), pattern);

            return cb.or(
                    nameMatch,
                    collegeMatch,
                    professionMatch,
                    currentCountryMatch,
                    currentCityMatch,
                    targetCountryMatch,
                    targetCityMatch,
                    hometownMatch,
                    skillsMatch
            );
        };
    }

    public static Specification<Profile> filterProfiles(
            Long currentUserId,
            String keyword,
            String college,
            String currentCountry,
            String currentCity,
            String profession,
            UserType userType,
            String targetCountry,
            String targetCity) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (currentUserId != null) {
                predicates.add(cb.notEqual(root.get("user").get("id"), currentUserId));
            }

            if (StringUtils.hasText(keyword)) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                Join<Profile, User> userJoin = root.join("user", JoinType.LEFT);
                Join<Profile, College> collegeJoin = root.join("college", JoinType.LEFT);

                Predicate nameMatch = cb.like(cb.lower(userJoin.get("name")), pattern);
                Predicate collegeMatch = cb.like(cb.lower(collegeJoin.get("name")), pattern);
                Predicate professionMatch = cb.like(cb.lower(root.get("profession")), pattern);
                Predicate currentCountryMatch = cb.like(cb.lower(root.get("currentCountry")), pattern);
                Predicate currentCityMatch = cb.like(cb.lower(root.get("currentCity")), pattern);
                Predicate targetCountryMatch = cb.like(cb.lower(root.get("targetCountry")), pattern);
                Predicate targetCityMatch = cb.like(cb.lower(root.get("targetCity")), pattern);
                Predicate hometownMatch = cb.like(cb.lower(root.get("hometown")), pattern);
                Predicate skillsMatch = cb.like(cb.lower(root.get("skills")), pattern);

                predicates.add(cb.or(
                        nameMatch, collegeMatch, professionMatch,
                        currentCountryMatch, currentCityMatch,
                        targetCountryMatch, targetCityMatch,
                        hometownMatch, skillsMatch
                ));
            }

            if (StringUtils.hasText(college)) {
                Join<Profile, College> collegeJoin = root.join("college", JoinType.LEFT);
                predicates.add(cb.like(cb.lower(collegeJoin.get("name")), "%" + college.trim().toLowerCase() + "%"));
            }

            if (StringUtils.hasText(currentCountry)) {
                predicates.add(cb.equal(cb.lower(root.get("currentCountry")), currentCountry.trim().toLowerCase()));
            }

            if (StringUtils.hasText(currentCity)) {
                predicates.add(cb.equal(cb.lower(root.get("currentCity")), currentCity.trim().toLowerCase()));
            }

            if (StringUtils.hasText(profession)) {
                predicates.add(cb.like(cb.lower(root.get("profession")), "%" + profession.trim().toLowerCase() + "%"));
            }

            if (userType != null) {
                Join<Profile, User> userJoin = root.join("user", JoinType.LEFT);
                predicates.add(cb.equal(userJoin.get("userType"), userType));
            }

            if (StringUtils.hasText(targetCountry)) {
                predicates.add(cb.equal(cb.lower(root.get("targetCountry")), targetCountry.trim().toLowerCase()));
            }

            if (StringUtils.hasText(targetCity)) {
                predicates.add(cb.equal(cb.lower(root.get("targetCity")), targetCity.trim().toLowerCase()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
