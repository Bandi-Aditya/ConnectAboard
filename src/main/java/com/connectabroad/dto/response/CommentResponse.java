package com.connectabroad.dto.response;

import java.time.LocalDateTime;

public class CommentResponse {

    private Long id;
    private Long postId;
    private AuthorSummaryResponse author;
    private String content;
    private LocalDateTime createdAt;
    private boolean isMine;

    public CommentResponse() {}

    public CommentResponse(Long id, Long postId, AuthorSummaryResponse author, String content, LocalDateTime createdAt, boolean isMine) {
        this.id = id;
        this.postId = postId;
        this.author = author;
        this.content = content;
        this.createdAt = createdAt;
        this.isMine = isMine;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }

    public AuthorSummaryResponse getAuthor() { return author; }
    public void setAuthor(AuthorSummaryResponse author) { this.author = author; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isMine() { return isMine; }
    public void setMine(boolean mine) { isMine = mine; }
}
