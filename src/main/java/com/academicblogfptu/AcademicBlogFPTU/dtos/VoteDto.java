package com.academicblogfptu.AcademicBlogFPTU.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VoteDto {

    private int voteId;
    private String typeOfVote;
    private String voteTime;
    private int userId;
    private int postId;
    private Integer commentId;


}
