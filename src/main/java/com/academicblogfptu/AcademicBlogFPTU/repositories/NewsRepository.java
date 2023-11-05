package com.academicblogfptu.AcademicBlogFPTU.repositories;

import com.academicblogfptu.AcademicBlogFPTU.dtos.NewsDtos.NewsDto;
import com.academicblogfptu.AcademicBlogFPTU.entities.NewsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NewsRepository extends JpaRepository<NewsEntity,Integer> {

    Optional<NewsEntity> findById(int id);



}
