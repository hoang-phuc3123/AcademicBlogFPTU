package com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos;

import com.academicblogfptu.AcademicBlogFPTU.entities.MajorEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.RoleEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.SkillEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.UserSkillEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonPropertyOrder({"id", "username", "password", "fullname", "email", "phone", "isBanned", "isMuted", "mutetime" ,"role" , "major" , "skills"})
public class ListUserDto {
    private Integer id;
    private String username;
    private String password;
    private String fullname;
    private String email;
    private String phone;
    @JsonProperty("isBanned")
    private boolean isBanned;
    @JsonProperty("isMuted")
    private boolean isMuted;
    private Timestamp mutetime;
    private RoleEntity role;
    private MajorEntity major;
    private List<SkillEntity> skills = new ArrayList<>(); // Khởi tạo trường skills ngay từ đầu
}

