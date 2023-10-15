package com.academicblogfptu.AcademicBlogFPTU.repositories;

import com.academicblogfptu.AcademicBlogFPTU.entities.PostDetailsEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostDetailsRepository extends JpaRepository<PostDetailsEntity, Integer> {

    Optional<PostDetailsEntity> findByPostId(int id);
}

