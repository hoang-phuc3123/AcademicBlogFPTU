package com.academicblogfptu.AcademicBlogFPTU.dtos;

import com.academicblogfptu.AcademicBlogFPTU.entities.RoleEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {
    private int id;
    private String username;
    private String roleName;
    private String token;

    public UserDto(int id, String username, RoleEntity role, String token) {
    }
}