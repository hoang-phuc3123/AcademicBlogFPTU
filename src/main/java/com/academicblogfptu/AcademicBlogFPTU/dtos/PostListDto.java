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
public class PostListDto {
    private int postId;

    private String accountName;

    private String title;

    private String description;

    private String dateOfPost;

    private List<CategoryEntity> category;

    private String tag;

    private String coverURL;

    @JsonProperty("is_rewarded")
    private boolean isRewarded;
}
