package com.academicblogfptu.AcademicBlogFPTU.dtos.CommentDtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReportedCommentDto {

    private Integer reportId;

    private String reportDate;

    private String reportType;

    private Integer reportedCommentId;

    private String content;

    private String reporterName;

    private String reasonOfReport;
}
