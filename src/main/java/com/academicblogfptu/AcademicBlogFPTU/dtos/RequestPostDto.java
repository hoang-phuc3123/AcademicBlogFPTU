package com.academicblogfptu.AcademicBlogFPTU.dtos;

import com.academicblogfptu.AcademicBlogFPTU.entities.ImageEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestPostDto {
    private String title;
    private String description;
    private String content;
    private boolean allowComment;
    private int categoryId;
    private int tagId;
    private String coverURL;
    private String slug;
    private Integer length;
}
