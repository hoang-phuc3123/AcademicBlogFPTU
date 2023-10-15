package com.academicblogfptu.AcademicBlogFPTU.dtos;

import com.academicblogfptu.AcademicBlogFPTU.entities.RoleEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.UserDetailsEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jdk.jfr.Timespan;
import lombok.*;

import java.sql.Timestamp;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonPropertyOrder({"id", "username", "isBanned", "isMuted", "mutetime" ,"roleName", "token"})
public class UserDto {

    private int id;
    private String username;
    @JsonProperty("isBanned")
    private boolean isBanned;
    @JsonProperty("isMuted")
    private boolean isMuted;
    private Timestamp mutetime;
    private String roleName;
    private String token;

    public UserDto(int id, String username, UserDetailsEntity isBanned, UserDetailsEntity isMuted, UserDetailsEntity mutetime ,RoleEntity roleName, String token) {

    }

}