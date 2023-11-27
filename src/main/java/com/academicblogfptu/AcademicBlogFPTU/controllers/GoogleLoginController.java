package com.academicblogfptu.AcademicBlogFPTU.controllers;

import com.academicblogfptu.AcademicBlogFPTU.config.UserAuthProvider;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos.GoogleTokenDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos.LoginRequestDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos.GoogleUserDetailsDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos.UserDto;
import com.academicblogfptu.AcademicBlogFPTU.services.TokenServices;
import com.academicblogfptu.AcademicBlogFPTU.services.UserServices;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
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
    private final TokenServices tokenService;

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
                if (!picture.isEmpty()) {
                    URL imageUploadUrl = new URL("https://lvnsoft.store/save-image.php");
                    HttpURLConnection imageConnection = (HttpURLConnection) imageUploadUrl.openConnection();
                    imageConnection.setRequestMethod("POST");
                    String imageData = "imageUrl=" + picture;
                    imageConnection.setDoOutput(true);
                    imageConnection.getOutputStream().write(imageData.getBytes("UTF-8"));
                    int imageResponseCode = imageConnection.getResponseCode();
                    if (imageResponseCode == HttpURLConnection.HTTP_OK) {
                        // Xử lý phản hồi thành công
                        BufferedReader imageReader = new BufferedReader(new InputStreamReader(imageConnection.getInputStream()));
                        StringBuilder imageResponse = new StringBuilder();
                        String imageLine;
                        while ((imageLine = imageReader.readLine()) != null) {
                            imageResponse.append(imageLine);
                        }
                        imageReader.close();
                        if (imageResponse.toString().contains("Upload")) {
                            picture = imageResponse.toString();
                        }
                    }
                }
                else {
                    picture = null;
                }
                GoogleUserDetailsDto userDetailsDto = new GoogleUserDetailsDto(email, name, picture);
                LoginRequestDto loginDto = new LoginRequestDto(email, generateRandomPassword(10).toCharArray());
                UserDto userDto = userService.register(loginDto);
                userService.RegisterUserDetail(userDetailsDto);
                String accessToken = userAuthProvider.createToken(userDto.getUsername() , 900000 * 2);
                userDto.setToken(accessToken);
                String refreshToken = UUID.randomUUID().toString();
                userDto.setRefreshToken(refreshToken);
                tokenService.StoreToken(accessToken, refreshToken);
                String ipAddress = getClientIpAddress();
                String cvt = convertIPv6ToIPv4(ipAddress);
                URL url_ = null;
                try {
                    url_ = new URL("https://lvnsoft.store/TotalVisit/visit-count.php?ip=" + cvt);
                    HttpURLConnection connection_ = (HttpURLConnection) url.openConnection();
                    connection_.setRequestMethod("GET");
                    int responseCode_ = connection.getResponseCode();
                    if (responseCode_ == HttpURLConnection.HTTP_OK) {
                        return ResponseEntity.ok(userDto);
                    }
                    else {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                    }
                } catch (Exception e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }
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

    private String getClientIpAddress() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader != null && !xForwardedForHeader.isEmpty()) {
            return xForwardedForHeader.split(",")[0].trim();
        }
        String remoteAddr = request.getRemoteAddr();
        if (remoteAddr != null && !remoteAddr.isEmpty()) {
            return remoteAddr;
        }
        return "Unknown";
    }

    public static String convertIPv6ToIPv4(String ipv6Address) {
        try {
            InetAddress inet6Address = InetAddress.getByName(ipv6Address);
            if (inet6Address instanceof Inet6Address) {
                Inet6Address ipv6 = (Inet6Address) inet6Address;
                byte[] ipv4Bytes = new byte[4];
                System.arraycopy(ipv6.getAddress(), 12, ipv4Bytes, 0, 4);
                InetAddress ipv4Address = InetAddress.getByAddress(ipv4Bytes);
                return ipv4Address.getHostAddress();
            } else {
                return ipv6Address; // Not an IPv6 address
            }
        } catch (UnknownHostException e) {
            e.printStackTrace(); // Handle the exception based on your needs
            return null;
        }
    }

}