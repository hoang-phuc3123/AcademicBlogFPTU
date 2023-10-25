package com.academicblogfptu.AcademicBlogFPTU.controllers;

import com.academicblogfptu.AcademicBlogFPTU.config.UserAuthProvider;
import com.academicblogfptu.AcademicBlogFPTU.dtos.CommentDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.CreateCommentDto;
import com.academicblogfptu.AcademicBlogFPTU.entities.UserEntity;
import com.academicblogfptu.AcademicBlogFPTU.repositories.UserRepository;
import com.academicblogfptu.AcademicBlogFPTU.services.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class CommentController {

    @Autowired
    private final CommentService commentService;

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final UserAuthProvider userAuthProvider;

    @PostMapping("/comments/create")
    public ResponseEntity<CommentDto> createComment(@RequestHeader("Authorization") String headerValue, @RequestBody CreateCommentDto createCommentDto){
        Optional<UserEntity> user = userRepository.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", "")));
        UserEntity userEntity = user.get();

        CommentDto newComment = commentService.createComment(createCommentDto, userEntity);
        return ResponseEntity.ok(newComment);
    }


    @PostMapping("/comments/edit")
    public ResponseEntity<CommentDto> editComment(@RequestHeader("Authorization") String headerValue, @RequestBody CommentDto commentDto){
        Optional<UserEntity> user = userRepository.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", "")));
        UserEntity userEntity = user.get();

        CommentDto editComment = commentService.editComment(commentDto, userEntity);
        return ResponseEntity.ok(editComment);
    }
}
