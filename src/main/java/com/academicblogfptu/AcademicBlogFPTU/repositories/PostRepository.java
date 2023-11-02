package com.academicblogfptu.AcademicBlogFPTU.repositories;

import com.academicblogfptu.AcademicBlogFPTU.entities.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<PostEntity, Integer> {
    Optional<PostEntity> findById(int id);

    Optional<PostEntity> findBySlug(String slug);
    Long countByUserId(int id);

    @Query(value = "SELECT COUNT(*) FROM post p JOIN post_details pd ON p.id = pd.post_id WHERE p.account_id = :userId AND pd.type = :type", nativeQuery = true)
    long countByUserIdAndType(int userId, String type);

}
