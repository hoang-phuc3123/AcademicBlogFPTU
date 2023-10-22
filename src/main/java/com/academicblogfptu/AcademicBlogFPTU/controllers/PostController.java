package com.academicblogfptu.AcademicBlogFPTU.controllers;

import com.academicblogfptu.AcademicBlogFPTU.dtos.*;
import com.academicblogfptu.AcademicBlogFPTU.entities.PostEntity;
import com.academicblogfptu.AcademicBlogFPTU.exceptions.AppException;
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

    @GetMapping("users/view-post")
    public ResponseEntity<PostDto> viewAPost(@RequestParam int postId){
        PostDto post = postServices.viewPostById(postId);
        return ResponseEntity.ok(post);
    }

    @PostMapping("posts/delete")
    public ResponseEntity<Boolean> deletePostById(@RequestBody PostDto postId){
        postServices.deletePostById(postId.getPostId());
        return ResponseEntity.ok(true);
    }

    @PostMapping("users/request-post")
    public ResponseEntity<PostDto> requestPost(@RequestBody RequestPostDto requestPostDto){
        PostDto newPost = postServices.requestPost(requestPostDto);
        postServices.postDetail(newPost.getPostId());
        return ResponseEntity.ok(newPost);
    }

    @PostMapping("posts/edit")
    public ResponseEntity<PostDto> editPost(@RequestBody EditPostDto editPostDto){
        PostDto post = postServices.editPost(editPostDto);
        return ResponseEntity.ok(post);
    }

    @GetMapping("posts/latest")
    public ResponseEntity<List<PostListDto>> viewLatestPost(){
        List<PostListDto> list = postServices.viewByLatestPost();
        return ResponseEntity.ok(list);
    }

    @GetMapping("posts/rewarded")
    public ResponseEntity<List<PostListDto>> viewRewardedPost(){
        List<PostListDto> list = postServices.viewRewardedPost();
        return ResponseEntity.ok(list);
    }

    @GetMapping("posts/q-a")
    public ResponseEntity<List<QuestionAnswerDto>> viewQuestionAndAnswerPost(){
        List<QuestionAnswerDto> list = postServices.viewQuestionAndAnswerPost();
        return ResponseEntity.ok(list);
    }
}
