package com.academicblogfptu.AcademicBlogFPTU.repositories;

import com.academicblogfptu.AcademicBlogFPTU.entities.FollowerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FollowerRepository extends JpaRepository<FollowerEntity,Integer> {

    List<FollowerEntity> findByFollowedBy(Integer id);

    List<FollowerEntity> findByUserId(Integer id);

    Optional<FollowerEntity> findByUserIdAndFollowedBy(Integer userId, Integer followedBy);

    Long countByUserId(Integer id);

}
