package com.academicblogfptu.AcademicBlogFPTU.services;

import com.academicblogfptu.AcademicBlogFPTU.entities.TokenEntity;
import com.academicblogfptu.AcademicBlogFPTU.repositories.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class CleanupTokenServices {

    @Autowired
    private TokenRepository tokenRepository;
    @Scheduled(fixedDelay = 60000) // Chạy mỗi 1 phút (60,000 mili giây)
    @Transactional
    public void CleanUpToken() {
        List<TokenEntity> tokenList = tokenRepository.findAll();
        for (TokenEntity tokenEntity : tokenList) {
            long currentTimeMillis = System.currentTimeMillis() / 1000;
            long muteTimeMillis = tokenEntity.getExpiredTime().getTime() / 1000;
            if (muteTimeMillis < currentTimeMillis) {
                tokenRepository.delete(tokenEntity);
            }
        }

    }

}
