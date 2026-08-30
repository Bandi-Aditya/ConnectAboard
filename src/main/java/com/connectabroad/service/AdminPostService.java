package com.connectabroad.service;

import com.connectabroad.dto.response.PageResponse;
import com.connectabroad.dto.response.PostResponse;
import com.connectabroad.entity.Post;
import com.connectabroad.entity.User;
import com.connectabroad.exception.ResourceNotFoundException;
import com.connectabroad.repository.PostRepository;
import com.connectabroad.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AdminPostService {

    private final PostRepository postRepository;
    private final PostService postService;
    private final UserRepository userRepository;
    private final AdminAuditLogService auditLogService;

    public AdminPostService(PostRepository postRepository,
                            PostService postService,
                            UserRepository userRepository,
                            AdminAuditLogService auditLogService) {
        this.postRepository = postRepository;
        this.postService = postService;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public PageResponse<PostResponse> getAdminPosts(String keyword, Pageable pageable) {
        Page<Post> page = postRepository.findAdminPosts(keyword, pageable);
        return PageResponse.from(page.map(post -> postService.mapToPostResponse(post, null)));
    }

    public void removePost(String adminEmail, Long postId) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with email: " + adminEmail));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        String authorEmail = post.getAuthor() != null ? post.getAuthor().getEmail() : "unknown";
        postRepository.delete(post);

        auditLogService.logAction(
                admin,
                "DELETE_POST",
                "POST",
                postId,
                "Deleted post #" + postId + " by author " + authorEmail
        );
    }
}
