package com.connectabroad.controller;

import com.connectabroad.dto.admin.CommunityResponse;
import com.connectabroad.dto.admin.CreateCommunityRequest;
import com.connectabroad.dto.response.PageResponse;
import com.connectabroad.service.CommunityService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/communities")
public class CommunityController {

    private final CommunityService communityService;

    public CommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<CommunityResponse>> getCommunities(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        String userEmail = userDetails != null ? userDetails.getUsername() : null;
        PageResponse<CommunityResponse> response = communityService.getCommunities(userEmail, pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<CommunityResponse> createCommunity(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateCommunityRequest request) {
        
        CommunityResponse response = communityService.createCommunity(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<CommunityResponse> joinCommunity(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        
        CommunityResponse response = communityService.joinCommunity(userDetails.getUsername(), id);
        return ResponseEntity.ok(response);
    }
}
