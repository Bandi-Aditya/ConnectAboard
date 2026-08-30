package com.connectabroad.service;

import com.connectabroad.dto.admin.CommunityResponse;
import com.connectabroad.dto.admin.CreateCommunityRequest;
import com.connectabroad.dto.response.PageResponse;
import com.connectabroad.entity.Community;
import com.connectabroad.entity.CommunityMember;
import com.connectabroad.entity.CommunityStatus;
import com.connectabroad.entity.User;
import com.connectabroad.exception.ResourceNotFoundException;
import com.connectabroad.repository.CommunityMemberRepository;
import com.connectabroad.repository.CommunityRepository;
import com.connectabroad.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final CommunityMemberRepository communityMemberRepository;
    private final UserRepository userRepository;

    public CommunityService(CommunityRepository communityRepository,
                            CommunityMemberRepository communityMemberRepository,
                            UserRepository userRepository) {
        this.communityRepository = communityRepository;
        this.communityMemberRepository = communityMemberRepository;
        this.userRepository = userRepository;
    }

    public CommunityResponse createCommunity(String userEmail, CreateCommunityRequest request) {
        User creator = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        Community community = new Community(
                request.getName(),
                request.getDescription(),
                request.getCategory(),
                request.getLocation(),
                creator
        );

        Community saved = communityRepository.save(community);

        CommunityMember member = new CommunityMember(saved, creator);
        communityMemberRepository.save(member);

        return mapToCommunityResponse(saved, creator.getId());
    }

    @Transactional(readOnly = true)
    public PageResponse<CommunityResponse> getCommunities(String userEmail, Pageable pageable) {
        User user = userEmail != null ? userRepository.findByEmail(userEmail).orElse(null) : null;
        Long currentUserId = user != null ? user.getId() : null;

        Page<Community> page = communityRepository.findByStatusOrderByCreatedAtDesc(CommunityStatus.ACTIVE, pageable);
        return PageResponse.from(page.map(c -> mapToCommunityResponse(c, currentUserId)));
    }

    public CommunityResponse joinCommunity(String userEmail, Long communityId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new ResourceNotFoundException("Community not found: " + communityId));

        if (!communityMemberRepository.existsByCommunityIdAndUserId(communityId, user.getId())) {
            CommunityMember member = new CommunityMember(community, user);
            communityMemberRepository.save(member);
            community.setMemberCount(communityMemberRepository.countByCommunityId(communityId));
            communityRepository.save(community);
        }

        return mapToCommunityResponse(community, user.getId());
    }

    public CommunityResponse mapToCommunityResponse(Community c, Long currentUserId) {
        CommunityResponse response = new CommunityResponse();
        response.setId(c.getId());
        response.setName(c.getName());
        response.setDescription(c.getDescription());
        response.setCategory(c.getCategory());
        response.setLocation(c.getLocation());
        response.setCreatorName(c.getCreator() != null ? c.getCreator().getName() : "Unknown");
        response.setCreatorId(c.getCreator() != null ? c.getCreator().getId() : null);
        response.setMemberCount(c.getMemberCount());
        response.setStatus(c.getStatus());
        response.setCreatedAt(c.getCreatedAt());
        if (currentUserId != null) {
            response.setMember(communityMemberRepository.existsByCommunityIdAndUserId(c.getId(), currentUserId));
        } else {
            response.setMember(false);
        }
        return response;
    }
}
