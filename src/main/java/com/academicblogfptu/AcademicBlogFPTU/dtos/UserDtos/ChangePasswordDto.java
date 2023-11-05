package com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChangePasswordDto {
    private int userId;
    private String oldPassword;
    private String newPassword;

}
