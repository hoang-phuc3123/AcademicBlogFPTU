package com.academicblogfptu.AcademicBlogFPTU.repositories;

import com.academicblogfptu.AcademicBlogFPTU.entities.VoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VoteRepository extends JpaRepository<VoteEntity, Integer> {
    List<VoteEntity> findByCommentId(int commentId);
    List<VoteEntity> findByPostId(int postId);

    Optional<VoteEntity> findByUserIdAndCommentId(int userId,int commentId);

    Optional<VoteEntity> findById(int id);
    List<VoteEntity> findByPostIdAndUserId(int postId,int userId);

    Optional<VoteEntity> findByPostIdAndUserIdAndCommentIdIsNull(int postId,int userId);
    List<VoteEntity> findByPostIdAndUserIdAndCommentId(Integer postId,Integer userId,Integer commentId);


}
