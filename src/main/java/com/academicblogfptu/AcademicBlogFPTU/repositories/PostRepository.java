package com.academicblogfptu.AcademicBlogFPTU.repositories;

import com.academicblogfptu.AcademicBlogFPTU.dtos.PostDtos.PostListDto;
import com.academicblogfptu.AcademicBlogFPTU.entities.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<PostEntity, Integer> {
    Optional<PostEntity> findById(int id);

    Optional<PostEntity> findBySlug(String slug);
    Long countByUserId(int id);

    @Query(value = "SELECT COUNT(*) FROM post p JOIN post_details pd ON p.id = pd.post_id WHERE p.account_id = :userId AND pd.type = :type", nativeQuery = true)
    long countByUserIdAndType(int userId, String type);

    @Query(value = "SELECT p.id, p.title, p.content, p.date_of_post, p.num_of_upvote, p.num_of_downvote," +
            "p.is_rewarded, p.is_edited, p.allow_comment, p.length, p.account_id, p.category_id, p.tag_id," +
            "p.parent_id, p.description, p.cover_URL, p.slug" +
            " FROM post p " +
            "JOIN post_details pd ON p.id = pd.post_id " +
            "WHERE pd.type = 'Approve' " +
            "AND (:categoryId IS NULL OR p.category_id = :categoryId) " +
            "AND (:tagId IS NULL OR p.tag_id = :tagId) " +
            "AND (:title IS NULL OR p.title LIKE CONCAT('%', :title, '%'))", nativeQuery = true)
    List<PostEntity> findPostsByCategoryIdAndTagIdAndTitle(Integer categoryId, Integer tagId, String title);

}
