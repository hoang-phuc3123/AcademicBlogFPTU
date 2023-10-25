package com.academicblogfptu.AcademicBlogFPTU.services;

import com.academicblogfptu.AcademicBlogFPTU.dtos.NotificationDto;
import com.academicblogfptu.AcademicBlogFPTU.entities.CommentEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.NotificationEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.UserDetailsEntity;
import com.academicblogfptu.AcademicBlogFPTU.exceptions.AppException;
import com.academicblogfptu.AcademicBlogFPTU.repositories.CommentRepository;
import com.academicblogfptu.AcademicBlogFPTU.repositories.NotificationRepository;
import com.academicblogfptu.AcademicBlogFPTU.repositories.UserDetailsRepository;
import com.academicblogfptu.AcademicBlogFPTU.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationServices {

    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserDetailsRepository userDetailsRepository;

    @Autowired
    private CommentRepository commentRepository;

    public NotificationEntity sendNotification(NotificationDto notificationDto){
        NotificationEntity notification = new NotificationEntity();

        notification.setContent(notificationDto.getContent());
        notification.setRead(false);
        notification.setType(notificationDto.getType());
        notification.setNotifyAt(Timestamp.valueOf(LocalDateTime.now()));
        notification.setRelatedId(notificationDto.getRelatedId());
        notification.setTriggerUser(notificationDto.getTriggerUser());
        notification.setUser(userRepository.findById(notificationDto.getUserId()).orElseThrow(() -> new AppException("Unknown user", HttpStatus.UNAUTHORIZED)));

        if(notificationDto.getType().equals("post")) {
            notification.setRelatedURL("users/view-post?id=" + notificationDto.getRelatedId());
        } else if (notificationDto.getType().equals("comment")) {
            // add after finish comment api
            CommentEntity comment = new CommentEntity();
            comment = commentRepository.findById(notificationDto.getRelatedId()).orElseThrow(() -> new AppException("Unknown comment", HttpStatus.UNAUTHORIZED));
            notification.setRelatedURL("users/view-post?id=" + comment.getPost().getId());

        }
        notificationRepository.save(notification);

        return notification;
    }

    public NotificationEntity readNotification(int id){
        NotificationEntity notification = notificationRepository.findById(id).orElseThrow(() -> new AppException("Unknown notification", HttpStatus.UNAUTHORIZED));
        if(!notification.isRead()){
            notification.setRead(true);
            notificationRepository.save(notification);
        }
        return notification;
    }

    public List<NotificationDto> getAllNotification(int id){
        List<NotificationEntity> notifications = notificationRepository.findAllByUserId(id);
        List<NotificationDto> notificationList = new ArrayList<>();
        for (NotificationEntity notificationEntity: notifications) {
            notificationList.add(mapToDto(notificationEntity));
        }
        return notificationList;
    }

    public NotificationEntity getNotification(int id){
        return notificationRepository.findById(id).orElseThrow(() -> new AppException("Unknown notification",HttpStatus.UNAUTHORIZED));
    }

    private NotificationDto mapToDto(NotificationEntity notificationEntity){

        NotificationDto notification = new NotificationDto();
        notification.setNotificationId(notificationEntity.getId());
        notification.setContent(notificationEntity.getContent());
        notification.setRead(notificationEntity.isRead()); ;
        notification.setRelatedId(notificationEntity.getRelatedId());
        notification.setType(notificationEntity.getType());
        notification.setNotifyTime(notificationEntity.getNotifyAt());
        notification.setRelatedUrl(notificationEntity.getRelatedURL());
        notification.setUserId(notificationEntity.getUser().getId());
        notification.setFullNameOfTriggerUser(userDetailsRepository.findByUserId(notificationEntity.getTriggerUser()).getFullName());

        return notification;
    }



}
