package com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos;

import com.academicblogfptu.AcademicBlogFPTU.dtos.PostDtos.PostListDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.PostDtos.QuestionAnswerDto;
import com.academicblogfptu.AcademicBlogFPTU.entities.BadgeEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
    private List<PostListDto> postList;
    private List<QuestionAnswerDto> qaList;


}
