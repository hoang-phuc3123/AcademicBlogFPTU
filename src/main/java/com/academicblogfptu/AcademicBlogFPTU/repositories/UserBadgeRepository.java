package com.academicblogfptu.AcademicBlogFPTU.repositories;

import com.academicblogfptu.AcademicBlogFPTU.entities.BadgeEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.UserBadgeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserBadgeRepository extends JpaRepository<UserBadgeEntity,Integer> {
    List<UserBadgeEntity> findByUserId(Integer id);

    Optional<UserBadgeEntity> findByUserIdAndBadgeId(int userId, int badgeId);

    Boolean existsByUserIdAndBadgeId(int userId, int badgeId);



}
