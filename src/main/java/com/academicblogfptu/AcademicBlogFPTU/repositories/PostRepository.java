package com.academicblogfptu.AcademicBlogFPTU.repositories;

import com.academicblogfptu.AcademicBlogFPTU.entities.PostDetailsEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostRepository extends JpaRepository<PostEntity, PostDetailsEntity> {
    Optional<PostEntity> findById(int id);

}
