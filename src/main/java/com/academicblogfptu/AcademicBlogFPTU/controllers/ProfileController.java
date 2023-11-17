package com.academicblogfptu.AcademicBlogFPTU.controllers;

import com.academicblogfptu.AcademicBlogFPTU.config.UserAuthProvider;
import com.academicblogfptu.AcademicBlogFPTU.dtos.CommentDtos.ReportCommentDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos.ChangePasswordDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos.ProfileDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos.ReportProfileDto;
import com.academicblogfptu.AcademicBlogFPTU.entities.PendingReportEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.UserDetailsEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.UserEntity;
import com.academicblogfptu.AcademicBlogFPTU.repositories.CommentRepository;
import com.academicblogfptu.AcademicBlogFPTU.repositories.UserDetailsRepository;
import com.academicblogfptu.AcademicBlogFPTU.repositories.UserRepository;
import com.academicblogfptu.AcademicBlogFPTU.services.CommentService;
import com.academicblogfptu.AcademicBlogFPTU.services.ProfileServices;
import com.academicblogfptu.AcademicBlogFPTU.services.UserServices;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final UserDetailsRepository userDetailsRepository;

    @Autowired
    private final CommentService commentService;

    @PostMapping("/view")
    public ResponseEntity<ProfileDto> viewProfile(@RequestHeader("Authorization") String headerValue, @RequestBody ProfileDto profileDto){
        try{
            ProfileDto profile = profileServices.viewProfile(profileDto.getUserId(),userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))).getId());
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

    @PostMapping("/cover-upload")
    public ResponseEntity<Map<String, String>> uploadCover(@RequestHeader("Authorization") String headerValue, @RequestParam("file[]") List<MultipartFile> files) throws Exception {
        String username = userAuthProvider.getUser(headerValue.replace("Bearer ", ""));
        Optional<UserEntity> optionalUser = userRepository.findByUsername(username);
        UserEntity user = optionalUser.get();
        Optional<UserDetailsEntity> userDetails = userDetailsRepository.findByUserAccount(user);
        UserDetailsEntity userDetail = userDetails.get();
        RestTemplate restTemplate = new RestTemplate();
        String phpApiUrl = "https://lvnsoft.store/upload.php";
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        for (MultipartFile file : files) {
            body.add("file[]", file.getResource());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange(phpApiUrl, HttpMethod.POST, requestEntity, String.class);

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            ObjectMapper objectMapper = new ObjectMapper();
            List<Map<String, Object>> jsonDataList = objectMapper.readValue(responseEntity.getBody(), new TypeReference<List<Map<String, Object>>>() {});
            String link = "";
            for (Map<String, Object> jsonData : jsonDataList) {
                link = (String) jsonData.get("link");
            }
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("imageURL" , link);
            userDetail.setCoverURL(link);
            userDetailsRepository.save(userDetail);
            return ResponseEntity.ok(response);
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "File upload failed.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PostMapping("/avatar-upload")
    public ResponseEntity<Map<String, String>> uploadAvtar(@RequestHeader("Authorization") String headerValue, @RequestParam("file[]") List<MultipartFile> files) throws Exception {
        String username = userAuthProvider.getUser(headerValue.replace("Bearer ", ""));
        Optional<UserEntity> optionalUser = userRepository.findByUsername(username);
        UserEntity user = optionalUser.get();
        Optional<UserDetailsEntity> userDetails = userDetailsRepository.findByUserAccount(user);
        UserDetailsEntity userDetail = userDetails.get();
        RestTemplate restTemplate = new RestTemplate();
        String phpApiUrl = "https://lvnsoft.store/upload.php";
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        for (MultipartFile file : files) {
            body.add("file[]", file.getResource());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange(phpApiUrl, HttpMethod.POST, requestEntity, String.class);

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            ObjectMapper objectMapper = new ObjectMapper();
            List<Map<String, Object>> jsonDataList = objectMapper.readValue(responseEntity.getBody(), new TypeReference<List<Map<String, Object>>>() {});
            String link = "";
            for (Map<String, Object> jsonData : jsonDataList) {
                link = (String) jsonData.get("link");
            }
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("imageURL" , link);
            userDetail.setProfileURL(link);
            userDetailsRepository.save(userDetail);
            return ResponseEntity.ok(response);
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "File upload failed.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }


}
