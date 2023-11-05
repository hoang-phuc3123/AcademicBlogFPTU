package com.academicblogfptu.AcademicBlogFPTU.dtos.NewsDtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NewsDto {
    private int newsId;
    private String title;
    private String content;
    private String newsAt;
    private String sentBy;
}
