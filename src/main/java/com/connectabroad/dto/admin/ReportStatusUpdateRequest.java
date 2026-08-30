package com.connectabroad.dto.admin;

import com.connectabroad.entity.ReportStatus;
import jakarta.validation.constraints.NotNull;

public class ReportStatusUpdateRequest {
    @NotNull(message = "Status is required")
    private ReportStatus status;

    public ReportStatusUpdateRequest() {}

    public ReportStatusUpdateRequest(ReportStatus status) {
        this.status = status;
    }

    public ReportStatus getStatus() { return status; }
    public void setStatus(ReportStatus status) { this.status = status; }
}
