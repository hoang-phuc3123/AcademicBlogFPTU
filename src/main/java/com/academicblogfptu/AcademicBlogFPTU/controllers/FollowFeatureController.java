package com.academicblogfptu.AcademicBlogFPTU.controllers;

import com.academicblogfptu.AcademicBlogFPTU.config.UserAuthProvider;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos.FollowDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos.FollowerDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos.UserDto;
import com.academicblogfptu.AcademicBlogFPTU.services.FollowerServices;
import com.academicblogfptu.AcademicBlogFPTU.services.UserServices;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class FollowFeatureController {

    @Autowired
    private final FollowerServices followerServices;
    @Autowired
    private final UserServices userService;

    @Autowired
    private final UserAuthProvider userAuthProvider;


    @PostMapping("/follower/view")
    public ResponseEntity<List<FollowerDto>> getFollowers(@RequestHeader("Authorization") String headerValue, @RequestBody FollowDto followDto) {

            List<FollowerDto> followers = followerServices.getFollower(followDto.getUserId());
            return ResponseEntity.ok(followers);

    }

    @PostMapping("/following/view")
    public ResponseEntity<List<FollowerDto>> getFollowing(@RequestBody FollowDto followDto) {
            List<FollowerDto> followers = followerServices.getFollowed(followDto.getUserId());
            return ResponseEntity.ok(followers);
    }

    @PostMapping("/accounts/unfollow")
    public ResponseEntity<String> follow(@RequestBody FollowDto followDto){
        followerServices.unfollow(followDto);
        return ResponseEntity.ok("Success");
    }

    @PostMapping("/accounts/follow")
    public ResponseEntity<String> unfollow(@RequestBody FollowDto followDto){
        followerServices.follow(followDto);
        return ResponseEntity.ok("Success");
    }




}
