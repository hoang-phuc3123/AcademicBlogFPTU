package com.academicblogfptu.AcademicBlogFPTU.dtos.MajorDtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MajorDto {

    private int id;
    private String majorName;

}
