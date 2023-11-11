package com.academicblogfptu.AcademicBlogFPTU.dtos.PostDtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchMultipleResultDto {
    List<PostListDto> postList;
    List<QuestionAnswerDto> qaList;
}
