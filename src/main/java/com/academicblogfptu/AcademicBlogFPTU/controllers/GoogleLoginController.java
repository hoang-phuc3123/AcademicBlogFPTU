package com.academicblogfptu.AcademicBlogFPTU.controllers;

import com.academicblogfptu.AcademicBlogFPTU.config.UserAuthProvider;
import com.academicblogfptu.AcademicBlogFPTU.dtos.GoogleTokenDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.LoginRequestDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDetailsDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDto;
import com.academicblogfptu.AcademicBlogFPTU.services.UserServices;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;

import java.security.SecureRandom;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class GoogleLoginController {
    private final UserServices userService;
    private final UserAuthProvider userAuthProvider;

    @PostMapping("/google-login")
    public ResponseEntity<UserDto> loginGoogle(@RequestBody GoogleTokenDto googleTokenDto) {
        String token = googleTokenDto.getEmail();
        try {
            // Tạo URL cho yêu cầu
            URL url = new URL("https://www.googleapis.com/oauth2/v3/userinfo");
            // Mở kết nối HTTP
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + token);
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Xử lý phản hồi thành công
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                JsonParser jsonParser = JsonParserFactory.getJsonParser();
                Map<String, Object> jsonData = jsonParser.parseMap(response.toString());
                // Lấy giá trị email từ đối tượng Map
                String email = (String) jsonData.get("email");
                String name = (String) jsonData.get("given_name");
                String picture = (String) jsonData.get("picture");
                UserDetailsDto userDetailsDto = new UserDetailsDto(email, name, picture);
                LoginRequestDto loginDto = new LoginRequestDto(email, generateRandomPassword(10).toCharArray());
                UserDto userDto = userService.register(loginDto);
                userService.RegisterUserDetail(userDetailsDto);
                userDto.setToken(userAuthProvider.createToken(userDto.getUsername() , 3600000));
                return ResponseEntity.ok(userDto);
            } else {
                // Xử lý lỗi nếu yêu cầu không thành công
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        } catch (IOException e) {
            // Xử lý ngoại lệ nếu có lỗi trong quá trình gửi yêu cầu
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private String generateRandomPassword(int length) {
        final int passwordLength = length;
        final String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom secureRandom = new SecureRandom();
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < passwordLength; i++) {
            int randomIndex = secureRandom.nextInt(characters.length());
            char randomChar = characters.charAt(randomIndex);
            password.append(randomChar);
        }
        return password.toString();
    }

}