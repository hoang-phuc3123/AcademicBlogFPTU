package com.academicblogfptu.AcademicBlogFPTU.dtos;

import com.academicblogfptu.AcademicBlogFPTU.entities.RoleEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.UserDetailsEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonPropertyOrder({"id", "username", "isBanned", "roleName", "token"})
public class UserDto {

    private int id;
    private String username;
    @JsonProperty("isBanned")
    private boolean isBanned;
    private String roleName;
    private String token;

    public UserDto(int id, String username, UserDetailsEntity isBanned, RoleEntity roleName, String token) {

    }

}