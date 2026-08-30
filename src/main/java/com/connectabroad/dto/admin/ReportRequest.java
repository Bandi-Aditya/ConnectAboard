package com.connectabroad.dto.admin;

import com.connectabroad.entity.ReportReason;
import com.connectabroad.entity.ReportTargetType;
import jakarta.validation.constraints.NotNull;

public class ReportRequest {
    @NotNull(message = "Target type is required")
    private ReportTargetType targetType;

    @NotNull(message = "Target ID is required")
    private Long targetId;

    @NotNull(message = "Report reason is required")
    private ReportReason reason;

    private String description;

    public ReportRequest() {}

    public ReportTargetType getTargetType() { return targetType; }
    public void setTargetType(ReportTargetType targetType) { this.targetType = targetType; }

    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }

    public ReportReason getReason() { return reason; }
    public void setReason(ReportReason reason) { this.reason = reason; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
