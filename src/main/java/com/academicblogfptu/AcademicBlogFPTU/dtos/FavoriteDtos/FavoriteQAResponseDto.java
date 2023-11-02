package com.academicblogfptu.AcademicBlogFPTU.dtos.FavoriteDtos;

import com.academicblogfptu.AcademicBlogFPTU.dtos.PostDtos.PostListDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.PostDtos.QuestionAnswerDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FavoriteQAResponseDto {
    private int favoriteId;
    private String saveAt;
    private QuestionAnswerDto questionAnswerDto;
}
