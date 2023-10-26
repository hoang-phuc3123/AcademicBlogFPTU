package com.academicblogfptu.AcademicBlogFPTU.controllers;


import com.academicblogfptu.AcademicBlogFPTU.config.UserAuthProvider;
import com.academicblogfptu.AcademicBlogFPTU.dtos.NotificationDto;
import com.academicblogfptu.AcademicBlogFPTU.entities.NotificationEntity;
import com.academicblogfptu.AcademicBlogFPTU.exceptions.AppException;
import com.academicblogfptu.AcademicBlogFPTU.services.NotificationServices;
import com.academicblogfptu.AcademicBlogFPTU.services.UserServices;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

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
        if (notifications.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/read")
    public ResponseEntity<String> readNotifications(@RequestHeader("Authorization") String headerValue, @RequestParam Integer id){
        NotificationEntity notification = notificationServices.getNotification(id);
        if(notification.getUser().getId()
                == userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))).getId()){
            NotificationEntity readNotification = notificationServices.readNotification(id);
            return ResponseEntity.ok(readNotification.getRelatedURL());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

   @PostMapping("/send")
   public ResponseEntity<String> sendNotification(@RequestHeader("Authorization") String headerValue, @RequestBody NotificationDto notificationDto){
       notificationDto.setTriggerUser(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))).getId());
       try{
           notificationServices.sendNotification(notificationDto);
       }catch(Exception e){
           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
       }
        return ResponseEntity.ok("Send notification success!");
   }


}
