package com.academicblogfptu.AcademicBlogFPTU.controllers;

import com.academicblogfptu.AcademicBlogFPTU.config.UserAuthProvider;
import com.academicblogfptu.AcademicBlogFPTU.dtos.NewsDtos.NewsDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.NewsDtos.NewsListDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos.UserDto;
import com.academicblogfptu.AcademicBlogFPTU.repositories.UserRepository;
import com.academicblogfptu.AcademicBlogFPTU.services.NewsServices;
import com.academicblogfptu.AcademicBlogFPTU.services.UserServices;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class NewsController {
    @Autowired
    private final NewsServices newsServices;
    @Autowired
    private final UserAuthProvider userAuthProvider;
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final UserServices userServices;

    public boolean isAdmin(UserDto userDto) {
        return userDto.getRoleName().equals("admin");
    }
    @GetMapping("news/list")
    public ResponseEntity<List<NewsListDto>> getNewsList(){
        List<NewsListDto> list = newsServices.getNewsList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("news/view")
    public ResponseEntity<NewsDto> viewNews(@RequestParam int id){
        NewsDto news = newsServices.viewNews(id);
        return ResponseEntity.ok(news);
    }

    @PostMapping("admin/add-news")
    public ResponseEntity<String> addNews(@RequestHeader("Authorization") String headerValue, @RequestBody NewsDto newsDto){
        int id = userServices.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))).getId();
        if(isAdmin(userServices.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))){
            newsServices.addNews(newsDto,id);
            return ResponseEntity.ok("Success");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("admin/delete-news")
    public ResponseEntity<String> deleteNews(@RequestHeader("Authorization") String headerValue, @RequestBody NewsDto newsDto){
        if(isAdmin(userServices.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))){
            newsServices.deleteNews(newsDto.getNewsId());
            return ResponseEntity.ok("Success");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

}
