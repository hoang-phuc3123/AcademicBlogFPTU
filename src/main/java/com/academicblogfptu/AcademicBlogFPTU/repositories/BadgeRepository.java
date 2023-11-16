package com.academicblogfptu.AcademicBlogFPTU.repositories;

import com.academicblogfptu.AcademicBlogFPTU.entities.BadgeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BadgeRepository extends JpaRepository<BadgeEntity,Integer> {

    Optional<BadgeEntity> findByBadgeName(String name);

}
