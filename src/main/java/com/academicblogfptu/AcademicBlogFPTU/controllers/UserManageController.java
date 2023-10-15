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

    @PostMapping("/ban-user")
    public ResponseEntity<HashMap<String, String>> BanUser(@RequestHeader("Authorization") String headerValue, @RequestBody IdentificationDto identificationDto){
        if (isAdmin(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            adminServices.banUser(adminServices.findById(identificationDto.getId()));
            HashMap < String, String > responseMap = new HashMap < > ();
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

    @GetMapping("/memaycucbeo")
    public ResponseEntity<String> mmb(@RequestHeader("Authorization") String headerValue) {

        if (isAdmin(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            return ResponseEntity.ok("MEMAYBEO");
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("CONCACMAYDEOPHAIADMIN");
        }
    }


}
