package com.academicblogfptu.AcademicBlogFPTU.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentDto {
    private int commentId;

    private String accountName;

    private String avatarURL;

    private String content;

    private Boolean isEdited;

    private int numOfUpvote;

    private int numOfDownvote;

    private String dateOfComment;

    private int postId;

}
