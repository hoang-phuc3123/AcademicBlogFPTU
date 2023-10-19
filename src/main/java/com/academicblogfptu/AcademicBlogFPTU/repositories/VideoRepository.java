package com.academicblogfptu.AcademicBlogFPTU.repositories;

import com.academicblogfptu.AcademicBlogFPTU.entities.VideoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VideoRepository extends JpaRepository<VideoEntity, Integer> {

    List<VideoEntity> findByPostId(int postId);
}
