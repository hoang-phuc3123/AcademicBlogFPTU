package com.academicblogfptu.AcademicBlogFPTU.controllers;


import com.academicblogfptu.AcademicBlogFPTU.config.MyHandler;
import com.academicblogfptu.AcademicBlogFPTU.config.UserAuthProvider;
import com.academicblogfptu.AcademicBlogFPTU.dtos.CommentDtos.CommentDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.NotificationDtos.Notification;
import com.academicblogfptu.AcademicBlogFPTU.dtos.NotificationDtos.NotificationDto;
import com.academicblogfptu.AcademicBlogFPTU.entities.NotificationEntity;
import com.academicblogfptu.AcademicBlogFPTU.services.NotificationServices;
import com.academicblogfptu.AcademicBlogFPTU.services.UserServices;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {
    @Autowired
    private NotificationServices notificationServices;
    @Autowired
    private final UserServices userService;
    @Autowired
    private final UserAuthProvider userAuthProvider;


    @GetMapping("/view")
    public ResponseEntity<List<NotificationDto>> getAllNotifications(@RequestHeader("Authorization") String headerValue){
        List<NotificationDto> notifications = notificationServices.getAllNotification(
                userService.findByUsername(
                        userAuthProvider.getUser(
                                headerValue.replace("Bearer ", "")
                        )).getId());
        return ResponseEntity.ok(notifications);
    }

    @PostMapping("/read")
    public ResponseEntity<String> readNotifications(@RequestHeader("Authorization") String headerValue, @RequestBody NotificationDto notificationDto){
        NotificationEntity notification = notificationServices.getNotification(notificationDto.getNotificationId());
        if(notification.getUser().getId()
                == userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))).getId()){
            NotificationEntity readNotification = notificationServices.readNotification(notificationDto.getNotificationId());
            return ResponseEntity.ok(readNotification.getRelatedURL());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

   @PostMapping("/send")
   public ResponseEntity<String> sendNotification(@RequestHeader("Authorization") String headerValue, @RequestBody NotificationDto notificationDto){
       notificationDto.setTriggerUser(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))).getId());

       NotificationDto newNotify = notificationServices.sendNotification(notificationDto);
       notificationServices.sendNotificationRealtime(notificationDto.getUserId(),newNotify);
       return ResponseEntity.ok("Send notification success!");

   }
    @PostMapping("/delete")
    public ResponseEntity<String> deleteNotification(@RequestBody NotificationDto notificationDto){
        notificationServices.deleteNotification(notificationDto);
        return ResponseEntity.ok("Success");
    }



   @PostMapping("/delete-post-notify")
   public ResponseEntity<String> deleteDeclineNotification(@RequestBody NotificationDto notificationDto){
        notificationServices.deletePostNotification(notificationDto);
        return ResponseEntity.ok("Success");
   }

    @PostMapping("/delete-comment-notify")
    public ResponseEntity<String> deleteDeletedCommentNotification(@RequestBody CommentDto comment){
        notificationServices.deleteDeletedCommentNotification(comment.getCommentId());
        return ResponseEntity.ok("Success");
    }



}
