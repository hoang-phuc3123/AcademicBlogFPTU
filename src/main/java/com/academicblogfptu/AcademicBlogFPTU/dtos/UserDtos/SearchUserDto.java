package com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchUserDto {
    private int userId;
    private String fullname;
    private String profileUrl;
    private Long numOfFollower;
    private Boolean followStatus;
}
