package com.academicblogfptu.AcademicBlogFPTU.services;

import com.academicblogfptu.AcademicBlogFPTU.entities.BadgeEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.UserBadgeEntity;
import com.academicblogfptu.AcademicBlogFPTU.repositories.BadgeRepository;
import com.academicblogfptu.AcademicBlogFPTU.repositories.UserBadgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BadgeServices {

    @Autowired
    private UserBadgeRepository userBadgeRepository;
    @Autowired
    private BadgeRepository badgeRepository;


    public List<BadgeEntity> findBadgesByUserId(Integer userId) {
        List<UserBadgeEntity> userBadges = userBadgeRepository.findByUserId(userId);
        List<BadgeEntity> badges = userBadges.stream()
                .map(UserBadgeEntity::getBadge)
                .collect(Collectors.toList());
        return badges;
    }




}
