package com.academicblogfptu.AcademicBlogFPTU.controllers;


import com.academicblogfptu.AcademicBlogFPTU.config.UserAuthProvider;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos.LoginRequestDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos.UserDto;
import com.academicblogfptu.AcademicBlogFPTU.services.TokenServices;
import com.academicblogfptu.AcademicBlogFPTU.services.UserServices;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.*;

import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import java.net.HttpURLConnection;
import java.net.URL;
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
        String token = userAuthProvider.createToken(userDto.getUsername(),900000);
        userDto.setToken(token);
        userDto.setRefreshToken(refreshToken);
        tokenService.StoreToken(token, refreshToken);
        URL url = null;
        try {
            url = new URL("https://lvnsoft.store/RequestCount/visit-count.php");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return ResponseEntity.ok(userDto);
            }
            else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }
}
