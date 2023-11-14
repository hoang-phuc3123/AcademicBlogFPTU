package com.academicblogfptu.AcademicBlogFPTU.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MailStructureDto {
    private int triggerId;
    private String triggerName;
    private int receiverId;
    private String receiverName;
    private String mailType;
    private String receiverMail;
    private String subject;
    private String message;
    private String postLink;
}
