package com.academicblogfptu.AcademicBlogFPTU.controllers;

import com.academicblogfptu.AcademicBlogFPTU.entities.PostEntity;
import com.academicblogfptu.AcademicBlogFPTU.services.UserServices;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class PostController {
    private UserServices userServices;

//    @GetMapping("/view-post")
//    public List<PostEntity> getPostList(){
//        List<PostEntity> list = userServices.getAll();
//        return list;
//    }

    @GetMapping("/view-post")
    public String viewPost(){
        return "view-post";
    }
}
