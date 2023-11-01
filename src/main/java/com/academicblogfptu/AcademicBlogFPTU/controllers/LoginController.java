package com.academicblogfptu.AcademicBlogFPTU.controllers;


import com.academicblogfptu.AcademicBlogFPTU.config.UserAuthProvider;
import com.academicblogfptu.AcademicBlogFPTU.dtos.LoginRequestDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDto;
import com.academicblogfptu.AcademicBlogFPTU.services.TokenServices;
import com.academicblogfptu.AcademicBlogFPTU.services.UserServices;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class LoginController {
    private final UserServices userService;
    private final TokenServices tokenService;
    private final UserAuthProvider userAuthProvider;
    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@RequestBody LoginRequestDto loginRequestDto) {
        UserDto userDto = userService.login(loginRequestDto);
        String refreshToken = UUID.randomUUID().toString();
        String token = userAuthProvider.createToken(userDto.getUsername(),90000000);
        userDto.setToken(token);
        userDto.setRefreshToken(refreshToken);
        tokenService.StoreToken(token, refreshToken);
        return ResponseEntity.ok(userDto);
    }
}
