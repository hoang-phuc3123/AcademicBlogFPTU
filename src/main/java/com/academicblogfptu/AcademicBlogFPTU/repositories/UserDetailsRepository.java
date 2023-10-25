package com.academicblogfptu.AcademicBlogFPTU.repositories;

import com.academicblogfptu.AcademicBlogFPTU.entities.UserDetailsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import com.academicblogfptu.AcademicBlogFPTU.entities.UserEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;
import java.util.List;
public interface UserDetailsRepository extends JpaRepository<UserDetailsEntity, Integer> {

    Optional<UserDetailsEntity> findById(int id);
    Optional<UserDetailsEntity> findByEmail(String email);

    UserDetailsEntity findByUserId(Integer userId);

    @Query("SELECT u FROM UserDetailsEntity u WHERE u.user = :user")
    Optional<UserDetailsEntity> findByUserAccount(UserEntity user);

    @Query("SELECT ue.id, ue.username, ue.password, ude.fullName, ude.email, ude.phone, ue.role FROM UserDetailsEntity ude JOIN ude.user ue")
    List<Object[]> getAllUsersInfo();
}