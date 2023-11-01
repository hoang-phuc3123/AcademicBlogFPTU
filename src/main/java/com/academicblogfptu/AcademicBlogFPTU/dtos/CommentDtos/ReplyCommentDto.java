package com.academicblogfptu.AcademicBlogFPTU.dtos.CommentDtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReplyCommentDto {
    private int postId;
    private int parentCommentId;
    private String content;
}
