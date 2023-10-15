package com.academicblogfptu.AcademicBlogFPTU.controllers;

import com.academicblogfptu.AcademicBlogFPTU.dtos.PostDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDto;
import com.academicblogfptu.AcademicBlogFPTU.entities.PostEntity;
import com.academicblogfptu.AcademicBlogFPTU.services.PostServices;
import com.academicblogfptu.AcademicBlogFPTU.services.UserServices;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class PostController {

    @Autowired
    private final PostServices postServices;

    @GetMapping("/view-post")
    public ResponseEntity<List<PostDto>> getPostList(){
        List<PostDto> list = postServices.viewAllPost();
        return ResponseEntity.ok(list);
    }


}
