package com.academicblogfptu.AcademicBlogFPTU.controllers;


import com.academicblogfptu.AcademicBlogFPTU.config.UserAuthProvider;
import com.academicblogfptu.AcademicBlogFPTU.dtos.TagDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDto;
import com.academicblogfptu.AcademicBlogFPTU.entities.TagEntity;
import com.academicblogfptu.AcademicBlogFPTU.repositories.UserDetailsRepository;
import com.academicblogfptu.AcademicBlogFPTU.services.TagServices;
import com.academicblogfptu.AcademicBlogFPTU.services.UserServices;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class TagManageController {

    @Autowired
    private final TagServices tagService;

    @Autowired
    private final UserServices userService;

    @Autowired
    private final UserAuthProvider userAuthProvider;

    public boolean isAdmin(UserDto userDto) {
        return userDto.getRoleName().equals("admin");
    }

    @GetMapping("/tags")
    public ResponseEntity<List<TagEntity>> getAllTags(@RequestHeader("Authorization") String headerValue) {
        if (isAdmin(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            List<TagEntity> tags = tagService.getAllTags();
            return ResponseEntity.ok(tags);
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/new-tag")
    public ResponseEntity<TagEntity> createTag(@RequestHeader("Authorization") String headerValue, @RequestBody TagDto tag) {
        if (isAdmin(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            TagEntity newTag = tagService.createTag(tag);
            return ResponseEntity.ok(newTag);
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

    }



    @PostMapping("/edit-tag")

    public ResponseEntity<TagEntity> updateTag(@RequestHeader("Authorization") String headerValue, @RequestBody TagDto updatedTag) {
        if (isAdmin(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            TagEntity _updatedTag = new TagEntity();
            _updatedTag = tagService.updateTag(updatedTag);
            return ResponseEntity.ok(_updatedTag);
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/delete-tag")
    public ResponseEntity<Boolean> deleteTag(@RequestHeader("Authorization") String headerValue, @RequestBody TagDto deletedTag) {
        if (isAdmin(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            tagService.deleteTag(deletedTag.getTagId());
            return ResponseEntity.ok(true);
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

}

