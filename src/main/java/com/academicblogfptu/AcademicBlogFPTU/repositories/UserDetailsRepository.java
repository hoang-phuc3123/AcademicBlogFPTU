package com.academicblogfptu.AcademicBlogFPTU.repositories;

import com.academicblogfptu.AcademicBlogFPTU.entities.UserDetailsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import com.academicblogfptu.AcademicBlogFPTU.entities.UserEntity;
import org.springframework.data.jpa.repository.Query;


import java.util.Optional;
import java.util.List;
public interface UserDetailsRepository extends JpaRepository<UserDetailsEntity, Integer> {

    Optional<UserDetailsEntity> findById(int id);
    Optional<UserDetailsEntity> findByEmail(String email);

    UserDetailsEntity findByUserId(Integer userId);

    Optional<UserDetailsEntity> findByPhone(String phone);

    @Query("SELECT u FROM UserDetailsEntity u WHERE u.user = :user")
    Optional<UserDetailsEntity> findByUserAccount(UserEntity user);

    @Query("SELECT ue.id, ue.username, ue.password, ude.fullName, ude.email, ude.phone, ude.isBanned, ude.isMuted, ude.mutetime, ue.role, ude.major, s.id as skillId, s.skillName FROM UserDetailsEntity ude LEFT JOIN ude.user ue LEFT JOIN ude.major LEFT JOIN UserSkillEntity us ON ue.id = us.user.id LEFT JOIN SkillEntity s ON us.skill.id = s.id")
    List<Object[]> getAllUsersInfo();


    @Query(value = "SELECT u FROM UserDetailsEntity u WHERE CONCAT(u.fullName, '') LIKE %:search%")
    List<UserDetailsEntity> findUserByFullName(String search);


    @Query(value = "SELECT u FROM UserDetailsEntity u WHERE CONCAT(u.fullName, '') LIKE %:search%")
    Page<UserDetailsEntity> findUsersPage(String search, Pageable pageable);


}