package com.connectabroad.controller;

import com.connectabroad.dto.request.CreateProfileRequest;
import com.connectabroad.dto.request.UpdateProfileRequest;
import com.connectabroad.dto.response.PageResponse;
import com.connectabroad.dto.response.ProfileResponse;
import com.connectabroad.dto.response.PublicProfileResponse;
import com.connectabroad.entity.UserType;
import com.connectabroad.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

    private final ProfileService profileService;
    private final com.connectabroad.service.PostService postService;

    public ProfileController(ProfileService profileService, com.connectabroad.service.PostService postService) {
        this.profileService = profileService;
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<ProfileResponse> createProfile(
            Authentication authentication,
            @Valid @RequestBody CreateProfileRequest request) {
        String userEmail = authentication.getName();
        ProfileResponse response = profileService.createProfile(userEmail, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile(Authentication authentication) {
        String userEmail = authentication.getName();
        ProfileResponse response = profileService.getMyProfile(userEmail);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<ProfileResponse> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {
        String userEmail = authentication.getName();
        ProfileResponse response = profileService.updateMyProfile(userEmail, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping({"", "/search", "/people"})
    public ResponseEntity<PageResponse<PublicProfileResponse>> getProfiles(
            Authentication authentication,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "college", required = false) String college,
            @RequestParam(value = "currentCountry", required = false) String currentCountry,
            @RequestParam(value = "currentCity", required = false) String currentCity,
            @RequestParam(value = "profession", required = false) String profession,
            @RequestParam(value = "userType", required = false) UserType userType,
            @RequestParam(value = "targetCountry", required = false) String targetCountry,
            @RequestParam(value = "targetCity", required = false) String targetCity,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "12") int size,
            @RequestParam(value = "sort", defaultValue = "id,desc") String sort) {

        String currentUserEmail = (authentication != null) ? authentication.getName() : null;
        String[] sortParts = sort.split(",");
        String sortProperty = sortParts[0];
        Sort.Direction sortDirection = (sortParts.length > 1 && sortParts[1].equalsIgnoreCase("asc"))
                ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortProperty));

        PageResponse<PublicProfileResponse> response = profileService.searchAndFilterProfiles(
                currentUserEmail,
                keyword,
                college,
                currentCountry,
                currentCity,
                profession,
                userType,
                targetCountry,
                targetCity,
                pageable
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/sections/college")
    public ResponseEntity<PageResponse<PublicProfileResponse>> getPeopleFromSameCollege(
            Authentication authentication,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "6") int size) {
        String currentUserEmail = (authentication != null) ? authentication.getName() : null;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return ResponseEntity.ok(profileService.getPeopleFromSameCollege(currentUserEmail, pageable));
    }

    @GetMapping("/sections/destination")
    public ResponseEntity<PageResponse<PublicProfileResponse>> getPeopleInTargetDestination(
            Authentication authentication,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "6") int size) {
        String currentUserEmail = (authentication != null) ? authentication.getName() : null;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return ResponseEntity.ok(profileService.getPeopleInTargetDestination(currentUserEmail, pageable));
    }

    @GetMapping("/sections/similar-journeys")
    public ResponseEntity<PageResponse<PublicProfileResponse>> getPeoplePlanningSimilarJourneys(
            Authentication authentication,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "6") int size) {
        String currentUserEmail = (authentication != null) ? authentication.getName() : null;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return ResponseEntity.ok(profileService.getPeoplePlanningSimilarJourneys(currentUserEmail, pageable));
    }

    @GetMapping("/sections/near-you")
    public ResponseEntity<PageResponse<PublicProfileResponse>> getPeopleNearYou(
            Authentication authentication,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "6") int size) {
        String currentUserEmail = (authentication != null) ? authentication.getName() : null;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return ResponseEntity.ok(profileService.getPeopleNearYou(currentUserEmail, pageable));
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<PublicProfileResponse> getPublicProfile(
            Authentication authentication,
            @PathVariable("id") Long userId) {
        String currentUserEmail = (authentication != null) ? authentication.getName() : null;
        if (currentUserEmail != null) {
            return ResponseEntity.ok(profileService.getPublicProfileWithContext(currentUserEmail, userId));
        } else {
            return ResponseEntity.ok(profileService.getPublicProfile(userId));
        }
    }

    @GetMapping("/{id:\\d+}/posts")
    public ResponseEntity<PageResponse<com.connectabroad.dto.response.PostResponse>> getPostsByAuthor(
            Authentication authentication,
            @PathVariable("id") Long userId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        String currentUserEmail = (authentication != null) ? authentication.getName() : null;
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(postService.getPostsByAuthor(userId, currentUserEmail, pageable));
    }
}
