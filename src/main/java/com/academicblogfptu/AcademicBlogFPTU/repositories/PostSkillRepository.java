package com.academicblogfptu.AcademicBlogFPTU.repositories;

import com.academicblogfptu.AcademicBlogFPTU.entities.PostEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.PostRewardEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.PostSkillEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostSkillRepository extends JpaRepository<PostSkillEntity, Integer> {

    List<PostSkillEntity> findByPost(PostEntity post);

}
