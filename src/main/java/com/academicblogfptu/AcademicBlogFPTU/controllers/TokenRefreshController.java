package com.academicblogfptu.AcademicBlogFPTU.controllers;

import com.academicblogfptu.AcademicBlogFPTU.config.UserAuthProvider;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos.RefreshTokenDto;
import com.academicblogfptu.AcademicBlogFPTU.services.TokenServices;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class TokenRefreshController {


    @Autowired
    private final TokenServices tokenService;

    @Autowired
    private final UserAuthProvider userAuthProvider;


    @PostMapping("/refresh-token")
    public ResponseEntity<HashMap<String, String>> RefreshToken(@RequestBody RefreshTokenDto tokenDto) {

        String refreshToken = tokenDto.getRefreshToken();
        String oldToken = tokenService.GetTokenFromRefreshToken(refreshToken);
        String newrefreshToken = UUID.randomUUID().toString();
        String user = userAuthProvider.getUserRefresh(oldToken);
        String newToken = userAuthProvider.createToken(user, 900000);
        tokenService.RefreshToken(oldToken, newToken , newrefreshToken);
        HashMap <String, String> responseMap = new HashMap<>();
        responseMap.put("token", newToken);
        responseMap.put("refreshToken", newrefreshToken);
        return ResponseEntity.ok(responseMap);

    }

    @PostMapping("/remove-token")
    public ResponseEntity<HashMap<String, String>> RemoveToken(@RequestBody RefreshTokenDto tokenDto) {

        String refreshToken = tokenDto.getRefreshToken();
        String token = tokenService.GetTokenFromRefreshToken(refreshToken);
        tokenService.RemoveToken(token);
        HashMap <String, String> responseMap = new HashMap<>();
        responseMap.put("message", "Remove token success.");
        return ResponseEntity.ok(responseMap);

    }

}
