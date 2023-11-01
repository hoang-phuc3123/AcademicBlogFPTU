package com.academicblogfptu.AcademicBlogFPTU.dtos.FavoriteDtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FavoriteDto {

    private int favoriteId;
    private String saveAt;
    private int postId;
    private int userId;

}
