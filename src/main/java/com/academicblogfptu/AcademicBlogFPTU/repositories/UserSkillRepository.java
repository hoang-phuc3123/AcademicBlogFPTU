package com.academicblogfptu.AcademicBlogFPTU.repositories;

import com.academicblogfptu.AcademicBlogFPTU.entities.UserEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.UserSkillEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserSkillRepository extends JpaRepository<UserSkillEntity, Integer> {
    List<UserSkillEntity> findByUser(UserEntity user);

    Boolean existsByUserIdAndSkillId(int userId,int skillId);

    Optional <UserSkillEntity> findByUserIdAndSkillId(int userId, int skillId);

    @Query("SELECT us.skill.id, us.skill.skillName FROM UserSkillEntity us WHERE us.user.id = :userId")
    List<Object[]> getUserSkills(@Param("userId") int userId);
}
