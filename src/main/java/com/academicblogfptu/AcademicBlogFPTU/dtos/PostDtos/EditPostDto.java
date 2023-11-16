package com.academicblogfptu.AcademicBlogFPTU.dtos.PostDtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EditPostDto {
    private int postId;
    private String title;
    private String description;
    private String content;
    private boolean allowComment;
    private int categoryId;
    private int tagId;
    private String coverURL;
    private String slug;
    private Integer length;
    private List<String> postSkill;
}
