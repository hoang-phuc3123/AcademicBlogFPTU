package com.academicblogfptu.AcademicBlogFPTU.controllers;

import com.academicblogfptu.AcademicBlogFPTU.config.UserAuthProvider;
import com.academicblogfptu.AcademicBlogFPTU.dtos.CommentDtos.CommentDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.CommentDtos.CreateCommentDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.CommentDtos.ReplyCommentDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.CommentDtos.ReportCommentDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.PostDtos.PostDto;
import com.academicblogfptu.AcademicBlogFPTU.entities.PendingReportEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.ReportReasonEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.UserEntity;
import com.academicblogfptu.AcademicBlogFPTU.repositories.UserRepository;
import com.academicblogfptu.AcademicBlogFPTU.services.CommentService;
import com.academicblogfptu.AcademicBlogFPTU.services.PostServices;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    @Autowired
    private final PostServices postServices;

    @PostMapping("comments/create")
    public ResponseEntity<CommentDto> createComment(@RequestHeader("Authorization") String headerValue, @RequestBody CreateCommentDto createCommentDto){
        Optional<UserEntity> user = userRepository.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", "")));
        UserEntity userEntity = user.get();

        CommentDto newComment = commentService.createComment(createCommentDto, userEntity);
        return ResponseEntity.ok(newComment);
    }


    @PostMapping("comments/edit")
    public ResponseEntity<CommentDto> editComment(@RequestHeader("Authorization") String headerValue, @RequestBody CommentDto commentDto){
        Optional<UserEntity> user = userRepository.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", "")));
        UserEntity userEntity = user.get();

        CommentDto editComment = commentService.editComment(commentDto, userEntity);
        return ResponseEntity.ok(editComment);
    }

    @PostMapping("comments/delete")
    public ResponseEntity<Boolean> deleteComment(@RequestBody CommentDto commentId){
        commentService.deleteComment(commentId.getCommentId());
        return ResponseEntity.ok(true);
    }

    @PostMapping("comments/reply")
    public ResponseEntity<CommentDto> replyComment(@RequestHeader("Authorization") String headerValue, @RequestBody ReplyCommentDto replyCommentDto){
        Optional<UserEntity> user = userRepository.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", "")));
        UserEntity userEntity = user.get();

        CommentDto newComment = commentService.replyComment(replyCommentDto, userEntity);
        return ResponseEntity.ok(newComment);
    }

    @PostMapping("comments/toggle")
    public ResponseEntity<Boolean> commentToggle(@RequestBody PostDto postId){
        postServices.commentToggle(postId.getPostId());
        return ResponseEntity.ok(true);
    }

    @GetMapping("report/report-reason")
    public ResponseEntity<List<ReportReasonEntity>> viewReportReason(){
        List<ReportReasonEntity> reportReasons = commentService.viewReportReason();
        return ResponseEntity.ok(reportReasons);
    }

    @PostMapping("comments/report")
    public ResponseEntity<Boolean> reportComment(@RequestHeader("Authorization") String headerValue,@RequestBody ReportCommentDto reportCommentDto){
        Optional<UserEntity> user = userRepository.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", "")));
        UserEntity reporter = user.get();

        PendingReportEntity pendingReportEntity =  commentService.reportComment(reportCommentDto, reporter);
        commentService.pendingReportReason(pendingReportEntity, reportCommentDto.getReasonOfReportId());
        return ResponseEntity.ok(true);
    }
}
