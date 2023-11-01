package com.academicblogfptu.AcademicBlogFPTU.dtos.CategoryAndTagDtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryRequestDto {

    private String specialization;
    private String semester;
    private String subject;
    private int majorId;
}
