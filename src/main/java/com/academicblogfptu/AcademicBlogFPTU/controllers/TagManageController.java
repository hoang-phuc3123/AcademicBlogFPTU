package com.academicblogfptu.AcademicBlogFPTU.controllers;


import com.academicblogfptu.AcademicBlogFPTU.config.UserAuthProvider;
import com.academicblogfptu.AcademicBlogFPTU.dtos.CategoryAndTagDtos.TagDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos.UserDto;
import com.academicblogfptu.AcademicBlogFPTU.entities.TagEntity;
import com.academicblogfptu.AcademicBlogFPTU.services.TagServices;
import com.academicblogfptu.AcademicBlogFPTU.services.UserServices;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
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
            List<TagEntity> tags = tagService.getAllTags();
            return ResponseEntity.ok(tags);
    }

    @PostMapping("/admin/new-tag")
    public ResponseEntity<TagEntity> createTag(@RequestHeader("Authorization") String headerValue, @RequestBody TagDto tag) {
        if (isAdmin(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            TagEntity newTag = tagService.createTag(tag);
            return ResponseEntity.ok(newTag);
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

    }



    @PostMapping("/admin/edit-tag")

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

    @PostMapping("/admin/delete-tag")
    public ResponseEntity<String> deleteTag(@RequestHeader("Authorization") String headerValue, @RequestBody TagDto deletedTag) {
        if (isAdmin(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            try{
                tagService.deleteTag(deletedTag.getTagId());
            }catch (Exception e){
                if(e.getMessage().equals("could not execute statement; SQL [n/a]; constraint [null]")){
                    return new ResponseEntity<>("This tag has at least 1 usage",HttpStatus.CONFLICT);
                }
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            return ResponseEntity.ok("Success");
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

}

