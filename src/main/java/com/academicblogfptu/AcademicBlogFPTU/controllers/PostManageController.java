package com.academicblogfptu.AcademicBlogFPTU.controllers;

import com.academicblogfptu.AcademicBlogFPTU.config.UserAuthProvider;
import com.academicblogfptu.AcademicBlogFPTU.dtos.DeclineDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.PostDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.PostListDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDto;
import com.academicblogfptu.AcademicBlogFPTU.entities.TagEntity;
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
@RequestMapping("/lecturer")
public class PostManageController {
    @Autowired
    private final PostServices postServices;

    @Autowired
    private final UserServices userServices;

    @Autowired
    private final UserAuthProvider userAuthProvider;

    @Autowired
    private final UserRepository userRepository;

    public boolean isLecturer(UserDto userDto) {
        return userDto.getRoleName().equals("lecturer");
    }

    @GetMapping("/posts/pending")
    public ResponseEntity<List<PostListDto>> viewPendingPost(@RequestHeader("Authorization") String headerValue) {
        if (isLecturer(userServices.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            Optional<UserEntity> user = userRepository.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", "")));
            UserEntity userEntity = user.get();
            List<PostListDto> pendingPost = postServices.viewPendingPost(userEntity);
            return ResponseEntity.ok(pendingPost);
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/posts/approved")
    public ResponseEntity<List<PostListDto>> viewApprovedPost(@RequestHeader("Authorization") String headerValue) {
        if (isLecturer(userServices.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            Optional<UserEntity> user = userRepository.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", "")));
            UserEntity userEntity = user.get();
            List<PostListDto> approvePostList = postServices.viewApprovedPost(userEntity);
            return ResponseEntity.ok(approvePostList);
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/posts/approve")
    public ResponseEntity<Boolean> approvePost(@RequestHeader("Authorization") String headerValue, @RequestBody PostDto postId) {
        if (isLecturer(userServices.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            Optional<UserEntity> user = userRepository.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", "")));
            UserEntity userEntity = user.get();

            postServices.approvePost(postId.getPostId(), userEntity);
            return ResponseEntity.ok(true);
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/posts/decline")
    public ResponseEntity<Boolean> declinePost(@RequestHeader("Authorization") String headerValue, @RequestBody DeclineDto declineDto) {
        if (isLecturer(userServices.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            Optional<UserEntity> user = userRepository.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", "")));
            UserEntity userEntity = user.get();
            postServices.declinePost(declineDto.getPostId(), declineDto.getReasonOfDecline(), userEntity);
            return ResponseEntity.ok(true);
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
