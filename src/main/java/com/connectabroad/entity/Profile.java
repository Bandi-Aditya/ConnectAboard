package com.connectabroad.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "profiles")
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(length = 1000)
    private String bio;

    @Column(name = "profile_photo")
    private String profilePhoto;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "college_id")
    private College college;

    private String degree;

    @Column(name = "graduation_year")
    private Integer graduationYear;

    private String hometown;

    @Column(name = "current_country")
    private String currentCountry;

    @Column(name = "current_city")
    private String currentCity;

    @Column(name = "target_country")
    private String targetCountry;

    @Column(name = "target_city")
    private String targetCity;

    @Column(name = "target_university")
    private String targetUniversity;

    @Column(name = "expected_move_date")
    private LocalDate expectedMoveDate;

    @Column(name = "moved_year")
    private Integer movedYear;

    private String profession;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Column(length = 1000)
    private String skills;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Profile() {
    }

    public Profile(User user) {
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public College getCollege() {
        return college;
    }

    public void setCollege(College college) {
        this.college = college;
    }

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }

    public Integer getGraduationYear() {
        return graduationYear;
    }

    public void setGraduationYear(Integer graduationYear) {
        this.graduationYear = graduationYear;
    }

    public String getHometown() {
        return hometown;
    }

    public void setHometown(String hometown) {
        this.hometown = hometown;
    }

    public String getCurrentCountry() {
        return currentCountry;
    }

    public void setCurrentCountry(String currentCountry) {
        this.currentCountry = currentCountry;
    }

    public String getCurrentCity() {
        return currentCity;
    }

    public void setCurrentCity(String currentCity) {
        this.currentCity = currentCity;
    }

    public String getTargetCountry() {
        return targetCountry;
    }

    public void setTargetCountry(String targetCountry) {
        this.targetCountry = targetCountry;
    }

    public String getTargetCity() {
        return targetCity;
    }

    public void setTargetCity(String targetCity) {
        this.targetCity = targetCity;
    }

    public String getTargetUniversity() {
        return targetUniversity;
    }

    public void setTargetUniversity(String targetUniversity) {
        this.targetUniversity = targetUniversity;
    }

    public LocalDate getExpectedMoveDate() {
        return expectedMoveDate;
    }

    public void setExpectedMoveDate(LocalDate expectedMoveDate) {
        this.expectedMoveDate = expectedMoveDate;
    }

    public Integer getMovedYear() {
        return movedYear;
    }

    public void setMovedYear(Integer movedYear) {
        this.movedYear = movedYear;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public Integer getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(Integer experienceYears) {
        this.experienceYears = experienceYears;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
