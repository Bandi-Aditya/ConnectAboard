package com.connectabroad.dto.response;

import com.connectabroad.entity.PostType;
import java.time.LocalDateTime;

public class PostResponse {

    private Long id;
    private AuthorSummaryResponse author;
    private String content;
    private String imageUrl;
    private PostType postType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private long likeCount;
    private long commentCount;
    private boolean likedByCurrentUser;
    private boolean isMine;

    public PostResponse() {}

    public PostResponse(Long id, AuthorSummaryResponse author, String content, String imageUrl,
                        PostType postType, LocalDateTime createdAt, LocalDateTime updatedAt,
                        long likeCount, long commentCount, boolean likedByCurrentUser, boolean isMine) {
        this.id = id;
        this.author = author;
        this.content = content;
        this.imageUrl = imageUrl;
        this.postType = postType;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.likedByCurrentUser = likedByCurrentUser;
        this.isMine = isMine;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public AuthorSummaryResponse getAuthor() { return author; }
    public void setAuthor(AuthorSummaryResponse author) { this.author = author; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public PostType getPostType() { return postType; }
    public void setPostType(PostType postType) { this.postType = postType; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public long getLikeCount() { return likeCount; }
    public void setLikeCount(long likeCount) { this.likeCount = likeCount; }

    public long getCommentCount() { return commentCount; }
    public void setCommentCount(long commentCount) { this.commentCount = commentCount; }

    public boolean isLikedByCurrentUser() { return likedByCurrentUser; }
    public void setLikedByCurrentUser(boolean likedByCurrentUser) { this.likedByCurrentUser = likedByCurrentUser; }

    public boolean isMine() { return isMine; }
    public void setMine(boolean mine) { isMine = mine; }
}
