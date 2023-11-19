package com.academicblogfptu.AcademicBlogFPTU.repositories;

import com.academicblogfptu.AcademicBlogFPTU.entities.PostEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.PostRewardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PostRewardRepository extends JpaRepository<PostRewardEntity, Integer> {
    @Query(value = "SELECT COUNT(*) FROM post_reward where post_id = :postId AND status = 'Pending' ", nativeQuery = true)
    int countNumOfReward(int postId);

    List<PostRewardEntity> findByPost(PostEntity postEntity);

    @Query(value = "SELECT post_id FROM post_reward where post_id = :postId ", nativeQuery = true)
    List<Integer> findByPostId(int postId);

    @Query(value = "SELECT post_id FROM post_reward where account_id = :accountId AND status = :status ", nativeQuery = true)
    List<Integer> findPostIdByUserIdAndStatus(int accountId, String status);

    List<PostRewardEntity> findByPostAndStatus(PostEntity post, String status);
}
