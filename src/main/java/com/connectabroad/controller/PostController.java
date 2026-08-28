package com.connectabroad.controller;

import com.connectabroad.dto.request.CreateCommentRequest;
import com.connectabroad.dto.request.CreatePostRequest;
import com.connectabroad.dto.request.UpdatePostRequest;
import com.connectabroad.dto.response.CommentResponse;
import com.connectabroad.dto.response.PageResponse;
import com.connectabroad.dto.response.PostResponse;
import com.connectabroad.service.PostService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping("/posts")
    public ResponseEntity<PostResponse> createPost(
            Authentication authentication,
            @Valid @RequestBody CreatePostRequest request) {
        String userEmail = authentication.getName();
        PostResponse response = postService.createPost(userEmail, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/posts/feed")
    public ResponseEntity<PageResponse<PostResponse>> getFeed(
            Authentication authentication,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        String userEmail = authentication.getName();
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(postService.getFeed(userEmail, pageable));
    }

    @GetMapping("/posts/{id}")
    public ResponseEntity<PostResponse> getPostById(
            Authentication authentication,
            @PathVariable("id") Long postId) {
        String userEmail = (authentication != null) ? authentication.getName() : null;
        return ResponseEntity.ok(postService.getPostById(postId, userEmail));
    }

    @PutMapping("/posts/{id}")
    public ResponseEntity<PostResponse> updatePost(
            Authentication authentication,
            @PathVariable("id") Long postId,
            @Valid @RequestBody UpdatePostRequest request) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(postService.updatePost(postId, userEmail, request));
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Map<String, String>> deletePost(
            Authentication authentication,
            @PathVariable("id") Long postId) {
        String userEmail = authentication.getName();
        postService.deletePost(postId, userEmail);
        return ResponseEntity.ok(Map.of("message", "Post deleted successfully."));
    }

    @PostMapping("/posts/{id}/like")
    public ResponseEntity<Map<String, String>> likePost(
            Authentication authentication,
            @PathVariable("id") Long postId) {
        String userEmail = authentication.getName();
        postService.likePost(postId, userEmail);
        return ResponseEntity.ok(Map.of("message", "Post liked."));
    }

    @DeleteMapping("/posts/{id}/like")
    public ResponseEntity<Map<String, String>> unlikePost(
            Authentication authentication,
            @PathVariable("id") Long postId) {
        String userEmail = authentication.getName();
        postService.unlikePost(postId, userEmail);
        return ResponseEntity.ok(Map.of("message", "Post unliked."));
    }

    @PostMapping("/posts/{id}/comments")
    public ResponseEntity<CommentResponse> addComment(
            Authentication authentication,
            @PathVariable("id") Long postId,
            @Valid @RequestBody CreateCommentRequest request) {
        String userEmail = authentication.getName();
        CommentResponse response = postService.addComment(postId, userEmail, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/posts/{id}/comments")
    public ResponseEntity<PageResponse<CommentResponse>> getComments(
            Authentication authentication,
            @PathVariable("id") Long postId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        String userEmail = (authentication != null) ? authentication.getName() : null;
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(postService.getComments(postId, userEmail, pageable));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Map<String, String>> deleteComment(
            Authentication authentication,
            @PathVariable("commentId") Long commentId) {
        String userEmail = authentication.getName();
        postService.deleteComment(commentId, userEmail);
        return ResponseEntity.ok(Map.of("message", "Comment deleted successfully."));
    }
}
