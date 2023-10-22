package com.academicblogfptu.AcademicBlogFPTU.dtos;

import com.academicblogfptu.AcademicBlogFPTU.entities.CategoryEntity;
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
public class QuestionAnswerDto {

    private int postId;

    private String accountName;

    private String title;

    private String content;

    private String dateOfPost;

    private Integer numOfUpVote;

    private Integer numOfDownVote;

    private List<CategoryEntity> category;

    private List<String> imageURL;

    private String tag;

    @JsonProperty("is_rewarded")
    private boolean isRewarded;
}
