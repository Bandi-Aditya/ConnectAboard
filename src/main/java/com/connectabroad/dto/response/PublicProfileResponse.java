package com.connectabroad.dto.response;

import com.connectabroad.entity.UserType;

import java.time.LocalDate;

public class PublicProfileResponse {

    private Long id;
    private Long userId;
    private String name;
    private UserType userType;

    private String bio;
    private String profilePhoto;

    private String collegeName;
    private String collegeCity;
    private String collegeCountry;

    private String degree;
    private Integer graduationYear;

    private String hometown;
    private String currentCountry;
    private String currentCity;
    private String targetCountry;
    private String targetCity;
    private String targetUniversity;
    private LocalDate expectedMoveDate;
    private Integer movedYear;

    private String profession;
    private Integer experienceYears;
    private String skills;
    private java.util.List<String> matchReasons = new java.util.ArrayList<>();

    private String connectionStatus = "NONE";
    private Long connectionId;
    private long connectionCount = 0;

    public PublicProfileResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
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

    public String getCollege() {
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

    public java.util.List<String> getMatchReasons() {
        return matchReasons;
    }

    public void setMatchReasons(java.util.List<String> matchReasons) {
        this.matchReasons = matchReasons;
    }

    public String getConnectionStatus() {
        return connectionStatus;
    }

    public void setConnectionStatus(String connectionStatus) {
        this.connectionStatus = connectionStatus;
    }

    public Long getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(Long connectionId) {
        this.connectionId = connectionId;
    }

    public long getConnectionCount() {
        return connectionCount;
    }

    public void setConnectionCount(long connectionCount) {
        this.connectionCount = connectionCount;
    }
}
