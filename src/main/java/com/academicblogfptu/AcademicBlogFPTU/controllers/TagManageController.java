package com.academicblogfptu.AcademicBlogFPTU.controllers;


import com.academicblogfptu.AcademicBlogFPTU.config.UserAuthProvider;
import com.academicblogfptu.AcademicBlogFPTU.dtos.AdminDtos.ActivitiesLogDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.CategoryAndTagDtos.TagDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos.UserDto;
import com.academicblogfptu.AcademicBlogFPTU.entities.SkillEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.TagEntity;
import com.academicblogfptu.AcademicBlogFPTU.exceptions.AppException;
import com.academicblogfptu.AcademicBlogFPTU.repositories.TagRepository;
import com.academicblogfptu.AcademicBlogFPTU.services.AdminServices;
import com.academicblogfptu.AcademicBlogFPTU.services.TagServices;
import com.academicblogfptu.AcademicBlogFPTU.services.UserServices;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
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

    @Autowired
    private final AdminServices adminService;

    @Autowired
    private final TagRepository tagRepository;

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
            // lưu vào activities_log
            ActivitiesLogDto activitiesLogDto = new ActivitiesLogDto();
            activitiesLogDto.setAction("Thêm mới thẻ: "+tag.getTagName());
            long currentTimeMillis = System.currentTimeMillis();
            Timestamp expirationTimestamp = new Timestamp(currentTimeMillis);
            activitiesLogDto.setActionTime(expirationTimestamp);
            String username = userAuthProvider.getUser(headerValue.replace("Bearer ", ""));
            UserDto adminDto = userService.findByUsername(username);
            activitiesLogDto.setUserID(adminDto.getId());
            adminService.saveActivity(activitiesLogDto);

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
            TagEntity oldTag = tagRepository.findById(updatedTag.getTagId())
                    .orElseThrow(()->new AppException("Unknown tag", HttpStatus.NOT_FOUND));
            // lưu vào activities_log
            ActivitiesLogDto activitiesLogDto = new ActivitiesLogDto();
            activitiesLogDto.setAction("Chỉnh sửa thẻ "+oldTag.getTagName() +" thành "+updatedTag.getTagName());
            long currentTimeMillis = System.currentTimeMillis();
            Timestamp expirationTimestamp = new Timestamp(currentTimeMillis);
            activitiesLogDto.setActionTime(expirationTimestamp);
            String username = userAuthProvider.getUser(headerValue.replace("Bearer ", ""));
            UserDto adminDto = userService.findByUsername(username);
            activitiesLogDto.setUserID(adminDto.getId());
            adminService.saveActivity(activitiesLogDto);

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
                TagEntity deleteTag = tagRepository.findById(deletedTag.getTagId())
                        .orElseThrow(()->new AppException("Unknown tag", HttpStatus.NOT_FOUND));
                tagService.deleteTag(deletedTag.getTagId());
                // lưu vào activities_log
                ActivitiesLogDto activitiesLogDto = new ActivitiesLogDto();
                activitiesLogDto.setAction("Xóa thẻ: "+deleteTag.getTagName());
                long currentTimeMillis = System.currentTimeMillis();
                Timestamp expirationTimestamp = new Timestamp(currentTimeMillis);
                activitiesLogDto.setActionTime(expirationTimestamp);
                String username = userAuthProvider.getUser(headerValue.replace("Bearer ", ""));
                UserDto adminDto = userService.findByUsername(username);
                activitiesLogDto.setUserID(adminDto.getId());
                adminService.saveActivity(activitiesLogDto);
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

