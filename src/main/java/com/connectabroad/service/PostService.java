package com.connectabroad.service;

import com.connectabroad.dto.request.CreateCommentRequest;
import com.connectabroad.dto.request.CreatePostRequest;
import com.connectabroad.dto.request.UpdatePostRequest;
import com.connectabroad.dto.response.AuthorSummaryResponse;
import com.connectabroad.dto.response.CommentResponse;
import com.connectabroad.dto.response.PageResponse;
import com.connectabroad.dto.response.PostResponse;
import com.connectabroad.entity.*;
import com.connectabroad.exception.ResourceNotFoundException;
import com.connectabroad.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final ConnectionRepository connectionRepository;
    private final NotificationService notificationService;

    public PostService(PostRepository postRepository,
                       PostLikeRepository postLikeRepository,
                       CommentRepository commentRepository,
                       UserRepository userRepository,
                       ProfileRepository profileRepository,
                       ConnectionRepository connectionRepository,
                       NotificationService notificationService) {
        this.postRepository = postRepository;
        this.postLikeRepository = postLikeRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.connectionRepository = connectionRepository;
        this.notificationService = notificationService;
    }

    public PostResponse createPost(String userEmail, CreatePostRequest request) {
        User author = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Post post = new Post(author, request.getContent(), request.getImageUrl(), request.getPostType());
        Post savedPost = postRepository.save(post);
        return mapToPostResponse(savedPost, author.getId());
    }

    @Transactional(readOnly = true)
    public PageResponse<PostResponse> getFeed(String userEmail, Pageable pageable) {
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        List<Long> connectedUserIds = connectionRepository.findConnectedUserIds(currentUser.getId());

        Page<Post> page;
        if (connectedUserIds.isEmpty()) {
            page = postRepository.findAllByOrderByCreatedAtDesc(pageable);
        } else {
            page = postRepository.findFeedPostsForUser(currentUser.getId(), connectedUserIds, pageable);
        }

        List<PostResponse> content = page.getContent().stream()
                .map(p -> mapToPostResponse(p, currentUser.getId()))
                .toList();

        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<PostResponse> getPostsByAuthor(Long authorId, String currentUserEmail, Pageable pageable) {
        User currentUser = currentUserEmail != null ? userRepository.findByEmail(currentUserEmail).orElse(null) : null;
        Long currentUserId = currentUser != null ? currentUser.getId() : null;

        Page<Post> page = postRepository.findByAuthorIdOrderByCreatedAtDesc(authorId, pageable);

        List<PostResponse> content = page.getContent().stream()
                .map(p -> mapToPostResponse(p, currentUserId))
                .toList();

        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    @Transactional(readOnly = true)
    public PostResponse getPostById(Long postId, String currentUserEmail) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with ID: " + postId));

        User currentUser = currentUserEmail != null ? userRepository.findByEmail(currentUserEmail).orElse(null) : null;
        Long currentUserId = currentUser != null ? currentUser.getId() : null;

        return mapToPostResponse(post, currentUserId);
    }

    public PostResponse updatePost(Long postId, String userEmail, UpdatePostRequest request) {
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with ID: " + postId));

        if (!post.getAuthor().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to edit this post.");
        }

        if (request.getContent() != null && !request.getContent().trim().isEmpty()) {
            post.setContent(request.getContent());
        }
        if (request.getPostType() != null) {
            post.setPostType(request.getPostType());
        }
        if (request.getImageUrl() != null) {
            post.setImageUrl(request.getImageUrl());
        }

        Post updated = postRepository.save(post);
        return mapToPostResponse(updated, currentUser.getId());
    }

    public void deletePost(Long postId, String userEmail) {
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with ID: " + postId));

        if (!post.getAuthor().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to delete this post.");
        }

        postRepository.delete(post);
    }

    public void likePost(Long postId, String userEmail) {
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with ID: " + postId));

        if (!postLikeRepository.existsByPostIdAndUserId(postId, currentUser.getId())) {
            postLikeRepository.save(new PostLike(post, currentUser));

            if (!post.getAuthor().getId().equals(currentUser.getId())) {
                notificationService.createNotification(
                        post.getAuthor(),
                        currentUser,
                        NotificationType.POST_LIKE,
                        "Post Liked",
                        currentUser.getName() + " liked your post.",
                        post.getId(),
                        ReferenceType.POST
                );
            }
        }
    }

    public void unlikePost(Long postId, String userEmail) {
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        postLikeRepository.deleteByPostIdAndUserId(postId, currentUser.getId());
    }

    public CommentResponse addComment(Long postId, String userEmail, CreateCommentRequest request) {
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with ID: " + postId));

        Comment comment = new Comment(post, currentUser, request.getContent());
        Comment savedComment = commentRepository.save(comment);

        if (!post.getAuthor().getId().equals(currentUser.getId())) {
            notificationService.createNotification(
                    post.getAuthor(),
                    currentUser,
                    NotificationType.POST_COMMENT,
                    "New Comment",
                    currentUser.getName() + " commented on your post.",
                    post.getId(),
                    ReferenceType.POST
            );
        }

        return mapToCommentResponse(savedComment, currentUser.getId());
    }

    @Transactional(readOnly = true)
    public PageResponse<CommentResponse> getComments(Long postId, String currentUserEmail, Pageable pageable) {
        User currentUser = currentUserEmail != null ? userRepository.findByEmail(currentUserEmail).orElse(null) : null;
        Long currentUserId = currentUser != null ? currentUser.getId() : null;

        Page<Comment> page = commentRepository.findByPostIdOrderByCreatedAtAsc(postId, pageable);

        List<CommentResponse> content = page.getContent().stream()
                .map(c -> mapToCommentResponse(c, currentUserId))
                .toList();

        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    public void deleteComment(Long commentId, String userEmail) {
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with ID: " + commentId));

        if (!comment.getAuthor().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to delete this comment.");
        }

        commentRepository.delete(comment);
    }

    public PostResponse mapToPostResponse(Post post, Long currentUserId) {
        AuthorSummaryResponse authorSummary = buildAuthorSummary(post.getAuthor());

        long likeCount = postLikeRepository.countByPostId(post.getId());
        long commentCount = commentRepository.countByPostId(post.getId());

        boolean likedByCurrentUser = false;
        boolean isMine = false;

        if (currentUserId != null) {
            likedByCurrentUser = postLikeRepository.existsByPostIdAndUserId(post.getId(), currentUserId);
            isMine = post.getAuthor().getId().equals(currentUserId);
        }

        return new PostResponse(
                post.getId(),
                authorSummary,
                post.getContent(),
                post.getImageUrl(),
                post.getPostType(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                likeCount,
                commentCount,
                likedByCurrentUser,
                isMine
        );
    }

    private CommentResponse mapToCommentResponse(Comment comment, Long currentUserId) {
        AuthorSummaryResponse authorSummary = buildAuthorSummary(comment.getAuthor());
        boolean isMine = currentUserId != null && comment.getAuthor().getId().equals(currentUserId);

        return new CommentResponse(
                comment.getId(),
                comment.getPost().getId(),
                authorSummary,
                comment.getContent(),
                comment.getCreatedAt(),
                isMine
        );
    }

    private AuthorSummaryResponse buildAuthorSummary(User user) {
        Optional<Profile> profOpt = profileRepository.findByUserId(user.getId());
        Profile prof = profOpt.orElse(null);

        String photo = prof != null ? prof.getProfilePhoto() : null;
        String profession = prof != null ? prof.getProfession() : "Community Member";
        String city = prof != null ? prof.getCurrentCity() : null;
        String country = prof != null ? prof.getCurrentCountry() : null;
        String college = (prof != null && prof.getCollege() != null) ? prof.getCollege().getName() : null;

        return new AuthorSummaryResponse(
                user.getId(),
                user.getName(),
                photo,
                profession,
                city,
                country,
                college,
                user.getUserType()
        );
    }
}
