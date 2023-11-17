package com.academicblogfptu.AcademicBlogFPTU.repositories;

import com.academicblogfptu.AcademicBlogFPTU.entities.UserEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.UserSkillEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserSkillRepository extends JpaRepository<UserSkillEntity, Integer> {
    List<UserSkillEntity> findByUser(UserEntity user);

    Boolean existsByUserIdAndSkillId(int userId,int skillId);

    Optional <UserSkillEntity> findByUserIdAndSkillId(int userId, int skillId);
}
