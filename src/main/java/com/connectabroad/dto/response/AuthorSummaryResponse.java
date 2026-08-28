package com.connectabroad.dto.response;

import com.connectabroad.entity.UserType;

public class AuthorSummaryResponse {

    private Long userId;
    private String name;
    private String profilePhoto;
    private String profession;
    private String currentCity;
    private String currentCountry;
    private String collegeName;
    private UserType userType;

    public AuthorSummaryResponse() {}

    public AuthorSummaryResponse(Long userId, String name, String profilePhoto, String profession,
                                 String currentCity, String currentCountry, String collegeName, UserType userType) {
        this.userId = userId;
        this.name = name;
        this.profilePhoto = profilePhoto;
        this.profession = profession;
        this.currentCity = currentCity;
        this.currentCountry = currentCountry;
        this.collegeName = collegeName;
        this.userType = userType;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getProfilePhoto() { return profilePhoto; }
    public void setProfilePhoto(String profilePhoto) { this.profilePhoto = profilePhoto; }

    public String getProfession() { return profession; }
    public void setProfession(String profession) { this.profession = profession; }

    public String getCurrentCity() { return currentCity; }
    public void setCurrentCity(String currentCity) { this.currentCity = currentCity; }

    public String getCurrentCountry() { return currentCountry; }
    public void setCurrentCountry(String currentCountry) { this.currentCountry = currentCountry; }

    public String getCollegeName() { return collegeName; }
    public void setCollegeName(String collegeName) { this.collegeName = collegeName; }

    public UserType getUserType() { return userType; }
    public void setUserType(UserType userType) { this.userType = userType; }
}
