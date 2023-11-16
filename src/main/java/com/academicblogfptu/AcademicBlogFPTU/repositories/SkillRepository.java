package com.academicblogfptu.AcademicBlogFPTU.repositories;

import com.academicblogfptu.AcademicBlogFPTU.entities.SkillEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SkillRepository extends JpaRepository<SkillEntity, Integer> {
    Optional<SkillEntity> findById(int id);

    Optional<SkillEntity> findBySkillName(String name);

    Boolean existsBySkillName(String name);
}
