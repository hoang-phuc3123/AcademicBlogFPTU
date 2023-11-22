package com.academicblogfptu.AcademicBlogFPTU.controllers;

import com.academicblogfptu.AcademicBlogFPTU.config.UserAuthProvider;
import com.academicblogfptu.AcademicBlogFPTU.dtos.AdminDtos.ActivitiesLogDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.MajorDtos.MajorDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.MajorDtos.SelectMajorDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos.UserDto;
import com.academicblogfptu.AcademicBlogFPTU.entities.MajorEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.SkillEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.UserDetailsEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.UserEntity;
import com.academicblogfptu.AcademicBlogFPTU.exceptions.AppException;
import com.academicblogfptu.AcademicBlogFPTU.repositories.MajorRepository;
import com.academicblogfptu.AcademicBlogFPTU.repositories.UserDetailsRepository;
import com.academicblogfptu.AcademicBlogFPTU.repositories.UserRepository;
import com.academicblogfptu.AcademicBlogFPTU.services.AdminServices;
import com.academicblogfptu.AcademicBlogFPTU.services.MajorServices;
import com.academicblogfptu.AcademicBlogFPTU.services.UserServices;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
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

    @Autowired
    private final AdminServices adminService;

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
            // lưu vào activities_log
            ActivitiesLogDto activitiesLogDto = new ActivitiesLogDto();
            activitiesLogDto.setAction("Thêm mới major: "+major.getMajorName());
            long currentTimeMillis = System.currentTimeMillis();
            Timestamp expirationTimestamp = new Timestamp(currentTimeMillis);
            activitiesLogDto.setActionTime(expirationTimestamp);
            String username = userAuthProvider.getUser(headerValue.replace("Bearer ", ""));
            UserDto adminDto = userService.findByUsername(username);
            activitiesLogDto.setUserID(adminDto.getId());
            adminService.saveActivity(activitiesLogDto);

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

            MajorEntity oldMajor = majorRepository.findById(updatedmajor.getId())
                    .orElseThrow(()->new AppException("Unknown major", HttpStatus.NOT_FOUND));
            // lưu vào activities_log
            ActivitiesLogDto activitiesLogDto = new ActivitiesLogDto();
            activitiesLogDto.setAction("Chỉnh sửa major "+oldMajor.getMajorName() +" thành "+updatedmajor.getMajorName());
            long currentTimeMillis = System.currentTimeMillis();
            Timestamp expirationTimestamp = new Timestamp(currentTimeMillis);
            activitiesLogDto.setActionTime(expirationTimestamp);
            String username = userAuthProvider.getUser(headerValue.replace("Bearer ", ""));
            UserDto adminDto = userService.findByUsername(username);
            activitiesLogDto.setUserID(adminDto.getId());
            adminService.saveActivity(activitiesLogDto);

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
                MajorEntity deleteMajor = majorRepository.findById(deletedmajor.getId())
                        .orElseThrow(()->new AppException("Unknown major", HttpStatus.NOT_FOUND));
                majorServices.deleteMajor(deletedmajor.getId());
                // lưu vào activities_log
                ActivitiesLogDto activitiesLogDto = new ActivitiesLogDto();
                activitiesLogDto.setAction("Xóa major: " +deleteMajor.getMajorName());
                long currentTimeMillis = System.currentTimeMillis();
                Timestamp expirationTimestamp = new Timestamp(currentTimeMillis);
                activitiesLogDto.setActionTime(expirationTimestamp);
                String username = userAuthProvider.getUser(headerValue.replace("Bearer ", ""));
                UserDto adminDto = userService.findByUsername(username);
                activitiesLogDto.setUserID(adminDto.getId());
                adminService.saveActivity(activitiesLogDto);
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

    @GetMapping("/users/check-major")
    public ResponseEntity<String> selectMajor(@RequestHeader("Authorization") String headerValue) {
        String username = userAuthProvider.getUser(headerValue.replace("Bearer ", ""));
        Optional<UserEntity> optionalUser = userRepository.findByUsername(username);
        UserEntity user = optionalUser.get();
        Optional<UserDetailsEntity> userDetails = userDetailsRepository.findByUserAccount(user);
        UserDetailsEntity userDetail = userDetails.get();
        if (userDetail.getMajor() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("False");
        }
        else return ResponseEntity.ok("True");
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