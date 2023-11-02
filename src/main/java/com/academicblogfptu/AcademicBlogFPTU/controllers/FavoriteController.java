package com.academicblogfptu.AcademicBlogFPTU.controllers;

import com.academicblogfptu.AcademicBlogFPTU.config.UserAuthProvider;
import com.academicblogfptu.AcademicBlogFPTU.dtos.FavoriteDtos.FavoriteDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.FavoriteDtos.FavoritePostResponseDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.FavoriteDtos.FavoriteQAResponseDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.PostDtos.PostListDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.PostDtos.QuestionAnswerDto;
import com.academicblogfptu.AcademicBlogFPTU.services.FavoriteServices;
import com.academicblogfptu.AcademicBlogFPTU.services.UserServices;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/favorite")
public class FavoriteController {

    @Autowired
    private FavoriteServices favoriteServices;
    @Autowired
    private final UserAuthProvider userAuthProvider;
    @Autowired
    private final UserServices userService;

    @GetMapping("/posts")
    public ResponseEntity<List<FavoritePostResponseDto>> getFavoritePostList(@RequestHeader("Authorization") String headerValue){
        List<FavoritePostResponseDto> post = favoriteServices.getFavoritePostList(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))).getId());
        return ResponseEntity.ok(post);
    }
    @GetMapping("/q-a")
    public ResponseEntity<List<FavoriteQAResponseDto>> getFavoriteQAList(@RequestHeader("Authorization") String headerValue){
        List<FavoriteQAResponseDto> post = favoriteServices.getFavoriteQAList(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))).getId());
        return ResponseEntity.ok(post);
    }
    @PostMapping("/add")
    public ResponseEntity<String> addToFavorite(@RequestHeader("Authorization") String headerValue, @RequestBody FavoriteDto favoriteDto){
        favoriteDto.setUserId(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))).getId());
        favoriteServices.addFavorite(favoriteDto);
        return ResponseEntity.ok("Success");
    }

    @PostMapping("/remove")
    public ResponseEntity<String> removeFromFavorite(@RequestHeader("Authorization") String headerValue, @RequestBody FavoriteDto favoriteDto) {
        favoriteDto.setUserId(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))).getId());
        favoriteServices.removeFavorite(favoriteDto);
        return ResponseEntity.ok("Success");
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> checkWasFavorite(@RequestHeader("Authorization") String headerValue, @RequestBody FavoriteDto favoriteDto){
        favoriteDto.setUserId(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))).getId());
        Boolean check = favoriteServices.checkFavorite(favoriteDto);
        return ResponseEntity.ok(check);
    }

}
