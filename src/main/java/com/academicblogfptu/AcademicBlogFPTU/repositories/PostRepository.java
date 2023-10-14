package com.academicblogfptu.AcademicBlogFPTU.repositories;

import com.academicblogfptu.AcademicBlogFPTU.entities.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<PostEntity, Integer> {



}
