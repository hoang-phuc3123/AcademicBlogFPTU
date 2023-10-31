package com.academicblogfptu.AcademicBlogFPTU.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class RegisterDto {

    private String username;
    private String password;
    private String fullname;
    private String email;
    private String phone;
    private String role;
    private int majorID;



}
