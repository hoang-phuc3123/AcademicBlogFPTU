package com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserInformationDto {
    private int userId;
    private String username;
    private String fullName;
    private String phone;
    private String email;
    private String coverUrl;
    private String avatarUrl;
}
