package com.academicblogfptu.AcademicBlogFPTU.controllers;

import com.academicblogfptu.AcademicBlogFPTU.config.UserAuthProvider;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos.ProfileDto;
import com.academicblogfptu.AcademicBlogFPTU.services.ProfileServices;
import com.academicblogfptu.AcademicBlogFPTU.services.UserServices;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private ProfileServices profileServices;
    @Autowired
    private final UserServices userService;

    @Autowired
    private final UserAuthProvider userAuthProvider;

    @GetMapping("/view")
    public ResponseEntity<ProfileDto> viewProfile(@RequestBody ProfileDto profileDto){
        try{
            ProfileDto profile = profileServices.viewProfile(profileDto.getUserId());

            return ResponseEntity.ok(profile);
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/edit")
    public ResponseEntity<String> editProfile(@RequestHeader("Authorization") String headerValue, @RequestBody ProfileDto profileDto){
        profileDto.setUserId(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))).getId());
        profileServices.editProfile(profileDto);
        return ResponseEntity.ok("Change successfully!");
    }


}
