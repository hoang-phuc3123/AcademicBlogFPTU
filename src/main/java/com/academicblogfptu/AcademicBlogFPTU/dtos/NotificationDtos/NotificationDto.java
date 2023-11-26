package com.academicblogfptu.AcademicBlogFPTU.dtos.NotificationDtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationDto {

    private int notificationId;
    private String content;
    private int relatedId;
    private boolean isRead;
    private String notifyTime;
    private String type;
    private int triggerUser;
    private String fullNameOfTriggerUser;
    private String avatarOfTriggerUser;
    private String relatedUrl;
    private int userId;
    private int commentId;
}
