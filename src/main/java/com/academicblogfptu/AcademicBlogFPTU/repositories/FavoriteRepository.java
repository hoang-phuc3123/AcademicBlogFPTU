package com.academicblogfptu.AcademicBlogFPTU.repositories;

import com.academicblogfptu.AcademicBlogFPTU.entities.FavoritePostEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<FavoritePostEntity,Integer> {

   List<FavoritePostEntity> findByUserId(Integer id);

   Optional<FavoritePostEntity> findById(int id);

   Optional<FavoritePostEntity> findByIdAndUserId(int id, int userId);

   FavoritePostEntity findByPostIdAndUserId(int postId,int userId);
}
