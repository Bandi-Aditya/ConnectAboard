package com.connectabroad.dto.request;

import com.connectabroad.entity.PostType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdatePostRequest {

    @NotBlank(message = "Post content cannot be empty")
    @Size(max = 2000, message = "Post content cannot exceed 2000 characters")
    private String content;

    private PostType postType;

    @Size(max = 1000, message = "Image URL cannot exceed 1000 characters")
    private String imageUrl;

    public UpdatePostRequest() {}

    public UpdatePostRequest(String content, PostType postType, String imageUrl) {
        this.content = content;
        this.postType = postType;
        this.imageUrl = imageUrl;
    }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public PostType getPostType() { return postType; }
    public void setPostType(PostType postType) { this.postType = postType; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
