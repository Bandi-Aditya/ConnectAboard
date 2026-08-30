package com.connectabroad.repository;

import com.connectabroad.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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

    @Query("SELECT p FROM Post p WHERE " +
           "(:keyword IS NULL OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.author.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY p.createdAt DESC")
    Page<Post> findAdminPosts(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT CAST(p.createdAt AS date) as date, COUNT(p) as count " +
           "FROM Post p WHERE p.createdAt >= :startDate " +
           "GROUP BY CAST(p.createdAt AS date) ORDER BY CAST(p.createdAt AS date)")
    List<Object[]> countNewPostsPerDay(@Param("startDate") LocalDateTime startDate);
}
