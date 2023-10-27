package com.academicblogfptu.AcademicBlogFPTU.repositories;

import com.academicblogfptu.AcademicBlogFPTU.entities.VoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VoteRepository extends JpaRepository<VoteEntity, Integer> {
    List<VoteEntity> findByCommentId(int commentId);
}
