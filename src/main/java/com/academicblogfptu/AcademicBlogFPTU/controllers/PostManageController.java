package com.academicblogfptu.AcademicBlogFPTU.controllers;

import com.academicblogfptu.AcademicBlogFPTU.config.UserAuthProvider;
import com.academicblogfptu.AcademicBlogFPTU.dtos.PostDtos.DeclineDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.PostDtos.PostDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.PostDtos.PostListDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos.UserDto;
import com.academicblogfptu.AcademicBlogFPTU.entities.PostEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.UserEntity;
import com.academicblogfptu.AcademicBlogFPTU.repositories.PostRepository;
import com.academicblogfptu.AcademicBlogFPTU.repositories.UserRepository;
import com.academicblogfptu.AcademicBlogFPTU.services.PostServices;
import com.academicblogfptu.AcademicBlogFPTU.services.UserServices;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lecturer")
public class PostManageController {
    @Autowired
    private final PostServices postServices;

    @Autowired
    private final UserServices userServices;

    @Autowired
    private final UserAuthProvider userAuthProvider;

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final PostRepository postRepository;

    public boolean isLecturer(UserDto userDto) {
        return userDto.getRoleName().equals("lecturer");
    }

    @GetMapping("/posts/pending")
    public ResponseEntity<Map<String, List<PostListDto>>> viewPendingPost(@RequestHeader("Authorization") String headerValue) {
        if (isLecturer(userServices.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            Optional<UserEntity> user = userRepository.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", "")));
            UserEntity userEntity = user.get();
            Map<String, List<PostListDto>> pendingPost = postServices.viewPendingPost(userEntity);
            return ResponseEntity.ok(pendingPost);
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/posts/approved")
    public ResponseEntity<List<PostListDto>> viewApprovedPost(@RequestHeader("Authorization") String headerValue) {
        if (isLecturer(userServices.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            Optional<UserEntity> user = userRepository.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", "")));
            UserEntity userEntity = user.get();
            List<PostListDto> approvePostList = postServices.viewApprovedPost(userEntity);
            return ResponseEntity.ok(approvePostList);
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/posts/approve")
    public ResponseEntity<Boolean> approvePost(@RequestHeader("Authorization") String headerValue, @RequestBody PostDto postId) {
        if (isLecturer(userServices.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            Optional<UserEntity> user = userRepository.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", "")));
            UserEntity userEntity = user.get();

            postServices.approvePost(postId.getPostId(), userEntity);
            return ResponseEntity.ok(true);
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/posts/decline")
    public ResponseEntity<Boolean> declinePost(@RequestHeader("Authorization") String headerValue, @RequestBody DeclineDto declineDto) {
        if (isLecturer(userServices.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            Optional<UserEntity> user = userRepository.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", "")));
            UserEntity userEntity = user.get();
            postServices.declinePost(declineDto.getPostId(), declineDto.getReasonOfDecline(), userEntity);
            return ResponseEntity.ok(true);
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/reward/add")
    public ResponseEntity<Boolean> giveReward(@RequestHeader("Authorization") String headerValue, @RequestBody PostDto postId) {
        if (isLecturer(userServices.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            Optional<PostEntity> postEntity = postRepository.findById(postId.getPostId());
            PostEntity post = postEntity.get();
            Optional<UserEntity> user = userRepository.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", "")));
            UserEntity userEntity = user.get();

            postServices.giveReward(post.getId(), userEntity);

            return ResponseEntity.ok(true);
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/reward/remove")
    public ResponseEntity<Boolean> removeReward(@RequestHeader("Authorization") String headerValue, @RequestBody PostDto postId) {
        if (isLecturer(userServices.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            Optional<PostEntity> postEntity = postRepository.findById(postId.getPostId());
            PostEntity post = postEntity.get();
            postServices.removeReward(post.getId());

            return ResponseEntity.ok(true);
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/reward/pending-reward-post")
    public ResponseEntity<List<PostListDto>> viewRewardedPosts(@RequestHeader("Authorization") String headerValue) {
        if (isLecturer(userServices.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            Optional<UserEntity> user = userRepository.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", "")));
            UserEntity userEntity = user.get();
            List<PostListDto> rewardedPost = postServices.getPendingRewardPost(userEntity);
            return ResponseEntity.ok(rewardedPost);
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/reward/dismiss")
    public ResponseEntity<Boolean> dismissReward(@RequestHeader("Authorization") String headerValue, @RequestBody PostDto postId) {
        if (isLecturer(userServices.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            Optional<PostEntity> postEntity = postRepository.findById(postId.getPostId());
            PostEntity post = postEntity.get();
            Optional<UserEntity> user = userRepository.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", "")));
            UserEntity userEntity = user.get();

            postServices.dismissReward(post.getId(), userEntity);

            return ResponseEntity.ok(true);
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
