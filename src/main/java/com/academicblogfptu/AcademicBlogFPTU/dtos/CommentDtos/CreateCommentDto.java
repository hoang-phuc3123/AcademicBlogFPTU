package com.academicblogfptu.AcademicBlogFPTU.dtos.CommentDtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateCommentDto {
    private int postId;
    private String content;
}
