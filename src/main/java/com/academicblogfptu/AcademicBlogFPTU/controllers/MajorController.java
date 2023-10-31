package com.academicblogfptu.AcademicBlogFPTU.controllers;

import com.academicblogfptu.AcademicBlogFPTU.config.UserAuthProvider;
import com.academicblogfptu.AcademicBlogFPTU.dtos.MajorDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.SelectMajorDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDetailsDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDto;
import com.academicblogfptu.AcademicBlogFPTU.entities.MajorEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.TagEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.UserDetailsEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.UserEntity;
import com.academicblogfptu.AcademicBlogFPTU.repositories.MajorRepository;
import com.academicblogfptu.AcademicBlogFPTU.repositories.UserDetailsRepository;
import com.academicblogfptu.AcademicBlogFPTU.repositories.UserRepository;
import com.academicblogfptu.AcademicBlogFPTU.services.MajorServices;
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
public class MajorController {

    @Autowired
    private final MajorServices majorServices;
    @Autowired
    private final UserServices userService;

    @Autowired
    private final MajorRepository majorRepository;

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final UserDetailsRepository userDetailsRepository;

    @Autowired
    private final UserAuthProvider userAuthProvider;

    public boolean isAdmin(UserDto userDto) {
        return userDto.getRoleName().equals("admin");
    }
    @GetMapping("/majors")
    public ResponseEntity<List<MajorEntity>> getMajorList(@RequestHeader("Authorization") String headerValue){
        if (isAdmin(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            List<MajorEntity> majors = majorServices.getAllMajors();
            return ResponseEntity.ok(majors);
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/admin/new-major")
    public ResponseEntity<MajorEntity> createMajor(@RequestHeader("Authorization") String headerValue, @RequestBody MajorDto major) {
        if (isAdmin(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            MajorEntity newmajor = majorServices.createMajor(major);
            return ResponseEntity.ok(newmajor);
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

    }



    @PostMapping("/admin/edit-major")
    public ResponseEntity<MajorEntity> updateMajor(@RequestHeader("Authorization") String headerValue, @RequestBody MajorDto updatedmajor) {
        if (isAdmin(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            MajorEntity _updatedmajor = new MajorEntity();
            _updatedmajor = majorServices.updateMajor(updatedmajor);
            return ResponseEntity.ok(_updatedmajor);
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/admin/delete-major")
    public ResponseEntity<String> deleteMajor(@RequestHeader("Authorization") String headerValue, @RequestBody MajorDto deletedmajor) {
        if (isAdmin(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            try{
                majorServices.deleteMajor(deletedmajor.getId());
            } catch (Exception e){
                if(e.getMessage().equals("could not execute statement; SQL [n/a]; constraint [null]")){
                    return new ResponseEntity<>("This major has at least 1 usage",HttpStatus.CONFLICT);
                }
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            return ResponseEntity.ok("Success");
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/users/select-major")
    public ResponseEntity<String> selectMajor(@RequestHeader("Authorization") String headerValue, @RequestBody SelectMajorDto majorDto) {
        String username = userAuthProvider.getUser(headerValue.replace("Bearer ", ""));
        Optional<UserEntity> optionalUser = userRepository.findByUsername(username);
        UserEntity user = optionalUser.get();
        Optional<UserDetailsEntity> userDetails = userDetailsRepository.findByUserAccount(user);
        UserDetailsEntity userDetail = userDetails.get();
        if (userDetail.getMajor() == null) {
            MajorEntity majorEntity = majorRepository.findById(majorDto.getId()).orElse(null) ;
            userDetail.setMajor(majorEntity);
            userDetailsRepository.save(userDetail);
            return ResponseEntity.ok("Success");
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Fail");
        }
    }
}