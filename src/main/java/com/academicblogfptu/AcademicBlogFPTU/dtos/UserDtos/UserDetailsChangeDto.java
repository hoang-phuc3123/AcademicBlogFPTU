package com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDetailsChangeDto {
    private int userId;
    private String email;
    private String phone;

}
