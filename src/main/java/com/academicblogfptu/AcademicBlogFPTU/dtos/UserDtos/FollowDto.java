package com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FollowDto {

    private int followedBy;
    private int userId;

}
