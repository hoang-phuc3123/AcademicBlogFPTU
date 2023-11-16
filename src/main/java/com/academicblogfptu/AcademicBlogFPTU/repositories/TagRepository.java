package com.academicblogfptu.AcademicBlogFPTU.repositories;

import com.academicblogfptu.AcademicBlogFPTU.entities.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagRepository extends JpaRepository<TagEntity,Integer> {
    Optional<TagEntity> findByTagName(String tagName);
    Optional<TagEntity> findById(int id);

    Boolean existsByTagName(String name);
}

