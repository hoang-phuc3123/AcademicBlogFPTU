package com.academicblogfptu.AcademicBlogFPTU.repositories;

import com.academicblogfptu.AcademicBlogFPTU.entities.CommentEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.UserDetailsEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<CommentEntity, Integer> {

    List<CommentEntity> findByParentComment(CommentEntity parentComment);

    List<CommentEntity> findByPostIdAndParentCommentIsNull(Integer postId);

    List<CommentEntity> findByPostId(Integer postId);

    @Query(value = "SELECT count(*) FROM comment WHERE post_id = :postId", nativeQuery = true)
    int countNumOfCommentForPost(int postId);
}
