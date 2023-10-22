package com.academicblogfptu.AcademicBlogFPTU.services;

import com.academicblogfptu.AcademicBlogFPTU.entities.TokenEntity;
import com.academicblogfptu.AcademicBlogFPTU.exceptions.AppException;
import com.academicblogfptu.AcademicBlogFPTU.repositories.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class TokenServices {

    @Autowired
    private final TokenRepository tokenRepository;


    public boolean isTokenExist(String token) {
        Optional<TokenEntity> optionalToken = tokenRepository.findByToken(token);
        return optionalToken.isPresent();
    }

    public void StoreToken(String token, String refreshToken) {
        TokenEntity tokenEntity = new TokenEntity();
        tokenEntity.setToken(token);
        tokenEntity.setRefreshToken(refreshToken);
        long currentTimeMillis = System.currentTimeMillis();
        long expirationTimeMillis = currentTimeMillis + (24 * 60 * 60 * 1000); // 24 giờ * 60 phút * 60 giây * 1000 millis
        Timestamp expirationTimestamp = new Timestamp(expirationTimeMillis);
        // Chuyển java.util.Date thành java.sql.Timestamp để lưu vào cơ sở dữ liệu
        tokenEntity.setExpiredTime(expirationTimestamp);
        tokenRepository.save(tokenEntity);
    }

    public String GetTokenFromRefreshToken(String refreshToken) {
        TokenEntity oldToken = tokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new AppException ("Unknown refresh token", HttpStatus.FORBIDDEN));
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Timestamp expirationTime = oldToken.getExpiredTime();

        if (expirationTime != null && expirationTime.before(now)) {
            throw new AppException("The refresh token expired", HttpStatus.FORBIDDEN);
        }
        return oldToken.getToken();
    }



    public void RefreshToken(String token, String newToken ,String newRefreshToken) {
        TokenEntity oldToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new AppException ("Unknown token", HttpStatus.FORBIDDEN));
        oldToken.setToken(newToken);
        oldToken.setRefreshToken(newRefreshToken);
        long currentTimeMillis = System.currentTimeMillis();
        long expirationTimeMillis = currentTimeMillis + (24 * 60 * 60 * 1000);
        Timestamp expirationTimestamp = new Timestamp(expirationTimeMillis);
        oldToken.setExpiredTime(expirationTimestamp);
        tokenRepository.save(oldToken);
    }

    public void RemoveToken(String token) {
        TokenEntity tokenEntity = tokenRepository.findByToken(token)
                .orElseThrow(() -> new AppException("Token not found", HttpStatus.FORBIDDEN));
        tokenRepository.delete(tokenEntity);
    }

}
