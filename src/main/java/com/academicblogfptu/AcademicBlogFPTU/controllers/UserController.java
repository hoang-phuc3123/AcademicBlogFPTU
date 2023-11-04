package com.academicblogfptu.AcademicBlogFPTU.controllers;

import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos.ListUserDtoV2;
import com.academicblogfptu.AcademicBlogFPTU.services.UserServices;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {
    @Autowired
    private UserServices userServices;

    @GetMapping("users/account")
    public ResponseEntity<List<ListUserDtoV2>> RegisterAccount(@RequestHeader("Authorization") String headerValue) {
        List<ListUserDtoV2> userList = userServices.getUserList();
        return ResponseEntity.ok(userList);
    }


}
