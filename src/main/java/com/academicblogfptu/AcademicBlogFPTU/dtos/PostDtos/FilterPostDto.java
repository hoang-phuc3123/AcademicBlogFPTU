package com.academicblogfptu.AcademicBlogFPTU.dtos.PostDtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FilterPostDto {
    private Integer categoryId;

    private Integer tagId;

    private String title;

    private String skill;
}
