package com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos;

import com.academicblogfptu.AcademicBlogFPTU.entities.RoleEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ListUserDto {
    private Integer id;
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String phone;
    private Boolean isMuted;
    private Boolean isBanned;
    private RoleEntity role;
}
