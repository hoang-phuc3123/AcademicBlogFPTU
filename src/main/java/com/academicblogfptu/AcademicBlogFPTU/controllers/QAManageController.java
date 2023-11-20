package com.academicblogfptu.AcademicBlogFPTU.controllers;

import com.academicblogfptu.AcademicBlogFPTU.config.UserAuthProvider;
import com.academicblogfptu.AcademicBlogFPTU.dtos.PostDtos.DeclineDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.PostDtos.PostDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.PostDtos.QuestionAnswerDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos.UserDto;
import com.academicblogfptu.AcademicBlogFPTU.entities.UserEntity;
import com.academicblogfptu.AcademicBlogFPTU.repositories.UserRepository;
import com.academicblogfptu.AcademicBlogFPTU.services.PostServices;
import com.academicblogfptu.AcademicBlogFPTU.services.UserServices;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mentor")
public class QAManageController {

    public boolean isMentor(UserDto userDto) {
        return userDto.getRoleName().equals("mentor");
    }

    @Autowired
    private final PostServices postServices;

    @Autowired
    private final UserServices userServices;

    @Autowired
    private final UserAuthProvider userAuthProvider;

    @Autowired
    private final UserRepository userRepository;

    @GetMapping("/q-a/view")
    public ResponseEntity<List<QuestionAnswerDto>> viewQAPending(@RequestHeader("Authorization") String headerValue) {
        if (isMentor(userServices.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            Optional<UserEntity> user = userRepository.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", "")));
            UserEntity userEntity = user.get();

            List<QuestionAnswerDto> qaPendingPostList = postServices.viewQAPendingPost(userEntity);
            return ResponseEntity.ok(qaPendingPostList);
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/q-a/approved")
    public ResponseEntity<List<QuestionAnswerDto>> viewQAApproved(@RequestHeader("Authorization") String headerValue) {
        if (isMentor(userServices.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            List<QuestionAnswerDto> qaApprovedPostList = postServices.viewQAApprovedPost();
            return ResponseEntity.ok(qaApprovedPostList);
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/q-a/approve")
    public ResponseEntity<Boolean> approveQAPost(@RequestHeader("Authorization") String headerValue, @RequestBody PostDto postId) {
        if (isMentor(userServices.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            Optional<UserEntity> user = userRepository.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", "")));
            UserEntity userEntity = user.get();

            postServices.approveQAPost(postId.getPostId(), userEntity);
            return ResponseEntity.ok(true);
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/q-a/decline")
    public ResponseEntity<Boolean> declineQAPost(@RequestHeader("Authorization") String headerValue, @RequestBody DeclineDto declineDto) {
        if (isMentor(userServices.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            Optional<UserEntity> user = userRepository.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", "")));
            UserEntity userEntity = user.get();

            postServices.declineQAPost(declineDto.getPostId(),declineDto.getReasonOfDecline() , userEntity);
            return ResponseEntity.ok(true);
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }


}
