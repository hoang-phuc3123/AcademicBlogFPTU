package com.academicblogfptu.AcademicBlogFPTU.repositories;

import com.academicblogfptu.AcademicBlogFPTU.entities.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<PostEntity, Integer> {
    Optional<PostEntity> findById(int id);

    List<PostEntity> findByUserId(int userId);

    PostEntity findByParentPost(PostEntity parentPost);

    List<PostEntity> findPostsByParentPost(PostEntity parentPost);
    Optional<PostEntity> findBySlug(String slug);
    Long countByUserId(int id);

    @Query(value = "SELECT COUNT(*) FROM post p JOIN post_details pd ON p.id = pd.post_id WHERE p.account_id = :userId AND pd.type = :type", nativeQuery = true)
    long countByUserIdAndType(int userId, String type);

    @Query(value =
            "SELECT p.id, p.title, p.content, p.date_of_post, p.num_of_upvote, p.num_of_downvote, " +
                    "p.is_rewarded, p.is_edited, p.allow_comment, p.length, p.account_id, p.category_id, " +
                    "p.tag_id, p.parent_id, p.description, p.cover_URL, p.slug " +
                    "FROM post p " +
                    "JOIN post_details pd ON p.id = pd.post_id " +
                    "WHERE pd.type = 'Approve' " +
                    "AND (:categoryId IS NULL " +
                    "OR (p.category_id = :categoryId OR p.category_id IN " +
                    "(SELECT c.id FROM category c WHERE c.parent_id = :categoryId))) " +
                    "AND (:tagId IS NULL OR p.tag_id = :tagId) " +
                    "AND (:title IS NULL OR p.title LIKE CONCAT('%', :title, '%'))", nativeQuery = true)
    List<PostEntity> findPostsByCategoryIdAndTagIdAndTitle(Integer categoryId, Integer tagId, String title);

    @Query(value =
            "SELECT p.id, p.title, p.content, p.date_of_post, p.num_of_upvote, p.num_of_downvote, " +
                    "p.is_rewarded, p.is_edited, p.allow_comment, p.length, p.account_id, p.category_id, " +
                    "p.tag_id, p.parent_id, p.description, p.cover_URL, p.slug " +
                    "FROM post p " +
                    "JOIN post_details pd ON p.id = pd.post_id " +
                    "WHERE pd.type = 'Approve' " +
                    "AND (:categoryId IS NULL " +
                    "OR (p.category_id IN " +
                    "(SELECT c.id FROM category c WHERE c.parent_id IN (SELECT c.id FROM category c WHERE c.parent_id = :categoryId)))) " +
                    "AND (:tagId IS NULL OR p.tag_id = :tagId) " +
                    "AND (:title IS NULL OR p.title LIKE CONCAT('%', :title, '%'))", nativeQuery = true)
    List<PostEntity> findPostsByCategoryIdsAndTagIdAndTitle(Integer categoryId, Integer tagId, String title);

    @Query(value = "SELECT p.id, p.title, p.content, p.date_of_post, p.num_of_upvote, p.num_of_downvote, " +
            "p.is_rewarded, p.is_edited, p.allow_comment, p.length, p.account_id, p.category_id, p.tag_id, " +
            "p.parent_id, p.description, p.cover_URL, p.slug " +
            "FROM post p " +
            "JOIN post_details pd ON p.id = pd.post_id " +
            "WHERE pd.type = 'Approve' and p.tag_id in (2,3,4) " +
            "ORDER BY (p.num_of_upvote - p.num_of_downVote) desc, p.date_of_post desc " +
            "OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY", nativeQuery = true)
    List<PostEntity> findPostsPaged(int offset, int limit);

    @Query(value = "SELECT * FROM post p " +
            "WHERE p.date_of_post >= DATEADD(day, -7, GETDATE())",
            nativeQuery = true)
    List<PostEntity> findPostForLast7Days();

    @Query(value = "SELECT COUNT(*) FROM post p JOIN post_details pd ON p.id = pd.post_id WHERE pd.type = 'Approve'", nativeQuery = true)
    int countTotalPost();

    @Query(value = "SELECT DISTINCT p.* FROM post p " +
            "JOIN post_details pd ON p.id = pd.post_id " +
            "WHERE (p.category_id IN (:categoryIds)) And " +
            "(p.tag_id IN (:tagIds)) AND pd.type = 'Approve' And " +
            "(:title IS NULL OR p.title LIKE CONCAT('%', :title, '%'))", nativeQuery = true)
    List<PostEntity> findByCategoriesAndTagsAndTitle(List<Integer> categoryIds, List<Integer> tagIds,String title);

    @Query(value = "SELECT DISTINCT p.* FROM post p " +
            "JOIN post_details pd ON p.id = pd.post_id " +
            "WHERE " +
            "(p.tag_id IN (:tagIds)) AND pd.type = 'Approve' And " +
            "(:title IS NULL OR p.title LIKE CONCAT('%', :title, '%'))", nativeQuery = true)
    List<PostEntity> findByTagsAndTitle(List<Integer> tagIds,String title);

    @Query(value = "SELECT DISTINCT p.* FROM post p " +
            "JOIN post_details pd ON p.id = pd.post_id " +
            "WHERE (p.category_id IN (:categoryIds)) And " +
            "pd.type = 'Approve' And " +
            "(:title IS NULL OR p.title LIKE CONCAT('%', :title, '%'))", nativeQuery = true)
    List<PostEntity> findByCategoriesAndTitle(List<Integer> categoryIds, String title);

    @Query(value = "SELECT DISTINCT p.* FROM post p " +
            "JOIN post_details pd ON p.id = pd.post_id " +
            "WHERE " +
            "pd.type = 'Approve' And " +
            "(:title IS NULL OR p.title LIKE CONCAT('%', :title, '%'))", nativeQuery = true)
    List<PostEntity> findByTitle(String title);


    @Query(value = "SELECT p.id, p.title, p.content, p.date_of_post, p.num_of_upvote, p.num_of_downvote, " +
            "p.is_rewarded, p.is_edited, p.allow_comment, p.length, p.account_id, p.category_id, p.tag_id, " +
            "p.parent_id, p.description, p.cover_URL, p.slug " +
            "FROM post p " +
            "JOIN post_details pd ON p.id = pd.post_id " +
            "WHERE pd.type = 'Approve' ", nativeQuery = true)
    List<PostEntity> getAllApprovedPost();

    @Query(value = "SELECT p.id, p.title, p.content, p.date_of_post, p.num_of_upvote, p.num_of_downvote, " +
            "p.is_rewarded, p.is_edited, p.allow_comment, p.length, p.account_id, p.category_id, p.tag_id, " +
            "p.parent_id, p.description, p.cover_URL, p.slug " +
            "FROM post p " +
            "JOIN post_details pd ON p.id = pd.post_id " +
            "WHERE pd.type = 'Request' ", nativeQuery = true)
    List<PostEntity> getAllPendingPost();

    @Query(value =
            "SELECT p.id, p.title, p.content, p.date_of_post, p.num_of_upvote, p.num_of_downvote, " +
                    "p.is_rewarded, p.is_edited, p.allow_comment, p.length, p.account_id, p.category_id, " +
                    "p.tag_id, p.parent_id, p.description, p.cover_URL, p.slug " +
                    "FROM post_details pd " +
                    "JOIN post p join post_skill ps " +
                    "ON p.id = ps.post_id " +
                    "ON p.id = pd.post_id " +
                    "WHERE pd.type = 'Approve' " +
                    "AND ps.skill_id = :skillId" , nativeQuery = true)
    List<PostEntity> findPostBySkill(int skillId);
}
