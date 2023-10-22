package com.academicblogfptu.AcademicBlogFPTU.dtos;

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
    private List<String> imageURL;
    private List<String> videoURL;
    private String slug;
}
