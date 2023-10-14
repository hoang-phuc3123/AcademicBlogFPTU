package com.academicblogfptu.AcademicBlogFPTU.controllers;

import com.academicblogfptu.AcademicBlogFPTU.dtos.TagDto;
import com.academicblogfptu.AcademicBlogFPTU.entities.TagEntity;
import com.academicblogfptu.AcademicBlogFPTU.services.TagServices;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class TagManageController {

    @Autowired
    private TagServices tagService;



    @GetMapping("/tags")
    public ResponseEntity<List<TagEntity>> getAllTags() {
        List<TagEntity> tags = tagService.getAllTags();
        return ResponseEntity.ok(tags);
    }

    @PostMapping("/new-tag")
    public ResponseEntity<TagEntity> createTag(@RequestBody TagDto tag) {
        TagEntity newTag = tagService.createTag(tag);
        return ResponseEntity.ok(newTag);
    }



    @PostMapping("/edit-tag")
    public ResponseEntity<TagEntity> updateTag(@RequestBody TagDto updatedTag) {
        TagEntity _updatedTag = new TagEntity();
        _updatedTag = tagService.updateTag(updatedTag);
        return ResponseEntity.ok(_updatedTag);
    }

    @PostMapping("/delete-tag")
    public ResponseEntity<Boolean> deleteTag(@RequestBody TagDto deletedTag) {
        tagService.deleteTag(deletedTag.getTagId());
        return ResponseEntity.ok(true);
    }

}
