package com.academicblogfptu.AcademicBlogFPTU.controllers;

import com.academicblogfptu.AcademicBlogFPTU.config.UserAuthProvider;
import com.academicblogfptu.AcademicBlogFPTU.dtos.*;
import com.academicblogfptu.AcademicBlogFPTU.entities.PostEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.UserEntity;
import com.academicblogfptu.AcademicBlogFPTU.exceptions.AppException;
import com.academicblogfptu.AcademicBlogFPTU.repositories.UserRepository;
import com.academicblogfptu.AcademicBlogFPTU.services.PostServices;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.config.RepositoryConfigurationSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class PostController {

    @Autowired
    private final PostServices postServices;

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final UserAuthProvider userAuthProvider;

    @GetMapping("users/post-list")
    public ResponseEntity<List<PostListDto>> getPostList(){
        List<PostListDto> list = postServices.viewAllPost();
        return ResponseEntity.ok(list);
    }

    @PostMapping("users/view-post")
    public ResponseEntity<PostDto> viewAPost(@RequestBody PostDto postDto){
        PostDto post = postServices.viewPostById(postDto.getSlug());
        return ResponseEntity.ok(post);
    }

    @PostMapping("posts/delete")
    public ResponseEntity<Boolean> deletePostById(@RequestBody PostDto postId){
        postServices.deletePostById(postId.getPostId());
        return ResponseEntity.ok(true);
    }

    @PostMapping("users/request-post")
    public ResponseEntity<PostDto> requestPost(@RequestHeader("Authorization") String headerValue,@RequestBody RequestPostDto requestPostDto){
        Optional<UserEntity> user = userRepository.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", "")));
        UserEntity userEntity = user.get();

        PostDto newPost = postServices.requestPost(requestPostDto, userEntity);
        if (userEntity.getRole().getRoleName().equalsIgnoreCase("lecturer")){
            postServices.postDetailLecturer(newPost.getPostId());
        }else  {
            postServices.postDetail(newPost.getPostId());
        }
        return ResponseEntity.ok(newPost);
    }

    @PostMapping("posts/edit")
    public ResponseEntity<PostDto> editPost(@RequestHeader("Authorization") String headerValue,@RequestBody EditPostDto editPostDto){
        Optional<UserEntity> user = userRepository.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", "")));
        UserEntity userEntity = user.get();

        PostDto post = postServices.editPost(editPostDto);
        if (userEntity.getRole().getRoleName().equalsIgnoreCase("lecturer")){
            postServices.postDetailLecturer(post.getPostId());
        }else {
            postServices.postDetail(post.getPostId());
        }
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

    @GetMapping("posts/trending")
    public ResponseEntity<List<PostListTrendingDto>> viewTrendingPost(){
        List<PostListTrendingDto> list = postServices.viewTrending();
        return ResponseEntity.ok(list);
    }

    @GetMapping("posts/shorts")
    public ResponseEntity<List<PostListDto>> viewShortPost(){
        List<PostListDto> list = postServices.viewShort();
        return ResponseEntity.ok(list);
    }

    @GetMapping("posts/q-a")
    public ResponseEntity<List<QuestionAnswerDto>> viewQuestionAndAnswerPost(){
        List<QuestionAnswerDto> list = postServices.viewQuestionAndAnswerPost();
        return ResponseEntity.ok(list);
    }

    @PostMapping("posts/post-history")
    public ResponseEntity<List<PostDto>> viewPostEditHistory(@RequestBody PostDto postId){
        List<PostDto> postEditHistoryList = postServices.viewPostEditHistory(postId.getPostId());
        return ResponseEntity.ok(postEditHistoryList);
    }
}
