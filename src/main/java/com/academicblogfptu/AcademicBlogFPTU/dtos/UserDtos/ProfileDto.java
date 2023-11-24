package com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos;

import com.academicblogfptu.AcademicBlogFPTU.dtos.PostDtos.PostListDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.PostDtos.QuestionAnswerDto;
import com.academicblogfptu.AcademicBlogFPTU.entities.BadgeEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.SkillEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProfileDto {

    private int userId;
    private String fullname;
    private String profileUrl;
    private String coverUrl;
    private String userStory;
    private List<BadgeEntity> badges;
    private Long numOfFollower;
    private Long numOfPost;
    private Map<String, List<PostListDto>> postList;
    private Map<String, List<QuestionAnswerDto>> qaList;
    private List<SkillEntity> userSkillsList;


}
