package com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos;

import com.academicblogfptu.AcademicBlogFPTU.entities.RoleEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.SkillEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.UserDetailsEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jdk.jfr.Timespan;
import lombok.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonPropertyOrder({"id", "username", "fullname", "isBanned", "isMuted", "mutetime" ,"roleName","profileURL", "coverURL" ,"token" , "refreshToken"})
public class UserDto {

    private int id;
    private String username;
    private String fullname;
    @JsonProperty("isBanned")
    private boolean isBanned;
    @JsonProperty("isMuted")
    private boolean isMuted;
    private Timestamp mutetime;
    private String roleName;
    private String profileURL;
    private String coverURL;
    private String token;
    private String refreshToken;

    public UserDto(int id, String username, String fullname ,UserDetailsEntity isBanned, UserDetailsEntity isMuted, UserDetailsEntity mutetime, UserDetailsEntity profileURL, UserDetailsEntity coverURL ,RoleEntity roleName, String token, String refreshToken) {

    }

}