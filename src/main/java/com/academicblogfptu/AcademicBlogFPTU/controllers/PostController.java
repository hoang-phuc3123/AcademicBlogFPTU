package com.academicblogfptu.AcademicBlogFPTU.controllers;

import com.academicblogfptu.AcademicBlogFPTU.config.UserAuthProvider;
import com.academicblogfptu.AcademicBlogFPTU.dtos.PostDtos.*;
import com.academicblogfptu.AcademicBlogFPTU.dtos.SearchMultipleDto;
import com.academicblogfptu.AcademicBlogFPTU.entities.UserEntity;
import com.academicblogfptu.AcademicBlogFPTU.repositories.UserRepository;
import com.academicblogfptu.AcademicBlogFPTU.services.PostServices;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
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

    @PostMapping("users/post-list")
    public ResponseEntity<Map<String, Object>> getPostList(@RequestBody OffSetFetchDto offSetFetchDto){
        Map<String, Object> list = postServices.viewAllPost(offSetFetchDto.getPage(), offSetFetchDto.getPostOfPage());
        return ResponseEntity.ok(list);
    }

    @PostMapping("users/view-post")
    public ResponseEntity<PostDto> viewAPost(@RequestHeader("Authorization") String headerValue, @RequestBody PostDto postDto){
        Optional<UserEntity> user = userRepository.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", "")));
        UserEntity userEntity = user.get();

        PostDto post = postServices.viewPostBySlug(postDto.getSlug(), userEntity);
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
            postServices.postDetailLecturer(newPost.getPostId(), userEntity);
        }else  {
            postServices.postDetail(newPost.getPostId(), "Request");
        }
        return ResponseEntity.ok(newPost);
    }

    @PostMapping("posts/edit")
    public ResponseEntity<PostDto> editPost(@RequestHeader("Authorization") String headerValue,@RequestBody EditPostDto editPostDto){
        return ResponseEntity.ok(postServices.editPost(editPostDto));
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

    @PostMapping("posts/filter")
    public ResponseEntity<List<PostListDto>> viewFilteredPost(@RequestBody FilterPostDto filter){
        String title = (filter.getTitle() != null) ? filter.getTitle() : "";
        List<PostListDto> filterPost = postServices.filterPosts(filter.getCategoryId(), filter.getTagId(), title);
        return ResponseEntity.ok(filterPost);
    }

    @PostMapping("posts/filter-skill")
    public ResponseEntity<List<PostListDto>> viewFilteredPostBySkill(@RequestBody FilterPostDto filter){
        List<PostListDto> filterPost = postServices.filterPostsBySkill(filter.getSkill());
        return ResponseEntity.ok(filterPost);
    }

    @PostMapping("q-a/filter")
    public ResponseEntity<List<QuestionAnswerDto>> viewFilteredQA(@RequestBody FilterPostDto filter){
        String title = (filter.getTitle() != null) ? filter.getTitle() : "";
        List<QuestionAnswerDto> filterPost = postServices.filterQA(filter.getCategoryId(), filter.getTagId(), title);
        return ResponseEntity.ok(filterPost);
    }

    @PostMapping("/search/multi-tags-categories")
    public ResponseEntity<SearchMultipleResultDto> search(@RequestBody SearchMultipleDto searchMultipleDto){
        SearchMultipleResultDto result = postServices.searchMultiple(searchMultipleDto);
        return ResponseEntity.ok(result);

    }

    @PostMapping("drafts/add")
    public ResponseEntity<PostDto> addDraft(@RequestHeader("Authorization") String headerValue,@RequestBody RequestPostDto requestPostDto){
        Optional<UserEntity> user = userRepository.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", "")));
        UserEntity userEntity = user.get();

        PostDto newDraft = postServices.requestPost(requestPostDto, userEntity);
        postServices.postDetail(newDraft.getPostId(), "Draft");
        return ResponseEntity.ok(newDraft);
    }

    @PostMapping("drafts/send")
    public ResponseEntity<Boolean> sendDraft(@RequestHeader("Authorization") String headerValue,@RequestBody PostDto postDto){
        Optional<UserEntity> user = userRepository.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", "")));
        UserEntity userEntity = user.get();

        if (userEntity.getRole().getRoleName().equalsIgnoreCase("lecturer")){
            postServices.sendDraftLecturer(postDto.getPostId(), userEntity);
        }else {
            postServices.sendDraft(postDto.getPostId());
        }
        return ResponseEntity.ok(true);
    }

    @GetMapping("drafts/view")
    public ResponseEntity<Map<String, List<?>>> viewDraft(@RequestHeader("Authorization") String headerValue){
        Optional<UserEntity> user = userRepository.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", "")));
        UserEntity userEntity = user.get();

        Map<String, List<?>> draftList = postServices.viewDraft(userEntity.getId());
        return ResponseEntity.ok(draftList);
    }

    @PostMapping("drafts/delete")
    public ResponseEntity<Boolean> deleteDraftById(@RequestBody PostDto postId){
        postServices.deletePostById(postId.getPostId());
        return ResponseEntity.ok(true);
    }

    @PostMapping("drafts/edit")
    public ResponseEntity<PostDto> editDraft(@RequestBody EditPostDto editPostDto){
        PostDto draft = postServices.editDraft(editPostDto);
        return ResponseEntity.ok(draft);
    }

    @GetMapping("posts/followed")
    public ResponseEntity<List<PostListDto>> getFollowedPost(@RequestHeader("Authorization") String headerValue){
        Optional<UserEntity> user = userRepository.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", "")));
        UserEntity userEntity = user.get();

        List<PostListDto> followedlist = postServices.viewFollowedPost(userEntity.getId());
        return ResponseEntity.ok(followedlist);
    }

    @GetMapping("q-a/followed")
    public ResponseEntity<List<QuestionAnswerDto>> getFollowedQA(@RequestHeader("Authorization") String headerValue){
        Optional<UserEntity> user = userRepository.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", "")));
        UserEntity userEntity = user.get();

        List<QuestionAnswerDto> followedlist = postServices.viewFollowedQA(userEntity.getId());
        return ResponseEntity.ok(followedlist);
    }

}
