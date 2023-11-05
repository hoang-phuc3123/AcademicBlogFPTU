package com.academicblogfptu.AcademicBlogFPTU.dtos.NewsDtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NewsListDto {
    private int newsId;
    private String title;
    private String newsAt;
}
