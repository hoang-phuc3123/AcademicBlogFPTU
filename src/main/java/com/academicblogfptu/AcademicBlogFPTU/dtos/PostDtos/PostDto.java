package com.academicblogfptu.AcademicBlogFPTU.dtos.PostDtos;

import com.academicblogfptu.AcademicBlogFPTU.dtos.CategoryAndTagDtos.CategoryListDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.CategoryAndTagDtos.TagDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.CommentDtos.CommentDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos.RewarderDto;
import com.academicblogfptu.AcademicBlogFPTU.entities.BadgeEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.CategoryEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.SkillEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.TagEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostDto {
    private int postId;

    private Integer userId;

    private String accountName;

    private String avatarURL;

    private String title;

    private String description;

    private String content;

    private String dateOfPost;

    private Integer numOfUpVote;

    private Integer numOfDownVote;

    @JsonProperty("is_rewarded")
    private boolean isRewarded;

    @JsonProperty("is_edited")
    private boolean isEdited;

    private boolean allowComment;

    private List<CategoryListDto> category;

    private TagDto tag;

    private String coverURL;

    private String slug;

    private List<CommentDto> comments;

    private String reasonOfDecline;

    private List<SkillEntity> postSkill;

    private List<RewarderDto> rewarder;

    private List<BadgeEntity> userBadge;

    private boolean isPending;
}

