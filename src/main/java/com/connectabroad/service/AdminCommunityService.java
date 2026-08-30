package com.connectabroad.service;

import com.connectabroad.dto.admin.CommunityResponse;
import com.connectabroad.dto.response.PageResponse;
import com.connectabroad.entity.Community;
import com.connectabroad.entity.CommunityStatus;
import com.connectabroad.entity.User;
import com.connectabroad.exception.ResourceNotFoundException;
import com.connectabroad.repository.CommunityRepository;
import com.connectabroad.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AdminCommunityService {

    private final CommunityRepository communityRepository;
    private final CommunityService communityService;
    private final UserRepository userRepository;
    private final AdminAuditLogService auditLogService;

    public AdminCommunityService(CommunityRepository communityRepository,
                                 CommunityService communityService,
                                 UserRepository userRepository,
                                 AdminAuditLogService auditLogService) {
        this.communityRepository = communityRepository;
        this.communityService = communityService;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public PageResponse<CommunityResponse> searchCommunities(String keyword, CommunityStatus status, Pageable pageable) {
        Page<Community> page = communityRepository.findAdminCommunities(keyword, status, pageable);
        return PageResponse.from(page.map(c -> communityService.mapToCommunityResponse(c, null)));
    }

    public CommunityResponse updateCommunityStatus(String adminEmail, Long communityId, CommunityStatus newStatus) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found: " + adminEmail));
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new ResourceNotFoundException("Community not found with id: " + communityId));

        CommunityStatus oldStatus = community.getStatus();
        community.setStatus(newStatus);
        Community saved = communityRepository.save(community);

        auditLogService.logAction(
                admin,
                "UPDATE_COMMUNITY_STATUS",
                "COMMUNITY",
                communityId,
                "Changed community '" + community.getName() + "' status from " + oldStatus + " to " + newStatus
        );

        return communityService.mapToCommunityResponse(saved, null);
    }

    public void removeCommunity(String adminEmail, Long communityId) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found: " + adminEmail));
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new ResourceNotFoundException("Community not found with id: " + communityId));

        String communityName = community.getName();
        communityRepository.delete(community);

        auditLogService.logAction(
                admin,
                "DELETE_COMMUNITY",
                "COMMUNITY",
                communityId,
                "Deleted community: " + communityName
        );
    }
}
