package com.academicblogfptu.AcademicBlogFPTU.controllers;


import com.academicblogfptu.AcademicBlogFPTU.config.UserAuthProvider;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos.LoginRequestDto;
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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
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
        UserDto userDto = new UserDto();
        if (loginRequestDto.getUsername().contains("@")) {
            userDto = userService.loginbyEmail(loginRequestDto);
        }
        else {
            userDto = userService.login(loginRequestDto);
        }
        String refreshToken = UUID.randomUUID().toString();
        String token = userAuthProvider.createToken(userDto.getUsername(),900000 * 2);
        userDto.setToken(token);
        userDto.setRefreshToken(refreshToken);
        tokenService.StoreToken(token, refreshToken);
        String ipAddress = getClientIpAddress();
        String cvt = convertIPv6ToIPv4(ipAddress);
        URL url = null;
        try {
            url = new URL("https://lvnsoft.store/TotalVisit/visit-count.php?ip=" + cvt);
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
                return ipv6Address;
            }
        } catch (UnknownHostException e) {
            return null;
        }
    }

}
