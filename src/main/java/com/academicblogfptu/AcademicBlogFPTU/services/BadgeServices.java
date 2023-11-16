package com.academicblogfptu.AcademicBlogFPTU.services;

import com.academicblogfptu.AcademicBlogFPTU.entities.BadgeEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.UserBadgeEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.UserEntity;
import com.academicblogfptu.AcademicBlogFPTU.exceptions.AppException;
import com.academicblogfptu.AcademicBlogFPTU.repositories.BadgeRepository;
import com.academicblogfptu.AcademicBlogFPTU.repositories.UserBadgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

    public void setRoleBadge(UserEntity user){
        UserBadgeEntity userBadge = new UserBadgeEntity();
        if(user.getRole().getRoleName().equalsIgnoreCase("lecturer")){
            BadgeEntity badge = badgeRepository.findByBadgeName(user.getRole().getRoleName()).orElseThrow(()-> new AppException("Unknown badge!!", HttpStatus.NOT_FOUND));
            userBadge.setBadge(badge);
        }else if(user.getRole().getRoleName().equalsIgnoreCase("mentor")){
            BadgeEntity badge = badgeRepository.findByBadgeName(user.getRole().getRoleName()).orElseThrow(()-> new AppException("Unknown badge!!", HttpStatus.NOT_FOUND));
            userBadge.setBadge(badge);
        }else{
            return;
        }
        userBadge.setUser(user);
        userBadgeRepository.save(userBadge);
    }

    public void adjustUserRoleBadge(String roleBefore,UserEntity user){
        if((roleBefore.equalsIgnoreCase("lecturer") || roleBefore.equalsIgnoreCase("mentor"))
                && ((user.getRole().getRoleName().equalsIgnoreCase("admin") || user.getRole().getRoleName().equalsIgnoreCase("student"))) ){
            removeUserRoleBadge(user,roleBefore);
        }else if((roleBefore.equalsIgnoreCase("admin") || roleBefore.equalsIgnoreCase("student"))
                && ((user.getRole().getRoleName().equalsIgnoreCase("lecturer") || user.getRole().getRoleName().equalsIgnoreCase("mentor"))) ){
            setRoleBadge(user);
        } else if ((roleBefore.equalsIgnoreCase("lecturer") && user.getRole().getRoleName().equalsIgnoreCase("mentor"))
                || (roleBefore.equalsIgnoreCase("mentor") && user.getRole().getRoleName().equalsIgnoreCase("lecturer"))) {
            changeUserRoleBadge(user,roleBefore);
        }
    }

    public void removeUserRoleBadge(UserEntity user,String roleBefore){
        BadgeEntity badge = badgeRepository.findByBadgeName(roleBefore).orElseThrow(()-> new AppException("Unknown badge!!",HttpStatus.NOT_FOUND));
        UserBadgeEntity removeBadge = userBadgeRepository.findByUserIdAndBadgeId(user.getId(), badge.getId()).orElseThrow(()-> new AppException("Unknown user badge!!",HttpStatus.NOT_FOUND));
        userBadgeRepository.delete(removeBadge);
    }

    public void changeUserRoleBadge(UserEntity user,String roleBefore){
        BadgeEntity newBadge = badgeRepository.findByBadgeName(user.getRole().getRoleName()).orElseThrow(() -> new AppException("Unknown badge!!!",HttpStatus.NOT_FOUND));
        BadgeEntity oldBadge = badgeRepository.findByBadgeName(roleBefore).orElseThrow(() -> new AppException("Unknown badge!!!",HttpStatus.NOT_FOUND));
        UserBadgeEntity badgeBefore = userBadgeRepository.findByUserIdAndBadgeId(user.getId(), oldBadge.getId()).orElseThrow(()-> new AppException("Unknown user badge!!",HttpStatus.NOT_FOUND));
        badgeBefore.setBadge(newBadge);
        userBadgeRepository.save(badgeBefore);
    }






}
