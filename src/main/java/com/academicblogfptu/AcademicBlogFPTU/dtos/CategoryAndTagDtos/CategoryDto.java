package com.academicblogfptu.AcademicBlogFPTU.dtos.CategoryAndTagDtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryDto {
    private int id;
    private String categoryName;
    private String categoryType;
    private String majorName;
    private List<CategoryDto> childCategories;
}
