package com.academicblogfptu.AcademicBlogFPTU.dtos.CommentDtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReportCommentDto {
    private int commentId;
    private int reasonOfReportId;
}

