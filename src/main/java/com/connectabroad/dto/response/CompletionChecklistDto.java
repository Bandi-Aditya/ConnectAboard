package com.connectabroad.dto.response;

public class CompletionChecklistDto {

    private boolean basicInfo;
    private boolean profilePhoto;
    private boolean bio;
    private boolean college;
    private boolean hometown;
    private boolean currentLocation;
    private boolean profession;
    private boolean journey;

    public CompletionChecklistDto() {
    }

    public CompletionChecklistDto(boolean basicInfo, boolean profilePhoto, boolean bio, boolean college,
                                  boolean hometown, boolean currentLocation, boolean profession, boolean journey) {
        this.basicInfo = basicInfo;
        this.profilePhoto = profilePhoto;
        this.bio = bio;
        this.college = college;
        this.hometown = hometown;
        this.currentLocation = currentLocation;
        this.profession = profession;
        this.journey = journey;
    }

    public boolean isBasicInfo() {
        return basicInfo;
    }

    public void setBasicInfo(boolean basicInfo) {
        this.basicInfo = basicInfo;
    }

    public boolean isProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(boolean profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public boolean isBio() {
        return bio;
    }

    public void setBio(boolean bio) {
        this.bio = bio;
    }

    public boolean isCollege() {
        return college;
    }

    public void setCollege(boolean college) {
        this.college = college;
    }

    public boolean isHometown() {
        return hometown;
    }

    public void setHometown(boolean hometown) {
        this.hometown = hometown;
    }

    public boolean isCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(boolean currentLocation) {
        this.currentLocation = currentLocation;
    }

    public boolean isProfession() {
        return profession;
    }

    public void setProfession(boolean profession) {
        this.profession = profession;
    }

    public boolean isJourney() {
        return journey;
    }

    public void setJourney(boolean journey) {
        this.journey = journey;
    }
}
