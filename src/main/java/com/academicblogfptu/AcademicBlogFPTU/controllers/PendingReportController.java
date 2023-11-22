package com.academicblogfptu.AcademicBlogFPTU.controllers;

import com.academicblogfptu.AcademicBlogFPTU.config.UserAuthProvider;
import com.academicblogfptu.AcademicBlogFPTU.dtos.AdminDtos.ActivitiesLogDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.CommentDtos.ReportedCommentDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos.ReportedProfileDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos.UserDto;
import com.academicblogfptu.AcademicBlogFPTU.entities.CommentEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.SkillEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.UserDetailsEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.UserEntity;
import com.academicblogfptu.AcademicBlogFPTU.exceptions.AppException;
import com.academicblogfptu.AcademicBlogFPTU.repositories.CommentRepository;
import com.academicblogfptu.AcademicBlogFPTU.repositories.UserDetailsRepository;
import com.academicblogfptu.AcademicBlogFPTU.repositories.UserRepository;
import com.academicblogfptu.AcademicBlogFPTU.services.AdminServices;
import com.academicblogfptu.AcademicBlogFPTU.services.CommentService;
import com.academicblogfptu.AcademicBlogFPTU.services.TagServices;
import com.academicblogfptu.AcademicBlogFPTU.services.UserServices;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
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

    @Autowired
    private final CommentRepository commentRepository;

    @Autowired
    private final AdminServices adminService;

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final UserDetailsRepository userDetailsRepository;


    public boolean isAdmin(UserDto userDto) {
        return userDto.getRoleName().equals("admin");
    }

    @GetMapping("/reported-profile")
    public ResponseEntity<List<ReportedProfileDto>> viewReportedProfile(@RequestHeader("Authorization") String headerValue){
        if (isAdmin(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            List<ReportedProfileDto> reportedProfileDto = adminServices.viewReportProfile();
            return ResponseEntity.ok(reportedProfileDto);
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/dismiss-reported-profile")
    public ResponseEntity<Boolean> dismissReportedProfile(@RequestHeader("Authorization") String headerValue , @RequestBody ReportedProfileDto reportedProfileDto){
        if (isAdmin(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            UserDetailsEntity userDetails = userDetailsRepository.findByUserId(reportedProfileDto.getReportedUserId());
            adminServices.deletePendingReportedProfile(reportedProfileDto.getReportedUserId());
            // lưu vào activities_log
            ActivitiesLogDto activitiesLogDto = new ActivitiesLogDto();
            activitiesLogDto.setAction("Gỡ bỏ báo cáo tài khoản: "+userDetails.getFullName());
            long currentTimeMillis = System.currentTimeMillis();
            Timestamp expirationTimestamp = new Timestamp(currentTimeMillis);
            activitiesLogDto.setActionTime(expirationTimestamp);
            String username = userAuthProvider.getUser(headerValue.replace("Bearer ", ""));
            UserDto adminDto = userService.findByUsername(username);
            activitiesLogDto.setUserID(adminDto.getId());
            adminService.saveActivity(activitiesLogDto);

            return ResponseEntity.ok(true);
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
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

    @PostMapping("/dismiss-reported-comment")
    public ResponseEntity<Boolean> dismissReportedComment(@RequestHeader("Authorization") String headerValue , @RequestBody ReportedCommentDto reportedCommentDto){
        if (isAdmin(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            adminServices.deleteReportComment(reportedCommentDto.getReportedCommentId());
            // lưu vào activities_log
            ActivitiesLogDto activitiesLogDto = new ActivitiesLogDto();
            activitiesLogDto.setAction("Hủy báo cáo bình luận");
            long currentTimeMillis = System.currentTimeMillis();
            Timestamp expirationTimestamp = new Timestamp(currentTimeMillis);
            activitiesLogDto.setActionTime(expirationTimestamp);
            String username = userAuthProvider.getUser(headerValue.replace("Bearer ", ""));
            UserDto adminDto = userService.findByUsername(username);
            activitiesLogDto.setUserID(adminDto.getId());
            adminService.saveActivity(activitiesLogDto);

            return ResponseEntity.ok(true);
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/delete-reported-comment")
    public ResponseEntity<Boolean> deleteReportedComment(@RequestHeader("Authorization") String headerValue, @RequestBody ReportedCommentDto reportedCommentDto){
        if (isAdmin(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            commentService.deleteComment(reportedCommentDto.getReportedCommentId());
            // lưu vào activities_log
            ActivitiesLogDto activitiesLogDto = new ActivitiesLogDto();
            activitiesLogDto.setAction("Xóa bình luận bị cáo cáo");
            long currentTimeMillis = System.currentTimeMillis();
            Timestamp expirationTimestamp = new Timestamp(currentTimeMillis);
            activitiesLogDto.setActionTime(expirationTimestamp);
            String username = userAuthProvider.getUser(headerValue.replace("Bearer ", ""));
            UserDto adminDto = userService.findByUsername(username);
            activitiesLogDto.setUserID(adminDto.getId());
            adminService.saveActivity(activitiesLogDto);

            return  ResponseEntity.ok(true);
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
