package com.academicblogfptu.AcademicBlogFPTU.repositories;

import com.academicblogfptu.AcademicBlogFPTU.entities.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Integer> {
    Optional<CategoryEntity> findById(Integer id);

    Optional<CategoryEntity> findByCategoryName(String name);

    List<CategoryEntity> findByCategoryType(String type);

    List<CategoryEntity> findByParentID(Integer id);

    List<CategoryEntity> findByParentIDIsNull();

    CategoryEntity findByCategoryNameAndCategoryTypeAndParentID(String categoryName,String categoryType,int parentID);

    CategoryEntity findByCategoryNameAndCategoryType(String categoryName,String categoryType);

    @Query(value = "SELECT c.id, c.category_name, c.category_type, c.parent_id, c.major_id " +
            "FROM post p " +
            "JOIN post_details pd ON p.id = pd.post_id " +
            "JOIN category c ON p.category_id = c.id " +
            "WHERE pd.type = 'Approve' " +
            "AND c.category_type = 'Subject' " +
            "AND p.date_of_post >= DATEADD(day, -7, GETDATE()) " +
            "GROUP BY c.id, c.category_name, c.category_type, c.parent_id, c.major_id " +
            "ORDER BY COUNT(p.category_id) DESC", nativeQuery = true)
    List<CategoryEntity> findTrendingCategoryForLast7Days();
}
