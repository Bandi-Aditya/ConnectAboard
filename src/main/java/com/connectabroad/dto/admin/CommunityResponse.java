package com.connectabroad.dto.admin;

import com.connectabroad.entity.CommunityStatus;
import java.time.LocalDateTime;

public class CommunityResponse {
    private Long id;
    private String name;
    private String description;
    private String category;
    private String location;
    private String creatorName;
    private Long creatorId;
    private long memberCount;
    private CommunityStatus status;
    private LocalDateTime createdAt;
    private boolean isMember;

    public CommunityResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }

    public Long getCreatorId() { return creatorId; }
    public void setCreatorId(Long creatorId) { this.creatorId = creatorId; }

    public long getMemberCount() { return memberCount; }
    public void setMemberCount(long memberCount) { this.memberCount = memberCount; }

    public CommunityStatus getStatus() { return status; }
    public void setStatus(CommunityStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isMember() { return isMember; }
    public void setMember(boolean member) { isMember = member; }
}
