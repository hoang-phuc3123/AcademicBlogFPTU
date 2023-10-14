package com.academicblogfptu.AcademicBlogFPTU.dtos;

import java.sql.Date;

public class PostDto {
    private int id;
    private String title;
    private String content;
    private Date dateOfPost;
    private int numOfUpvote;
    private int numofDownvote;
    private boolean isRewarded;
    private boolean isEdited;
    private boolean allowComment;
    private int length;

    private int userId;

}
