package com.academicblogfptu.AcademicBlogFPTU.controllers;

import com.academicblogfptu.AcademicBlogFPTU.dtos.*;
import com.academicblogfptu.AcademicBlogFPTU.entities.PostEntity;
import com.academicblogfptu.AcademicBlogFPTU.services.PostServices;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PostController {

    @Autowired
    private final PostServices postServices;

    @GetMapping("users/post-list")
    public ResponseEntity<List<PostListDto>> getPostList(){
        List<PostListDto> list = postServices.viewAllPost();
        return ResponseEntity.ok(list);
    }

    @PostMapping("users/view-post")
    public ResponseEntity<PostDto> viewAPost(@RequestBody PostDto postId){
        PostDto post = postServices.viewPostById(postId.getPostId());
        return ResponseEntity.ok(post);
    }

    @PostMapping("posts/delete")
    public ResponseEntity<HttpStatus> deletePostById(@RequestBody PostDto postId){
        postServices.deletePostById(postId.getPostId());
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @PostMapping("users/request-post")
    public ResponseEntity<PostDto> requestPost(@RequestBody RequestPostDto requestPostDto){
        PostDto newPost = postServices.requestPost(requestPostDto);
        postServices.postDetail(newPost.getPostId());
        return ResponseEntity.ok(newPost);
    }
}
