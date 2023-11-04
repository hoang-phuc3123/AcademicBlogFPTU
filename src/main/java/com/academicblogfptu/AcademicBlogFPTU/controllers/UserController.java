package com.academicblogfptu.AcademicBlogFPTU.controllers;


import com.academicblogfptu.AcademicBlogFPTU.config.UserAuthProvider;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos.RegisterDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos.SearchUserDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos.UserDto;
import com.academicblogfptu.AcademicBlogFPTU.exceptions.AppException;
import com.academicblogfptu.AcademicBlogFPTU.repositories.UserDetailsRepository;
import com.academicblogfptu.AcademicBlogFPTU.services.ProfileServices;
import com.academicblogfptu.AcademicBlogFPTU.services.UserServices;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {
    @Autowired
    private ProfileServices profileServices;


    @Autowired
    private final UserAuthProvider userAuthProvider;
    @Autowired
    private final UserServices userService;
    @GetMapping("users/account")
    public ResponseEntity<List<SearchUserDto>> getAllUsersForSearch(@RequestHeader("Authorization") String headerValue){
        try{
            List<SearchUserDto> list = profileServices.getAllUser(userService.findByUsername((userAuthProvider.getUser(headerValue.replace("Bearer ","")))).getId());
            return ResponseEntity.ok(list);}
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }


}
