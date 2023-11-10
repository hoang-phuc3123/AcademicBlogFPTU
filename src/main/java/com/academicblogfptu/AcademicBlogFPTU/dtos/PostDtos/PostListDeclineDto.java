package com.academicblogfptu.AcademicBlogFPTU.dtos.PostDtos;

import com.academicblogfptu.AcademicBlogFPTU.dtos.CategoryAndTagDtos.CategoryListDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.CategoryAndTagDtos.TagDto;
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
public class PostListDeclineDto {

    private int postId;

    private Integer userId;

    private String accountName;

    private String avatarURL;

    private String title;

    private String description;

    private String dateOfPost;

    private List<CategoryListDto> category;

    private TagDto tag;

    private String coverURL;

    @JsonProperty("is_rewarded")
    private boolean isRewarded;

    private String reasonOfDecline;

    private String slug;
}
