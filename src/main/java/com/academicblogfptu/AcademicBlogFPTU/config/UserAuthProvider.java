package com.academicblogfptu.AcademicBlogFPTU.config;

import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos.GoogleUserDetailsDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos.UserDto;
import com.academicblogfptu.AcademicBlogFPTU.repositories.UserDetailsRepository;
import com.academicblogfptu.AcademicBlogFPTU.services.UserServices;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;

@RequiredArgsConstructor
@Component
public class UserAuthProvider {
    @Value("${security.jwt.token.secret-key:secret-key}")
    private String secretKey;

    private final UserServices userService;
    private final UserDetailsRepository userDetailsRepository;

    @PostConstruct
    protected void init() {
        // this is to avoid having the raw secret key available in the JVM
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    public String createToken(String username, int timeout) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + timeout); // 1 hour = 3600000

        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        return JWT.create()
                .withSubject(username)
                .withIssuedAt(now)
                .withExpiresAt(validity)
                .sign(algorithm);
    }

    public Authentication validateToken(String token) {
        Algorithm algorithm = Algorithm.HMAC256(secretKey);

        JWTVerifier verifier = JWT.require(algorithm)
                .build();

        DecodedJWT decoded = verifier.verify(token);

        UserDto user = userService.findByUsername(decoded.getSubject());

        return new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
    }

    public String getUser(String token) {
        Algorithm algorithm = Algorithm.HMAC256(secretKey);

        JWTVerifier verifier = JWT.require(algorithm)
                .build();

        DecodedJWT decoded = verifier.verify(token);

        return decoded.getSubject();
    }

    public boolean isBanned(String username) {
        UserDto userDto = userService.findByUsername(username);
        return userDto.isBanned();
    }

    public String getUserRefresh(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secretKey);

            // Sử dụng verifier để xác minh token
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT decoded = verifier.verify(token);

            return decoded.getSubject();
        } catch (Exception e) {
            // Nếu token đã hết hạn, vẫn giải mã để lấy username
        }
        DecodedJWT decoded = JWT.decode(token);
        return decoded.getSubject();
    }

    public Authentication validateTokenEmail(String token) {
        Algorithm algorithm = Algorithm.HMAC256(secretKey);

        JWTVerifier verifier = JWT.require(algorithm)
                .build();

        DecodedJWT decoded = verifier.verify(token);

        GoogleUserDetailsDto user = userService.findByEmail(decoded.getSubject());

        return new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
    }
}
