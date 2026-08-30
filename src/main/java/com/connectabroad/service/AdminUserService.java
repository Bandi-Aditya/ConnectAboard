package com.connectabroad.service;

import com.connectabroad.dto.admin.AdminUserDetailResponse;
import com.connectabroad.dto.response.PageResponse;
import com.connectabroad.dto.response.UserResponse;
import com.connectabroad.entity.Profile;
import com.connectabroad.entity.Role;
import com.connectabroad.entity.User;
import com.connectabroad.entity.UserStatus;
import com.connectabroad.exception.ResourceNotFoundException;
import com.connectabroad.repository.ProfileRepository;
import com.connectabroad.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class AdminUserService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final AdminAuditLogService auditLogService;

    public AdminUserService(UserRepository userRepository,
                            ProfileRepository profileRepository,
                            AdminAuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getUsers(String keyword, Role role, UserStatus status, Pageable pageable) {
        Page<User> page = userRepository.findAdminUsers(keyword, role, status, pageable);
        return PageResponse.from(page.map(this::mapToUserResponse));
    }

    @Transactional(readOnly = true)
    public AdminUserDetailResponse getUserDetails(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Optional<Profile> profileOpt = profileRepository.findByUserId(userId);

        AdminUserDetailResponse dto = new AdminUserDetailResponse();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        dto.setUserType(user.getUserType());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());

        if (profileOpt.isPresent()) {
            Profile prof = profileOpt.get();
            dto.setProfilePhoto(prof.getProfilePhoto());
            dto.setBio(prof.getBio());
            dto.setCollegeName(prof.getCollege() != null ? prof.getCollege().getName() : null);
            dto.setDegree(prof.getDegree());
            dto.setGraduationYear(prof.getGraduationYear());
            dto.setHometown(prof.getHometown());
            dto.setCurrentCountry(prof.getCurrentCountry());
            dto.setCurrentCity(prof.getCurrentCity());
            dto.setTargetCountry(prof.getTargetCountry());
            dto.setTargetCity(prof.getTargetCity());
            dto.setProfession(prof.getProfession());
            dto.setProfileCompletionPercentage(calculateCompletionPercentage(prof));
        } else {
            dto.setProfileCompletionPercentage(20);
        }

        return dto;
    }

    public UserResponse updateUserStatus(String adminEmail, Long userId, UserStatus newStatus) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with email: " + adminEmail));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        UserStatus oldStatus = user.getStatus();
        user.setStatus(newStatus);
        User saved = userRepository.save(user);

        auditLogService.logAction(
                admin,
                "UPDATE_USER_STATUS",
                "USER",
                userId,
                "Changed status for user " + user.getEmail() + " from " + oldStatus + " to " + newStatus
        );

        return mapToUserResponse(saved);
    }

    public UserResponse updateUserRole(String adminEmail, Long userId, Role newRole) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with email: " + adminEmail));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Role oldRole = user.getRole();
        user.setRole(newRole);
        User saved = userRepository.save(user);

        auditLogService.logAction(
                admin,
                "UPDATE_USER_ROLE",
                "USER",
                userId,
                "Changed role for user " + user.getEmail() + " from " + oldRole + " to " + newRole
        );

        return mapToUserResponse(saved);
    }

    private UserResponse mapToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getUserType(),
                user.getCreatedAt(),
                user.isEnabled()
        );
    }

    private int calculateCompletionPercentage(Profile p) {
        int score = 0;
        int total = 7;
        if (p.getBio() != null && !p.getBio().isBlank()) score++;
        if (p.getProfilePhoto() != null && !p.getProfilePhoto().isBlank()) score++;
        if (p.getCollege() != null) score++;
        if (p.getDegree() != null && !p.getDegree().isBlank()) score++;
        if (p.getHometown() != null && !p.getHometown().isBlank()) score++;
        if (p.getCurrentCountry() != null && !p.getCurrentCountry().isBlank()) score++;
        if (p.getProfession() != null && !p.getProfession().isBlank()) score++;
        return (int) Math.round(((double) score / total) * 100);
    }
}
