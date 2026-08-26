package com.connectabroad.service;

import com.connectabroad.dto.request.CreateProfileRequest;
import com.connectabroad.dto.request.UpdateProfileRequest;
import com.connectabroad.dto.response.CompletionChecklistDto;
import com.connectabroad.dto.response.PageResponse;
import com.connectabroad.dto.response.ProfileResponse;
import com.connectabroad.dto.response.PublicProfileResponse;
import com.connectabroad.entity.College;
import com.connectabroad.entity.Profile;
import com.connectabroad.entity.User;
import com.connectabroad.entity.UserType;
import com.connectabroad.exception.ProfileAlreadyExistsException;
import com.connectabroad.exception.ProfileNotFoundException;
import com.connectabroad.exception.ResourceNotFoundException;
import com.connectabroad.entity.Connection;
import com.connectabroad.entity.ConnectionStatus;
import com.connectabroad.repository.CollegeRepository;
import com.connectabroad.repository.ConnectionRepository;
import com.connectabroad.repository.ProfileRepository;
import com.connectabroad.repository.UserRepository;
import com.connectabroad.specification.ProfileSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final CollegeRepository collegeRepository;
    private final ConnectionRepository connectionRepository;

    public ProfileService(ProfileRepository profileRepository,
                          UserRepository userRepository,
                          CollegeRepository collegeRepository,
                          ConnectionRepository connectionRepository) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.collegeRepository = collegeRepository;
        this.connectionRepository = connectionRepository;
    }

    @Transactional
    public ProfileResponse createProfile(String userEmail, CreateProfileRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        if (profileRepository.existsByUserId(user.getId())) {
            throw new ProfileAlreadyExistsException("Profile already exists for user: " + userEmail);
        }

        if (StringUtils.hasText(request.getName())) {
            user.setName(request.getName());
            userRepository.save(user);
        }

        Profile profile = new Profile(user);
        updateProfileFieldsFromCreateRequest(profile, request);

        Profile savedProfile = profileRepository.save(profile);
        return mapToProfileResponse(savedProfile, user);
    }

    @Transactional(readOnly = true)
    public ProfileResponse getMyProfile(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ProfileNotFoundException("Profile not found for user: " + userEmail));

        return mapToProfileResponse(profile, user);
    }

    @Transactional
    public ProfileResponse updateMyProfile(String userEmail, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ProfileNotFoundException("Profile not found for user: " + userEmail));

        if (StringUtils.hasText(request.getName())) {
            user.setName(request.getName());
            userRepository.save(user);
        }

        updateProfileFieldsFromUpdateRequest(profile, request);

        Profile updatedProfile = profileRepository.save(profile);
        return mapToProfileResponse(updatedProfile, user);
    }

    @Transactional(readOnly = true)
    public PublicProfileResponse getPublicProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ProfileNotFoundException("Profile not found for user id: " + userId));

        return mapToPublicProfileResponse(profile, null);
    }

    @Transactional(readOnly = true)
    public PublicProfileResponse getPublicProfileWithContext(String currentUserEmail, Long targetUserId) {
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + targetUserId));

        Profile targetProfile = profileRepository.findByUserId(targetUserId)
                .orElseThrow(() -> new ProfileNotFoundException("Profile not found for user id: " + targetUserId));

        Profile currentProfile = profileRepository.findByUserEmail(currentUserEmail).orElse(null);
        return mapToPublicProfileResponse(targetProfile, currentProfile);
    }

    @Transactional(readOnly = true)
    public PageResponse<PublicProfileResponse> searchAndFilterProfiles(
            String currentUserEmail,
            String keyword,
            String college,
            String currentCountry,
            String currentCity,
            String profession,
            UserType userType,
            String targetCountry,
            String targetCity,
            Pageable pageable) {

        User currentUser = StringUtils.hasText(currentUserEmail)
                ? userRepository.findByEmail(currentUserEmail).orElse(null)
                : null;

        Long currentUserId = (currentUser != null) ? currentUser.getId() : null;
        Profile currentProfile = (currentUserId != null)
                ? profileRepository.findByUserId(currentUserId).orElse(null)
                : null;

        Specification<Profile> spec = ProfileSpecification.filterProfiles(
                currentUserId,
                keyword,
                college,
                currentCountry,
                currentCity,
                profession,
                userType,
                targetCountry,
                targetCity
        );

        Page<Profile> profilesPage = profileRepository.findAll(spec, pageable);
        Page<PublicProfileResponse> responsePage = profilesPage.map(p -> mapToPublicProfileResponse(p, currentProfile));

        return PageResponse.from(responsePage);
    }

    @Transactional(readOnly = true)
    public PageResponse<PublicProfileResponse> getPeopleFromSameCollege(String currentUserEmail, Pageable pageable) {
        User currentUser = StringUtils.hasText(currentUserEmail)
                ? userRepository.findByEmail(currentUserEmail).orElse(null)
                : null;

        Profile currentProfile = (currentUser != null)
                ? profileRepository.findByUserId(currentUser.getId()).orElse(null)
                : null;

        if (currentProfile == null || currentProfile.getCollege() == null) {
            return searchAndFilterProfiles(currentUserEmail, null, null, null, null, null, null, null, null, pageable);
        }

        String collegeName = currentProfile.getCollege().getName();
        return searchAndFilterProfiles(currentUserEmail, null, collegeName, null, null, null, null, null, null, pageable);
    }

    @Transactional(readOnly = true)
    public PageResponse<PublicProfileResponse> getPeopleInTargetDestination(String currentUserEmail, Pageable pageable) {
        User currentUser = StringUtils.hasText(currentUserEmail)
                ? userRepository.findByEmail(currentUserEmail).orElse(null)
                : null;

        Profile currentProfile = (currentUser != null)
                ? profileRepository.findByUserId(currentUser.getId()).orElse(null)
                : null;

        if (currentProfile == null || !StringUtils.hasText(currentProfile.getTargetCountry())) {
            return searchAndFilterProfiles(currentUserEmail, null, null, null, null, null, UserType.ABROAD, null, null, pageable);
        }

        return searchAndFilterProfiles(
                currentUserEmail,
                null,
                null,
                currentProfile.getTargetCountry(),
                currentProfile.getTargetCity(),
                null,
                UserType.ABROAD,
                null,
                null,
                pageable
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<PublicProfileResponse> getPeoplePlanningSimilarJourneys(String currentUserEmail, Pageable pageable) {
        User currentUser = StringUtils.hasText(currentUserEmail)
                ? userRepository.findByEmail(currentUserEmail).orElse(null)
                : null;

        Profile currentProfile = (currentUser != null)
                ? profileRepository.findByUserId(currentUser.getId()).orElse(null)
                : null;

        String targetCountry = currentProfile != null ? currentProfile.getTargetCountry() : null;
        String targetCity = currentProfile != null ? currentProfile.getTargetCity() : null;

        return searchAndFilterProfiles(
                currentUserEmail,
                null,
                null,
                null,
                null,
                null,
                UserType.ASPIRING,
                targetCountry,
                targetCity,
                pageable
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<PublicProfileResponse> getPeopleNearYou(String currentUserEmail, Pageable pageable) {
        User currentUser = StringUtils.hasText(currentUserEmail)
                ? userRepository.findByEmail(currentUserEmail).orElse(null)
                : null;

        Profile currentProfile = (currentUser != null)
                ? profileRepository.findByUserId(currentUser.getId()).orElse(null)
                : null;

        String currentCountry = currentProfile != null ? currentProfile.getCurrentCountry() : null;
        String currentCity = currentProfile != null ? currentProfile.getCurrentCity() : null;

        return searchAndFilterProfiles(
                currentUserEmail,
                null,
                null,
                currentCountry,
                currentCity,
                null,
                UserType.ABROAD,
                null,
                null,
                pageable
        );
    }

    private void updateProfileFieldsFromCreateRequest(Profile profile, CreateProfileRequest request) {
        profile.setBio(request.getBio());
        profile.setProfilePhoto(request.getProfilePhoto());
        profile.setDegree(request.getDegree());
        profile.setGraduationYear(request.getGraduationYear());
        profile.setHometown(request.getHometown());
        profile.setCurrentCountry(request.getCurrentCountry());
        profile.setCurrentCity(request.getCurrentCity());
        profile.setTargetCountry(request.getTargetCountry());
        profile.setTargetCity(request.getTargetCity());
        profile.setTargetUniversity(request.getTargetUniversity());
        profile.setExpectedMoveDate(request.getExpectedMoveDate());
        profile.setMovedYear(request.getMovedYear());
        profile.setProfession(request.getProfession());
        profile.setExperienceYears(request.getExperienceYears());
        profile.setSkills(request.getSkills());

        if (StringUtils.hasText(request.getCollegeName())) {
            College college = findOrCreateCollege(
                    request.getCollegeName(),
                    request.getCollegeCity(),
                    request.getCollegeState(),
                    request.getCollegeCountry()
            );
            profile.setCollege(college);
        }
    }

    private void updateProfileFieldsFromUpdateRequest(Profile profile, UpdateProfileRequest request) {
        if (request.getBio() != null) profile.setBio(request.getBio());
        if (request.getProfilePhoto() != null) profile.setProfilePhoto(request.getProfilePhoto());
        if (request.getDegree() != null) profile.setDegree(request.getDegree());
        if (request.getGraduationYear() != null) profile.setGraduationYear(request.getGraduationYear());
        if (request.getHometown() != null) profile.setHometown(request.getHometown());
        if (request.getCurrentCountry() != null) profile.setCurrentCountry(request.getCurrentCountry());
        if (request.getCurrentCity() != null) profile.setCurrentCity(request.getCurrentCity());
        if (request.getTargetCountry() != null) profile.setTargetCountry(request.getTargetCountry());
        if (request.getTargetCity() != null) profile.setTargetCity(request.getTargetCity());
        if (request.getTargetUniversity() != null) profile.setTargetUniversity(request.getTargetUniversity());
        if (request.getExpectedMoveDate() != null) profile.setExpectedMoveDate(request.getExpectedMoveDate());
        if (request.getMovedYear() != null) profile.setMovedYear(request.getMovedYear());
        if (request.getProfession() != null) profile.setProfession(request.getProfession());
        if (request.getExperienceYears() != null) profile.setExperienceYears(request.getExperienceYears());
        if (request.getSkills() != null) profile.setSkills(request.getSkills());

        if (StringUtils.hasText(request.getCollegeName())) {
            College college = findOrCreateCollege(
                    request.getCollegeName(),
                    request.getCollegeCity(),
                    request.getCollegeState(),
                    request.getCollegeCountry()
            );
            profile.setCollege(college);
        }
    }

    private College findOrCreateCollege(String name, String city, String state, String country) {
        String trimmedName = name.trim();
        Optional<College> existing = collegeRepository.findByNameIgnoreCase(trimmedName);
        if (existing.isPresent()) {
            return existing.get();
        }
        College newCollege = new College(trimmedName, city, state, country);
        return collegeRepository.save(newCollege);
    }

    public ProfileResponse mapToProfileResponse(Profile profile, User user) {
        ProfileResponse dto = new ProfileResponse();
        dto.setId(profile.getId());
        dto.setUserId(user.getId());
        dto.setUserName(user.getName());
        dto.setUserEmail(user.getEmail());
        dto.setUserType(user.getUserType());
        dto.setRole(user.getRole());

        dto.setBio(profile.getBio());
        dto.setProfilePhoto(profile.getProfilePhoto());

        if (profile.getCollege() != null) {
            dto.setCollegeId(profile.getCollege().getId());
            dto.setCollegeName(profile.getCollege().getName());
            dto.setCollegeCity(profile.getCollege().getCity());
            dto.setCollegeState(profile.getCollege().getState());
            dto.setCollegeCountry(profile.getCollege().getCountry());
        }

        dto.setDegree(profile.getDegree());
        dto.setGraduationYear(profile.getGraduationYear());

        dto.setHometown(profile.getHometown());
        dto.setCurrentCountry(profile.getCurrentCountry());
        dto.setCurrentCity(profile.getCurrentCity());
        dto.setTargetCountry(profile.getTargetCountry());
        dto.setTargetCity(profile.getTargetCity());
        dto.setTargetUniversity(profile.getTargetUniversity());
        dto.setExpectedMoveDate(profile.getExpectedMoveDate());
        dto.setMovedYear(profile.getMovedYear());

        dto.setProfession(profile.getProfession());
        dto.setExperienceYears(profile.getExperienceYears());
        dto.setSkills(profile.getSkills());

        dto.setCreatedAt(profile.getCreatedAt());
        dto.setUpdatedAt(profile.getUpdatedAt());

        CompletionChecklistDto checklist = calculateChecklist(profile, user);
        dto.setCompletionChecklist(checklist);
        dto.setProfileCompletion(calculateCompletionPercentage(checklist));

        dto.setConnectionCount(connectionRepository.countAcceptedConnectionsForUser(user.getId()));

        return dto;
    }

    private PublicProfileResponse mapToPublicProfileResponse(Profile profile, Profile currentProfile) {
        User user = profile.getUser();
        PublicProfileResponse dto = new PublicProfileResponse();
        dto.setId(profile.getId());
        dto.setUserId(user.getId());
        dto.setName(user.getName());
        dto.setUserType(user.getUserType());

        dto.setBio(profile.getBio());
        dto.setProfilePhoto(profile.getProfilePhoto());

        if (profile.getCollege() != null) {
            dto.setCollegeName(profile.getCollege().getName());
            dto.setCollegeCity(profile.getCollege().getCity());
            dto.setCollegeCountry(profile.getCollege().getCountry());
        }

        dto.setDegree(profile.getDegree());
        dto.setGraduationYear(profile.getGraduationYear());

        dto.setHometown(profile.getHometown());
        dto.setCurrentCountry(profile.getCurrentCountry());
        dto.setCurrentCity(profile.getCurrentCity());
        dto.setTargetCountry(profile.getTargetCountry());
        dto.setTargetCity(profile.getTargetCity());
        dto.setTargetUniversity(profile.getTargetUniversity());
        dto.setExpectedMoveDate(profile.getExpectedMoveDate());
        dto.setMovedYear(profile.getMovedYear());

        dto.setProfession(profile.getProfession());
        dto.setExperienceYears(profile.getExperienceYears());
        dto.setSkills(profile.getSkills());

        dto.setMatchReasons(calculateMatchReasons(currentProfile, profile));

        dto.setConnectionCount(connectionRepository.countAcceptedConnectionsForUser(user.getId()));

        if (currentProfile != null && currentProfile.getUser() != null) {
            Long currentUserId = currentProfile.getUser().getId();
            if (!currentUserId.equals(user.getId())) {
                Optional<Connection> connOpt = connectionRepository.findConnectionBetweenUsers(currentUserId, user.getId());
                if (connOpt.isPresent()) {
                    Connection conn = connOpt.get();
                    dto.setConnectionId(conn.getId());
                    if (conn.getStatus() == ConnectionStatus.ACCEPTED) {
                        dto.setConnectionStatus("CONNECTED");
                    } else if (conn.getStatus() == ConnectionStatus.PENDING) {
                        if (conn.getSender().getId().equals(currentUserId)) {
                            dto.setConnectionStatus("PENDING_SENT");
                        } else {
                            dto.setConnectionStatus("PENDING_RECEIVED");
                        }
                    } else if (conn.getStatus() == ConnectionStatus.REJECTED) {
                        dto.setConnectionStatus("REJECTED");
                    }
                }
            }
        }

        return dto;
    }

    public List<String> calculateMatchReasons(Profile current, Profile other) {
        List<String> reasons = new ArrayList<>();
        if (current == null || other == null) return reasons;

        // Rule 1: Same College
        if (current.getCollege() != null && other.getCollege() != null &&
                StringUtils.hasText(current.getCollege().getName()) &&
                current.getCollege().getName().equalsIgnoreCase(other.getCollege().getName())) {
            reasons.add("✓ Same college");
        }

        // Rule 2: Same Hometown
        if (StringUtils.hasText(current.getHometown()) && StringUtils.hasText(other.getHometown()) &&
                current.getHometown().equalsIgnoreCase(other.getHometown())) {
            reasons.add("✓ Same hometown");
        }

        // Rule 3: Lives in your target country
        if (current.getUser() != null && current.getUser().getUserType() == UserType.ASPIRING &&
                other.getUser() != null && other.getUser().getUserType() == UserType.ABROAD &&
                StringUtils.hasText(current.getTargetCountry()) && StringUtils.hasText(other.getCurrentCountry()) &&
                current.getTargetCountry().equalsIgnoreCase(other.getCurrentCountry())) {
            reasons.add("✓ Lives in your target country");
        }

        // Rule 4: Lives in your target city
        if (current.getUser() != null && current.getUser().getUserType() == UserType.ASPIRING &&
                other.getUser() != null && other.getUser().getUserType() == UserType.ABROAD &&
                StringUtils.hasText(current.getTargetCity()) && StringUtils.hasText(other.getCurrentCity()) &&
                current.getTargetCity().equalsIgnoreCase(other.getCurrentCity())) {
            reasons.add("✓ Lives in your target city");
        }

        // Rule 5: Same profession
        if (StringUtils.hasText(current.getProfession()) && StringUtils.hasText(other.getProfession()) &&
                (current.getProfession().equalsIgnoreCase(other.getProfession()) ||
                 current.getProfession().toLowerCase().contains(other.getProfession().toLowerCase()) ||
                 other.getProfession().toLowerCase().contains(current.getProfession().toLowerCase()))) {
            reasons.add("✓ Same profession");
        }

        // Rule 6: Planning move to same target country
        if (current.getUser() != null && current.getUser().getUserType() == UserType.ASPIRING &&
                other.getUser() != null && other.getUser().getUserType() == UserType.ASPIRING &&
                StringUtils.hasText(current.getTargetCountry()) && StringUtils.hasText(other.getTargetCountry()) &&
                current.getTargetCountry().equalsIgnoreCase(other.getTargetCountry())) {
            reasons.add("✓ Planning move to same destination");
        }

        // Rule 7: Living in same city / country
        if (current.getUser() != null && current.getUser().getUserType() == UserType.ABROAD &&
                other.getUser() != null && other.getUser().getUserType() == UserType.ABROAD &&
                StringUtils.hasText(current.getCurrentCountry()) && StringUtils.hasText(other.getCurrentCountry()) &&
                current.getCurrentCountry().equalsIgnoreCase(other.getCurrentCountry())) {
            reasons.add("✓ Living in same country");
        }

        return reasons;
    }

    public CompletionChecklistDto calculateChecklist(Profile profile, User user) {
        boolean basicInfo = StringUtils.hasText(user.getName());
        boolean profilePhoto = StringUtils.hasText(profile.getProfilePhoto());
        boolean bio = StringUtils.hasText(profile.getBio());
        boolean college = profile.getCollege() != null && StringUtils.hasText(profile.getCollege().getName());
        boolean hometown = StringUtils.hasText(profile.getHometown());
        boolean currentLocation = StringUtils.hasText(profile.getCurrentCountry());
        boolean profession = StringUtils.hasText(profile.getProfession());

        boolean journey;
        if (user.getUserType() == UserType.ASPIRING) {
            journey = StringUtils.hasText(profile.getTargetCountry()) || profile.getExpectedMoveDate() != null;
        } else {
            journey = profile.getMovedYear() != null || StringUtils.hasText(profile.getCurrentCity());
        }

        return new CompletionChecklistDto(
                basicInfo,
                profilePhoto,
                bio,
                college,
                hometown,
                currentLocation,
                profession,
                journey
        );
    }

    private int calculateCompletionPercentage(CompletionChecklistDto checklist) {
        int count = 0;
        if (checklist.isBasicInfo()) count++;
        if (checklist.isProfilePhoto()) count++;
        if (checklist.isBio()) count++;
        if (checklist.isCollege()) count++;
        if (checklist.isHometown()) count++;
        if (checklist.isCurrentLocation()) count++;
        if (checklist.isProfession()) count++;
        if (checklist.isJourney()) count++;

        return (int) Math.round((count / 8.0) * 100);
    }
}
