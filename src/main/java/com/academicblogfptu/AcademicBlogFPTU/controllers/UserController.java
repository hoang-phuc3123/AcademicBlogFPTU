package com.academicblogfptu.AcademicBlogFPTU.controllers;



import com.academicblogfptu.AcademicBlogFPTU.config.UserAuthProvider;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos.*;
import com.academicblogfptu.AcademicBlogFPTU.exceptions.AppException;
import com.academicblogfptu.AcademicBlogFPTU.repositories.UserDetailsRepository;
import com.academicblogfptu.AcademicBlogFPTU.services.ProfileServices;

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

    @PostMapping("accounts/search")
    public ResponseEntity<List<SearchUserDto>> getSearchResult(@RequestHeader("Authorization") String headerValue, @RequestBody SearchRequestDto searchRequestDto){
        try{
            searchRequestDto.setUserId(userService.findByUsername((userAuthProvider.getUser(headerValue.replace("Bearer ","")))).getId());
            List<SearchUserDto> list = profileServices.getSearchResult(searchRequestDto);
            return ResponseEntity.ok(list);
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/user/change-user-details")
    public ResponseEntity<String> editUserDetails(@RequestHeader("Authorization") String headerValue, @RequestBody UserDetailsChangeDto userDetails){
        userDetails.setUserId(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))).getId());
        userService.editUserDetails(userDetails);
        return ResponseEntity.ok("Change successfully!");
    }

    @GetMapping("/user/information")
    public ResponseEntity<UserInformationDto> getUserInformation(@RequestHeader("Authorization") String headerValue){
        UserInformationDto userInformationDto = userService.getUserInformation(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))).getId());
        return ResponseEntity.ok(userInformationDto);
    }
}
