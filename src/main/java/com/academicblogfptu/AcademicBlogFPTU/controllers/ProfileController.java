package com.academicblogfptu.AcademicBlogFPTU.controllers;

import com.academicblogfptu.AcademicBlogFPTU.config.UserAuthProvider;
import com.academicblogfptu.AcademicBlogFPTU.dtos.CommentDtos.ReportCommentDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos.ChangePasswordDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos.ProfileDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos.ReportProfileDto;
import com.academicblogfptu.AcademicBlogFPTU.entities.PendingReportEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.UserEntity;
import com.academicblogfptu.AcademicBlogFPTU.repositories.CommentRepository;
import com.academicblogfptu.AcademicBlogFPTU.repositories.UserRepository;
import com.academicblogfptu.AcademicBlogFPTU.services.CommentService;
import com.academicblogfptu.AcademicBlogFPTU.services.ProfileServices;
import com.academicblogfptu.AcademicBlogFPTU.services.UserServices;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

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

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final CommentService commentService;

    @PostMapping("/view")
    public ResponseEntity<ProfileDto> viewProfile(@RequestBody ProfileDto profileDto){
        try{
            ProfileDto profile = profileServices.viewProfile(profileDto.getUserId());
            return ResponseEntity.ok(profile);
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestHeader("Authorization") String headerValue, @RequestBody ChangePasswordDto changePasswordDto){
        int id = userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))).getId();
        changePasswordDto.setUserId(id);
        userService.changePassword(changePasswordDto);
        return ResponseEntity.ok("Success");
    }

    @PostMapping("/edit")
    public ResponseEntity<String> editProfile(@RequestHeader("Authorization") String headerValue, @RequestBody ProfileDto profileDto){
        profileDto.setUserId(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))).getId());
        profileServices.editProfile(profileDto);
        return ResponseEntity.ok("Change successfully!");
    }

    @PostMapping("/report")
    public ResponseEntity<Boolean> reportProfile(@RequestHeader("Authorization") String headerValue,@RequestBody ReportProfileDto reportProfileDto){
        Optional<UserEntity> user = userRepository.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", "")));
        UserEntity reporter = user.get();

        PendingReportEntity pendingReportEntity =  profileServices.reportProfile(reportProfileDto, reporter);
        commentService.pendingReportReason(pendingReportEntity, reportProfileDto.getReasonOfReportId());
        return ResponseEntity.ok(true);
    }
}
