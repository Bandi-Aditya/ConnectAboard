package com.connectabroad.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class CreateProfileRequest {

    private String name;

    @Size(max = 1000, message = "Bio cannot exceed 1000 characters")
    private String bio;

    private String profilePhoto;

    private String collegeName;

    private String collegeCity;

    private String collegeState;

    private String collegeCountry;

    private String degree;

    @Min(value = 1950, message = "Graduation year must be valid")
    @Max(value = 2035, message = "Graduation year must be valid")
    private Integer graduationYear;

    private String hometown;

    private String currentCountry;

    private String currentCity;

    private String targetCountry;

    private String targetCity;

    private String targetUniversity;

    private LocalDate expectedMoveDate;

    @Min(value = 1950, message = "Moved year must be valid")
    @Max(value = 2035, message = "Moved year must be valid")
    private Integer movedYear;

    private String profession;

    @Min(value = 0, message = "Experience years cannot be negative")
    private Integer experienceYears;

    @Size(max = 1000, message = "Skills cannot exceed 1000 characters")
    private String skills;

    public CreateProfileRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getCollegeName() {
        return collegeName;
    }

    public void setCollegeName(String collegeName) {
        this.collegeName = collegeName;
    }

    public String getCollegeCity() {
        return collegeCity;
    }

    public void setCollegeCity(String collegeCity) {
        this.collegeCity = collegeCity;
    }

    public String getCollegeState() {
        return collegeState;
    }

    public void setCollegeState(String collegeState) {
        this.collegeState = collegeState;
    }

    public String getCollegeCountry() {
        return collegeCountry;
    }

    public void setCollegeCountry(String collegeCountry) {
        this.collegeCountry = collegeCountry;
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
}
