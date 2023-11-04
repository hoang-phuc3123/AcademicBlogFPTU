package com.academicblogfptu.AcademicBlogFPTU.controllers;

import com.academicblogfptu.AcademicBlogFPTU.config.UserAuthProvider;
import com.academicblogfptu.AcademicBlogFPTU.dtos.CommentDtos.ReportedCommentDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos.ReportedProfileDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos.UserDto;
import com.academicblogfptu.AcademicBlogFPTU.services.AdminServices;
import com.academicblogfptu.AcademicBlogFPTU.services.CommentService;
import com.academicblogfptu.AcademicBlogFPTU.services.TagServices;
import com.academicblogfptu.AcademicBlogFPTU.services.UserServices;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class PendingReportController {
    @Autowired
    private final TagServices tagService;

    @Autowired
    private final UserServices userService;

    @Autowired
    private final UserAuthProvider userAuthProvider;

    @Autowired
    private final AdminServices adminServices;

    @Autowired
    private final CommentService commentService;

    public boolean isAdmin(UserDto userDto) {
        return userDto.getRoleName().equals("admin");
    }

    @GetMapping("/reported-comment")
    public ResponseEntity<List<ReportedCommentDto>> viewReportedComments(@RequestHeader("Authorization") String headerValue){
        if (isAdmin(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            List<ReportedCommentDto> reportCommentList = adminServices.viewPendingReportComment();
            return ResponseEntity.ok(reportCommentList);
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/delete-reported-comment")
    public ResponseEntity<Boolean> deleteReportedComment(@RequestHeader("Authorization") String headerValue, @RequestBody ReportedCommentDto reportedCommentDto){
        if (isAdmin(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            commentService.deleteComment(reportedCommentDto.getReportedCommentId());
            return  ResponseEntity.ok(true);
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
