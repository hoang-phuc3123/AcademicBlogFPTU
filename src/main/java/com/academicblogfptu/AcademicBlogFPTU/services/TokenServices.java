package com.academicblogfptu.AcademicBlogFPTU.services;

import com.academicblogfptu.AcademicBlogFPTU.entities.TokenEntity;
import com.academicblogfptu.AcademicBlogFPTU.exceptions.AppException;
import com.academicblogfptu.AcademicBlogFPTU.repositories.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

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

    public void StoreToken(String token) {
        TokenEntity tokenEntity = new TokenEntity();
        tokenEntity.setToken(token);
        tokenRepository.save(tokenEntity);
    }

    public void RefreshToken(String token, String newToken) {
        TokenEntity oldToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new AppException ("Unknown token", HttpStatus.UNAUTHORIZED));
        oldToken.setToken(newToken);
        tokenRepository.save(oldToken);
    }

    public void RemoveToken(String token) {
        TokenEntity tokenEntity = tokenRepository.findByToken(token)
                .orElseThrow(() -> new AppException("Token not found", HttpStatus.NOT_FOUND));

        tokenRepository.delete(tokenEntity);
    }

}
