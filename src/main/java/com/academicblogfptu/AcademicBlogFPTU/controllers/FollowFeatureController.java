package com.academicblogfptu.AcademicBlogFPTU.controllers;

import com.academicblogfptu.AcademicBlogFPTU.dtos.FollowDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.FollowerDto;
import com.academicblogfptu.AcademicBlogFPTU.services.FollowerServices;
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


    @GetMapping("/follower/view")
    public ResponseEntity<List<FollowerDto>> getFollowers(@RequestParam("id") Integer accountId) {
        List<FollowerDto> followers = followerServices.getFollower(accountId) ;
        return ResponseEntity.ok(followers);
    }

    @GetMapping("/following/view")
    public ResponseEntity<List<FollowerDto>> getFollowing(@RequestParam("id") Integer accountId) {
        List<FollowerDto> followers = followerServices.getFollowed(accountId) ;
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
