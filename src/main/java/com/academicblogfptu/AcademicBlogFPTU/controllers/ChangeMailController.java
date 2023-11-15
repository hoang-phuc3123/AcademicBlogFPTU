package com.academicblogfptu.AcademicBlogFPTU.controllers;

import com.academicblogfptu.AcademicBlogFPTU.config.UserAuthProvider;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos.GoogleTokenDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos.ResetPasswordDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos.UserDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos.VerifyCodeDto;
import com.academicblogfptu.AcademicBlogFPTU.entities.UserDetailsEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.UserEntity;
import com.academicblogfptu.AcademicBlogFPTU.exceptions.AppException;
import com.academicblogfptu.AcademicBlogFPTU.repositories.UserDetailsRepository;
import com.academicblogfptu.AcademicBlogFPTU.repositories.UserRepository;
import com.academicblogfptu.AcademicBlogFPTU.services.TokenServices;
import com.academicblogfptu.AcademicBlogFPTU.services.UserServices;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.*;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;

import java.util.*;
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class ChangeMailController {
    private final UserServices userService;
    private final UserAuthProvider userAuthProvider;
    private final TokenServices tokenService;
    private final UserDetailsRepository userDetailsRepository;
    private final UserRepository userRepository;
    @PostMapping("/send-verify-code")
    public ResponseEntity<HashMap<String,String>> ResetPass(@RequestBody GoogleTokenDto googleTokenDto) {
        String email = googleTokenDto.getEmail();
        try {
            // Tạo URL cho yêu cầu
            URL url = new URL("https://lvnsoft.store/change-mail/send-code.php");
            // Mở kết nối HTTP
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            // Thiết lập phương thức POST
            connection.setRequestMethod("POST");
            // Thiết lập Header để chỉ định Content-Type là application/x-www-form-urlencoded
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            // Tạo dữ liệu POST theo định dạng x-www-form-urlencoded
            String postData = "email=" + email;
            // Ghi dữ liệu POST vào body của yêu cầu
            connection.setDoOutput(true);
            OutputStream os = connection.getOutputStream();
            os.write(postData.getBytes("UTF-8"));
            os.flush();
            os.close();
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
                Map < String, Object > jsonData = jsonParser.parseMap(response.toString());
                // Lấy giá trị email từ đối tượng Map
                String code = (String) jsonData.get("verificationCode");
                if (code.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                } else {
                    HashMap <String, String> responseMap = new HashMap < > ();
                    responseMap.put("message", "true");
                    // Tạo một ResponseEntity với HttpStatus.OK và dữ liệu JSON
                    return ResponseEntity.ok(responseMap);
                }
            } else {
                // Xử lý lỗi nếu yêu cầu không thành công
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        } catch (IOException e) {
            // Xử lý ngoại lệ nếu có lỗi trong quá trình gửi yêu cầu
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/verify-code-mail")
    public ResponseEntity<HashMap<String, String>> VerifyCode(@RequestBody VerifyCodeDto verifyCodeDto) {
        try {
            URL url = new URL("https://lvnsoft.store/change-mail/verify-code.php");
            // Mở kết nối HTTP
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Thiết lập phương thức POST
            connection.setRequestMethod("POST");

            // Thiết lập Header để chỉ định Content-Type là application/x-www-form-urlencoded
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            // Tạo dữ liệu POST theo định dạng x-www-form-urlencoded

            // Kết hợp các giá trị đã mã hóa vào chuỗi POST data
            String postData = "email=" + verifyCodeDto.getEmail() + "&code=" + verifyCodeDto.getCode();

            // Ghi dữ liệu POST vào body của yêu cầu
            connection.setDoOutput(true);
            OutputStream os = connection.getOutputStream();
            os.write(postData.getBytes("UTF-8"));
            os.flush();
            os.close();
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
                Map < String, Object > jsonData = jsonParser.parseMap(response.toString());
                String msg = (String) jsonData.get("status");
                HashMap < String, String > responseMap = new HashMap < > ();
                responseMap.put("message", msg);
                if (msg.equals("true")) {
                    return ResponseEntity.ok(responseMap);
                } else return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            } else {
                // Xử lý lỗi nếu yêu cầu không thành công
                return ResponseEntity.status(responseCode).build();
            }
        } catch (IOException e) {
            // Xử lý ngoại lệ nếu có lỗi trong quá trình gửi yêu cầu
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/change-mail")
    public ResponseEntity<String> ChangeMail(@RequestHeader("Authorization") String headerValue , @RequestBody GoogleTokenDto googleTokenDto) {
        UserEntity user = userRepository.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", "")))
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.UNAUTHORIZED));
        UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.UNAUTHORIZED));
        userDetails.setEmail(googleTokenDto.getEmail());
        userDetailsRepository.save(userDetails);
        return ResponseEntity.ok("success");
    }

}