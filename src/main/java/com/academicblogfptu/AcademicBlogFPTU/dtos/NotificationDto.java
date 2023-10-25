package com.academicblogfptu.AcademicBlogFPTU.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationDto {

    private int notificationId;
    private String content;
    private int relatedId;
    private boolean isRead;
    private Timestamp notifyTime;
    private String type;
    private int triggerUser;
    private String fullNameOfTriggerUser;
    private String relatedUrl;
    private int userId;
}
