package com.academicblogfptu.AcademicBlogFPTU.dtos.PostDtos;

import com.academicblogfptu.AcademicBlogFPTU.dtos.CategoryAndTagDtos.CategoryListDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.CategoryAndTagDtos.TagDto;
import com.academicblogfptu.AcademicBlogFPTU.entities.CategoryEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.TagEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.swing.text.html.HTML;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostListTrendingDto {
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

    private Integer numOfUpVote;

    private Integer numOfDownVote;

    private String slug;
}
