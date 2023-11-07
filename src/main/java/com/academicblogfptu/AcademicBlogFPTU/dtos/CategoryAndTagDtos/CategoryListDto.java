package com.academicblogfptu.AcademicBlogFPTU.dtos.CategoryAndTagDtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryListDto {
    private int categoryId;

    private String categoryName;

    private String categoryType;
}
