package com.connectabroad.dto.admin;

import java.util.List;

public class PlatformAnalyticsResponse {
    private List<TimeSeriesDataPoint> userGrowth;
    private List<TimeSeriesDataPoint> postsPerDay;
    private List<TimeSeriesDataPoint> jobsPerDay;
    private List<TimeSeriesDataPoint> communitiesPerDay;
    private List<TimeSeriesDataPoint> connectionsPerDay;

    private double avgPostsPerActiveUser;
    private double avgConnectionsPerUser;
    private long totalLikes;
    private long totalComments;
    private long totalMessages;

    public PlatformAnalyticsResponse() {}

    public List<TimeSeriesDataPoint> getUserGrowth() { return userGrowth; }
    public void setUserGrowth(List<TimeSeriesDataPoint> userGrowth) { this.userGrowth = userGrowth; }

    public List<TimeSeriesDataPoint> getPostsPerDay() { return postsPerDay; }
    public void setPostsPerDay(List<TimeSeriesDataPoint> postsPerDay) { this.postsPerDay = postsPerDay; }

    public List<TimeSeriesDataPoint> getJobsPerDay() { return jobsPerDay; }
    public void setJobsPerDay(List<TimeSeriesDataPoint> jobsPerDay) { this.jobsPerDay = jobsPerDay; }

    public List<TimeSeriesDataPoint> getCommunitiesPerDay() { return communitiesPerDay; }
    public void setCommunitiesPerDay(List<TimeSeriesDataPoint> communitiesPerDay) { this.communitiesPerDay = communitiesPerDay; }

    public List<TimeSeriesDataPoint> getConnectionsPerDay() { return connectionsPerDay; }
    public void setConnectionsPerDay(List<TimeSeriesDataPoint> connectionsPerDay) { this.connectionsPerDay = connectionsPerDay; }

    public double getAvgPostsPerActiveUser() { return avgPostsPerActiveUser; }
    public void setAvgPostsPerActiveUser(double avgPostsPerActiveUser) { this.avgPostsPerActiveUser = avgPostsPerActiveUser; }

    public double getAvgConnectionsPerUser() { return avgConnectionsPerUser; }
    public void setAvgConnectionsPerUser(double avgConnectionsPerUser) { this.avgConnectionsPerUser = avgConnectionsPerUser; }

    public long getTotalLikes() { return totalLikes; }
    public void setTotalLikes(long totalLikes) { this.totalLikes = totalLikes; }

    public long getTotalComments() { return totalComments; }
    public void setTotalComments(long totalComments) { this.totalComments = totalComments; }

    public long getTotalMessages() { return totalMessages; }
    public void setTotalMessages(long totalMessages) { this.totalMessages = totalMessages; }
}
