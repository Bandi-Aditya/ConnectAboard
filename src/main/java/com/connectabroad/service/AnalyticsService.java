package com.connectabroad.service;

import com.connectabroad.dto.admin.PlatformAnalyticsResponse;
import com.connectabroad.dto.admin.TimeSeriesDataPoint;
import com.connectabroad.entity.ConnectionStatus;
import com.connectabroad.entity.UserStatus;
import com.connectabroad.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final JobRepository jobRepository;
    private final CommunityRepository communityRepository;
    private final ConnectionRepository connectionRepository;
    private final MessageRepository messageRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentRepository commentRepository;

    public AnalyticsService(UserRepository userRepository,
                            PostRepository postRepository,
                            JobRepository jobRepository,
                            CommunityRepository communityRepository,
                            ConnectionRepository connectionRepository,
                            MessageRepository messageRepository,
                            PostLikeRepository postLikeRepository,
                            CommentRepository commentRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.jobRepository = jobRepository;
        this.communityRepository = communityRepository;
        this.connectionRepository = connectionRepository;
        this.messageRepository = messageRepository;
        this.postLikeRepository = postLikeRepository;
        this.commentRepository = commentRepository;
    }

    public PlatformAnalyticsResponse getPlatformAnalytics(int days) {
        if (days <= 0) days = 30;
        LocalDateTime startDate = LocalDateTime.now().minusDays(days).withHour(0).withMinute(0).withSecond(0);

        List<TimeSeriesDataPoint> userGrowth = formatTimeSeries(userRepository.countNewUsersPerDay(startDate), days);
        List<TimeSeriesDataPoint> postsPerDay = formatTimeSeries(postRepository.countNewPostsPerDay(startDate), days);
        List<TimeSeriesDataPoint> jobsPerDay = formatTimeSeries(jobRepository.countNewJobsPerDay(startDate), days);
        List<TimeSeriesDataPoint> communitiesPerDay = formatTimeSeries(communityRepository.countNewCommunitiesPerDay(startDate), days);
        List<TimeSeriesDataPoint> connectionsPerDay = formatTimeSeries(connectionRepository.countNewConnectionsPerDay(startDate), days);

        long activeUsers = userRepository.countByStatus(UserStatus.ACTIVE);
        long totalPosts = postRepository.count();
        long totalConnections = connectionRepository.countByStatus(ConnectionStatus.ACCEPTED);
        long totalUsers = userRepository.count();

        double avgPostsPerActiveUser = activeUsers > 0 ? (double) totalPosts / activeUsers : 0.0;
        double avgConnectionsPerUser = totalUsers > 0 ? (double) (totalConnections * 2) / totalUsers : 0.0;

        long totalLikes = postLikeRepository.count();
        long totalComments = commentRepository.count();
        long totalMessages = messageRepository.count();

        PlatformAnalyticsResponse response = new PlatformAnalyticsResponse();
        response.setUserGrowth(userGrowth);
        response.setPostsPerDay(postsPerDay);
        response.setJobsPerDay(jobsPerDay);
        response.setCommunitiesPerDay(communitiesPerDay);
        response.setConnectionsPerDay(connectionsPerDay);

        response.setAvgPostsPerActiveUser(Math.round(avgPostsPerActiveUser * 100.0) / 100.0);
        response.setAvgConnectionsPerUser(Math.round(avgConnectionsPerUser * 100.0) / 100.0);
        response.setTotalLikes(totalLikes);
        response.setTotalComments(totalComments);
        response.setTotalMessages(totalMessages);

        return response;
    }

    private List<TimeSeriesDataPoint> formatTimeSeries(List<Object[]> queryResults, int days) {
        Map<String, Long> dateCountMap = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");

        for (Object[] result : queryResults) {
            if (result != null && result.length >= 2) {
                String dateStr = null;
                if (result[0] instanceof java.sql.Date) {
                    dateStr = ((java.sql.Date) result[0]).toLocalDate().format(formatter);
                } else if (result[0] instanceof LocalDate) {
                    dateStr = ((LocalDate) result[0]).format(formatter);
                } else if (result[0] != null) {
                    dateStr = result[0].toString();
                }

                long count = 0;
                if (result[1] instanceof Number) {
                    count = ((Number) result[1]).longValue();
                }

                if (dateStr != null) {
                    dateCountMap.put(dateStr, count);
                }
            }
        }

        List<TimeSeriesDataPoint> points = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String formattedDate = date.format(formatter);
            long count = dateCountMap.getOrDefault(formattedDate, 0L);
            points.add(new TimeSeriesDataPoint(formattedDate, count));
        }

        return points;
    }
}
