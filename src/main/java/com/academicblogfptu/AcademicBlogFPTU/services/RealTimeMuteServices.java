package com.academicblogfptu.AcademicBlogFPTU.services;

import com.academicblogfptu.AcademicBlogFPTU.entities.UserDetailsEntity;
import com.academicblogfptu.AcademicBlogFPTU.repositories.UserDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
public class RealTimeMuteServices {
    @Autowired
    private UserDetailsRepository userDetailsRepository;

    @Scheduled(fixedDelay = 60000) // Chạy mỗi 1 phút (60,000 mili giây)
    @Transactional
    public void checkMuteStatus() {
        // Lấy danh sách user_details từ cơ sở dữ liệu
        List<UserDetailsEntity> userDetailsList = userDetailsRepository.findAll();
        for (UserDetailsEntity userDetails : userDetailsList) {
            if (userDetails.isMuted()) {
                long currentTimeMillis = System.currentTimeMillis() / 1000; // Thời gian hiện tại ở định dạng Unix time
                if (userDetails.getMutetime() != null) {
                    long muteTimeMillis = userDetails.getMutetime().getTime() / 1000; // Chuyển đổi Timestamp thành Unix time
                    if (muteTimeMillis < currentTimeMillis) {
                        userDetails.setMuted(false);
                        userDetails.setMutetime(null);
                    }
                }
            }
        }
    }
}
