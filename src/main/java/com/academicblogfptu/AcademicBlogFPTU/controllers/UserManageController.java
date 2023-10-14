package com.academicblogfptu.AcademicBlogFPTU.controllers;

import com.academicblogfptu.AcademicBlogFPTU.config.UserAuthProvider;
import com.academicblogfptu.AcademicBlogFPTU.dtos.GoogleTokenDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDto;
import com.academicblogfptu.AcademicBlogFPTU.services.UserServices;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class UserManageController {

    private final UserServices userService;
    private final UserAuthProvider userAuthProvider;


    public boolean isAdmin(UserDto userDto) {
        return userDto.getRoleName().equals("admin");
    }
    @GetMapping("/memaycucbeo")
    public ResponseEntity<String> mmb(@RequestHeader("Authorization") String headerValue) {

        if (isAdmin(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            return ResponseEntity.ok("MEMAYBEO");
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("CONCACMAYDEOPHAIADMIN");
        }
    }


}
