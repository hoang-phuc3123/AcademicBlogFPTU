package com.academicblogfptu.AcademicBlogFPTU.controllers;

import com.academicblogfptu.AcademicBlogFPTU.config.UserAuthProvider;
import com.academicblogfptu.AcademicBlogFPTU.dtos.VoteDto;
import com.academicblogfptu.AcademicBlogFPTU.exceptions.AppException;
import com.academicblogfptu.AcademicBlogFPTU.services.UserServices;
import com.academicblogfptu.AcademicBlogFPTU.services.VoteServices;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class VoteController {

    @Autowired
    private VoteServices voteServices;
    @Autowired
    private final UserServices userService;
    @Autowired
    private final UserAuthProvider userAuthProvider;


    @PostMapping("/vote/check-vote")
    public ResponseEntity<List<VoteDto>> isVoted(@RequestHeader("Authorization") String headerValue, @RequestBody VoteDto voteDto){
        voteDto.setUserId(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))).getId());
        List<VoteDto> votesInPost = voteServices.getVoteInPost(voteDto.getPostId(),voteDto.getUserId());
        return ResponseEntity.ok(votesInPost);
    }

    @PostMapping("/vote/add")
    public ResponseEntity<String> giveAVote(@RequestHeader("Authorization") String headerValue, @RequestBody VoteDto voteDto){
            voteDto.setUserId(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))).getId());
            voteServices.giveAVote(voteDto);
            return ResponseEntity.ok("Success");
    }

    @PostMapping("/vote/remove")
    public ResponseEntity<String> removeAVote(@RequestHeader("Authorization") String headerValue, @RequestBody VoteDto voteDto){
        voteDto.setUserId(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))).getId());
        voteServices.removeAVote(voteDto);
        return ResponseEntity.ok("Success");
    }


}
