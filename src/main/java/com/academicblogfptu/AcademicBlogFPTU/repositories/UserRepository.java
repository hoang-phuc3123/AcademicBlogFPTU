package com.academicblogfptu.AcademicBlogFPTU.repositories;

import com.academicblogfptu.AcademicBlogFPTU.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity,Integer> {
    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findById(int id);

    @Query("SELECT u FROM UserEntity u WHERE u.role.id <> 1")
    List<UserEntity> findAllExceptAdmins();




}
