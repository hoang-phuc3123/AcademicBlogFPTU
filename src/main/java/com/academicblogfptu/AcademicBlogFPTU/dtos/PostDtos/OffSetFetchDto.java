package com.academicblogfptu.AcademicBlogFPTU.dtos.PostDtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OffSetFetchDto {
    private int page;
    private int postOfPage;
}
