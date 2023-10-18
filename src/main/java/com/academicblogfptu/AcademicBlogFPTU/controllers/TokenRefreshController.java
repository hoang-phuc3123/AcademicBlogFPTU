package com.academicblogfptu.AcademicBlogFPTU.controllers;

import com.academicblogfptu.AcademicBlogFPTU.config.UserAuthProvider;
import com.academicblogfptu.AcademicBlogFPTU.dtos.TokenDto;
import com.academicblogfptu.AcademicBlogFPTU.repositories.TokenRepository;
import com.academicblogfptu.AcademicBlogFPTU.services.TokenServices;
import com.academicblogfptu.AcademicBlogFPTU.services.UserServices;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class TokenRefreshController {


    @Autowired
    private final TokenServices tokenService;

    @Autowired
    private final UserAuthProvider userAuthProvider;

    @PostMapping("/refresh-token")
    public ResponseEntity<HashMap<String, String>> RefreshToken(@RequestBody TokenDto tokenDto) {

        String oldToken = tokenDto.getToken();
        String user = userAuthProvider.getUserRefresh(oldToken);
        String newToken = userAuthProvider.createToken(user, 3600000);
        tokenService.RefreshToken(oldToken, newToken);
        HashMap <String, String> responseMap = new HashMap<>();
        responseMap.put("token", newToken);
        return ResponseEntity.ok(responseMap);

    }

    @PostMapping("/remove-token")
    public ResponseEntity<HashMap<String, String>> RemoveToken(@RequestBody TokenDto tokenDto) {

        String token = tokenDto.getToken();
        tokenService.RemoveToken(token);
        HashMap <String, String> responseMap = new HashMap<>();
        responseMap.put("message", "Remove token success.");
        return ResponseEntity.ok(responseMap);

    }


}
