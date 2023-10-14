package com.academicblogfptu.AcademicBlogFPTU.controllers;

import com.academicblogfptu.AcademicBlogFPTU.config.UserAuthProvider;
import com.academicblogfptu.AcademicBlogFPTU.dtos.*;
import com.academicblogfptu.AcademicBlogFPTU.services.AdminServices;
import com.academicblogfptu.AcademicBlogFPTU.services.UserServices;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;

import java.util.Date;
import java.util.HashMap;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class UserManageController {

    private final UserServices userService;
    private final AdminServices adminServices;
    private final UserAuthProvider userAuthProvider;


    public boolean isAdmin(UserDto userDto) {
        return userDto.getRoleName().equals("admin");
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> RegisterAccount(@RequestHeader("Authorization") String headerValue, @RequestBody RegisterDto registerDto) {
        if (isAdmin(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            UserDto userDto = adminServices.register(registerDto);
            adminServices.RegisterUserDetail(registerDto);
            return ResponseEntity.ok(userDto);
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/set-role")
    public ResponseEntity<HashMap<String, String>> SetRole(@RequestHeader("Authorization") String headerValue, @RequestBody SetRoleDto setRoleDto){
        if (isAdmin(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            UserDto userDto = adminServices.findById(setRoleDto.getId());
            userDto.setRoleName(setRoleDto.getRole());
            adminServices.setRoleUser(userDto);
            HashMap < String, String > responseMap = new HashMap<>();
            responseMap.put("message", "Set role success.");
            return ResponseEntity.ok(responseMap);
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
    @PostMapping("/ban-user")
    public ResponseEntity<HashMap<String, String>> BanUser(@RequestHeader("Authorization") String headerValue, @RequestBody IdentificationDto identificationDto){
        if (isAdmin(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            adminServices.banUser(adminServices.findById(identificationDto.getId()));
            HashMap < String, String > responseMap = new HashMap<>();
            responseMap.put("message", "Ban success.");
            return ResponseEntity.ok(responseMap);
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/unban-user")
    public ResponseEntity<HashMap<String, String>> UnbanUser(@RequestHeader("Authorization") String headerValue, @RequestBody IdentificationDto identificationDto){
        if (isAdmin(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            adminServices.unbanUser(adminServices.findById(identificationDto.getId()));
            HashMap <String, String> responseMap = new HashMap<>();
            responseMap.put("message", "Unban success.");
            return ResponseEntity.ok(responseMap);
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/mute-user")
    public ResponseEntity<HashMap<String, String>> MuteUser(@RequestHeader("Authorization") String headerValue, @RequestBody MuteDto muteDto){
        if (isAdmin(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            Date currentTime = new Date();

            // Tính thời điểm kết thúc dựa trên thời điểm hiện tại và thời lượng cấm (đơn vị là giờ)
            long muteDurationMillis = muteDto.getMuteDuration() * 3600000; // 1 giờ = 3600000 ms
            Date muteEndTime = new Date(currentTime.getTime() + muteDurationMillis);

            // Chuyển java.util.Date thành java.sql.Timestamp để lưu vào cơ sở dữ liệu
            Timestamp timestamp = new Timestamp(muteEndTime.getTime());
            adminServices.muteUser(adminServices.findById(muteDto.getId()), timestamp);
            HashMap <String, String> responseMap = new HashMap<>();
            responseMap.put("message", "Mute success.");
            return ResponseEntity.ok(responseMap);
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/unmute-user")
    public ResponseEntity<HashMap<String, String>> UnmuteUser(@RequestHeader("Authorization") String headerValue, @RequestBody IdentificationDto identificationDto){
        if (isAdmin(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            adminServices.unmuteUser(adminServices.findById(identificationDto.getId()));
            HashMap <String, String> responseMap = new HashMap<>();
            responseMap.put("message", "Unmute success.");
            return ResponseEntity.ok(responseMap);
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
