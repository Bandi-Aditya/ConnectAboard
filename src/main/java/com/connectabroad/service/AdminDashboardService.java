package com.connectabroad.service;

import com.connectabroad.dto.admin.DashboardStatsResponse;
import com.connectabroad.dto.admin.RecentActivityResponse;
import com.connectabroad.entity.*;
import com.connectabroad.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final ConnectionRepository connectionRepository;
    private final PostRepository postRepository;
    private final JobRepository jobRepository;
    private final CommunityRepository communityRepository;
    private final MessageRepository messageRepository;
    private final NotificationRepository notificationRepository;
    private final ReportRepository reportRepository;

    public AdminDashboardService(UserRepository userRepository,
                                 ConnectionRepository connectionRepository,
                                 PostRepository postRepository,
                                 JobRepository jobRepository,
                                 CommunityRepository communityRepository,
                                 MessageRepository messageRepository,
                                 NotificationRepository notificationRepository,
                                 ReportRepository reportRepository) {
        this.userRepository = userRepository;
        this.connectionRepository = connectionRepository;
        this.postRepository = postRepository;
        this.jobRepository = jobRepository;
        this.communityRepository = communityRepository;
        this.messageRepository = messageRepository;
        this.notificationRepository = notificationRepository;
        this.reportRepository = reportRepository;
    }

    public DashboardStatsResponse getDashboardStats() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByStatus(UserStatus.ACTIVE);
        long totalConnections = connectionRepository.countByStatus(ConnectionStatus.ACCEPTED);
        long totalPosts = postRepository.count();
        long totalJobs = jobRepository.count();
        long totalCommunities = communityRepository.count();
        long totalMessages = messageRepository.count();
        long totalNotifications = notificationRepository.count();
        long pendingReports = reportRepository.countByStatus(ReportStatus.PENDING);

        return new DashboardStatsResponse(
                totalUsers,
                activeUsers,
                totalConnections,
                totalPosts,
                totalJobs,
                totalCommunities,
                totalMessages,
                totalNotifications,
                pendingReports
        );
    }

    public List<RecentActivityResponse> getRecentActivity() {
        List<RecentActivityResponse> activities = new ArrayList<>();

        // Fetch recent users
        userRepository.findAll(PageRequest.of(0, 5)).getContent().forEach(user -> {
            activities.add(new RecentActivityResponse(
                    "USER_" + user.getId(),
                    "USER",
                    "👤 " + user.getName() + " joined ConnectAbroad",
                    user.getCreatedAt()
            ));
        });

        // Fetch recent posts
        postRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 5)).getContent().forEach(post -> {
            String authorName = post.getAuthor() != null ? post.getAuthor().getName() : "A user";
            activities.add(new RecentActivityResponse(
                    "POST_" + post.getId(),
                    "POST",
                    "📝 New post created by " + authorName,
                    post.getCreatedAt()
            ));
        });

        // Fetch recent jobs
        jobRepository.findAll(PageRequest.of(0, 5)).getContent().forEach(job -> {
            activities.add(new RecentActivityResponse(
                    "JOB_" + job.getId(),
                    "JOB",
                    "💼 New job posted: " + job.getTitle() + " in " + job.getCity(),
                    job.getCreatedAt()
            ));
        });

        // Fetch recent communities
        communityRepository.findAll(PageRequest.of(0, 5)).getContent().forEach(community -> {
            activities.add(new RecentActivityResponse(
                    "COMMUNITY_" + community.getId(),
                    "COMMUNITY",
                    "👥 New community created: " + community.getName(),
                    community.getCreatedAt()
            ));
        });

        // Fetch recent reports
        reportRepository.findAll(PageRequest.of(0, 5)).getContent().forEach(report -> {
            String reporterName = report.getReporter() != null ? report.getReporter().getName() : "A user";
            activities.add(new RecentActivityResponse(
                    "REPORT_" + report.getId(),
                    "REPORT",
                    "🚨 New report submitted by " + reporterName + " (" + report.getReason() + ")",
                    report.getCreatedAt()
            ));
        });

        activities.sort(Comparator.comparing(RecentActivityResponse::getTimestamp, Comparator.nullsLast(Comparator.reverseOrder())));
        if (activities.size() > 10) {
            return activities.subList(0, 10);
        }
        return activities;
    }
}
