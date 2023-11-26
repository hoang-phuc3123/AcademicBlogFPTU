package com.academicblogfptu.AcademicBlogFPTU.repositories;

import com.academicblogfptu.AcademicBlogFPTU.entities.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<NotificationEntity,Integer> {
    Optional<NotificationEntity> findByUserId(Integer userId);

    List<NotificationEntity> findAllByUserId(Integer userId);

    List<NotificationEntity> findByCommentId(int commentId);

    @Query(value = "SELECT DISTINCT p.* FROM notification p " +
            "WHERE (p.type = :type) AND " +
            "(p.account_id = :userId) AND " +
            "(p.related_id = :relatedId) AND " +
            "(p.content LIKE CONCAT('%', :content, '%'))", nativeQuery = true)
    List<NotificationEntity> findByUserIdAndContentAndRelatedIdAndType(int userId,int relatedId,String type,String content);
}
