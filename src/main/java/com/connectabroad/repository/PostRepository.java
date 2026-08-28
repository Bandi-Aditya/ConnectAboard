package com.connectabroad.repository;

import com.connectabroad.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p WHERE p.author.id = :userId OR p.author.id IN :connectedUserIds ORDER BY p.createdAt DESC")
    Page<Post> findFeedPostsForUser(
            @Param("userId") Long userId,
            @Param("connectedUserIds") List<Long> connectedUserIds,
            Pageable pageable
    );

    Page<Post> findByAuthorIdOrderByCreatedAtDesc(Long authorId, Pageable pageable);

    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
