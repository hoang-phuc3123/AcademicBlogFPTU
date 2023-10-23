package com.academicblogfptu.AcademicBlogFPTU.repositories;

import com.academicblogfptu.AcademicBlogFPTU.entities.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Integer> {
    Optional<CategoryEntity> findById(Integer id);

    Optional<CategoryEntity> findByCategoryName(String name);

    List<CategoryEntity> findByParentID(Integer id);

    List<CategoryEntity> findByParentIDIsNull();

    CategoryEntity findByCategoryNameAndCategoryTypeAndParentID(String categoryName,String categoryType,int parentID);

    CategoryEntity findByCategoryNameAndCategoryType(String categoryName,String categoryType);
}
