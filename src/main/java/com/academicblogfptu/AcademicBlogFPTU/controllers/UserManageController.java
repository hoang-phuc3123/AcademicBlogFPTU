package com.academicblogfptu.AcademicBlogFPTU.controllers;

import com.academicblogfptu.AcademicBlogFPTU.config.UserAuthProvider;
import com.academicblogfptu.AcademicBlogFPTU.dtos.*;
import com.academicblogfptu.AcademicBlogFPTU.entities.RoleEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.UserDetailsEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.UserEntity;
import com.academicblogfptu.AcademicBlogFPTU.repositories.UserDetailsRepository;
import com.academicblogfptu.AcademicBlogFPTU.services.AdminServices;
import com.academicblogfptu.AcademicBlogFPTU.services.UserServices;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;

import java.util.Date;
import java.util.HashMap;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class UserManageController {

    private final UserServices userService;
    private final AdminServices adminService;
    private final UserAuthProvider userAuthProvider;
    private final UserDetailsRepository userDetailsRepository;


    public boolean isAdmin(UserDto userDto) {
        return userDto.getRoleName().equals("admin");
    }

    @GetMapping("/users")
    public ResponseEntity<List<ListUserDto>> getAllUsers(@RequestHeader("Authorization") String headerValue) {
        if (isAdmin(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            List<Object[]> userInfos = userDetailsRepository.getAllUsersInfo();
            List<ListUserDto> users = new ArrayList<>();

            for (Object[] userInfo : userInfos) {
                ListUserDto userDetailsInfo = new ListUserDto();
                userDetailsInfo.setId((Integer) userInfo[0]);
                userDetailsInfo.setUsername(userInfo[1].toString());
                userDetailsInfo.setPassword(userInfo[2].toString());
                userDetailsInfo.setFullName(userInfo[3].toString());
                userDetailsInfo.setEmail(userInfo[4] != null ? userInfo[4].toString() : null); // Check for null
                userDetailsInfo.setPhone(userInfo[5] != null ? userInfo[5].toString() : null); // Check for null;
                userDetailsInfo.setRole((RoleEntity) userInfo[6]);
                users.add(userDetailsInfo);
            }
            return ResponseEntity.ok(users);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> RegisterAccount(@RequestHeader("Authorization") String headerValue, @RequestBody RegisterDto registerDto) {
        if (isAdmin(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            String email = registerDto.getEmail();
            if (userService.isEmailExist(email).equals("true")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            UserDto userDto = adminService.register(registerDto);
            adminService.RegisterUserDetail(registerDto);
            return ResponseEntity.ok(userDto);
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/set-role")
    public ResponseEntity<HashMap<String, String>> SetRole(@RequestHeader("Authorization") String headerValue, @RequestBody SetRoleDto setRoleDto){
        if (isAdmin(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            UserDto userDto = adminService.findById(setRoleDto.getId());
            userDto.setRoleName(setRoleDto.getRole());
            UserDto userSet = userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", "")));
            adminService.setRoleUser(userDto,userSet.getId());
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
            adminService.banUser(adminService.findById(identificationDto.getId()));
            HashMap <String, String> responseMap = new HashMap<>();
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
            adminService.unbanUser(adminService.findById(identificationDto.getId()));
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
            Timestamp timestamp = new Timestamp(muteEndTime.getTime());
            adminService.muteUser(adminService.findById(muteDto.getId()), timestamp);
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
            adminService.unmuteUser(adminService.findById(identificationDto.getId()));
            HashMap <String, String> responseMap = new HashMap<>();
            responseMap.put("message", "Unmute success.");
            return ResponseEntity.ok(responseMap);
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
