package com.connectabroad.dto.admin;

public class DashboardStatsResponse {
    private long totalUsers;
    private long activeUsers;
    private long totalConnections;
    private long totalPosts;
    private long totalJobs;
    private long totalCommunities;
    private long totalMessages;
    private long totalNotifications;
    private long pendingReports;

    public DashboardStatsResponse() {}

    public DashboardStatsResponse(long totalUsers, long activeUsers, long totalConnections,
                                  long totalPosts, long totalJobs, long totalCommunities,
                                  long totalMessages, long totalNotifications, long pendingReports) {
        this.totalUsers = totalUsers;
        this.activeUsers = activeUsers;
        this.totalConnections = totalConnections;
        this.totalPosts = totalPosts;
        this.totalJobs = totalJobs;
        this.totalCommunities = totalCommunities;
        this.totalMessages = totalMessages;
        this.totalNotifications = totalNotifications;
        this.pendingReports = pendingReports;
    }

    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }

    public long getActiveUsers() { return activeUsers; }
    public void setActiveUsers(long activeUsers) { this.activeUsers = activeUsers; }

    public long getTotalConnections() { return totalConnections; }
    public void setTotalConnections(long totalConnections) { this.totalConnections = totalConnections; }

    public long getTotalPosts() { return totalPosts; }
    public void setTotalPosts(long totalPosts) { this.totalPosts = totalPosts; }

    public long getTotalJobs() { return totalJobs; }
    public void setTotalJobs(long totalJobs) { this.totalJobs = totalJobs; }

    public long getTotalCommunities() { return totalCommunities; }
    public void setTotalCommunities(long totalCommunities) { this.totalCommunities = totalCommunities; }

    public long getTotalMessages() { return totalMessages; }
    public void setTotalMessages(long totalMessages) { this.totalMessages = totalMessages; }

    public long getTotalNotifications() { return totalNotifications; }
    public void setTotalNotifications(long totalNotifications) { this.totalNotifications = totalNotifications; }

    public long getPendingReports() { return pendingReports; }
    public void setPendingReports(long pendingReports) { this.pendingReports = pendingReports; }
}
