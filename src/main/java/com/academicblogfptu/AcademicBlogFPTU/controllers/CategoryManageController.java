package com.academicblogfptu.AcademicBlogFPTU.controllers;


import com.academicblogfptu.AcademicBlogFPTU.config.UserAuthProvider;
import com.academicblogfptu.AcademicBlogFPTU.dtos.CategoryDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.CategoryRequestDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.TagDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDto;
import com.academicblogfptu.AcademicBlogFPTU.entities.CategoryEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.TagEntity;
import com.academicblogfptu.AcademicBlogFPTU.services.CategoryServices;
import com.academicblogfptu.AcademicBlogFPTU.services.UserServices;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CategoryManageController {
    @Autowired
    private final CategoryServices categoryServices;
    @Autowired
    private final UserServices userService;
    @Autowired
    private final UserAuthProvider userAuthProvider;

    public boolean isAdmin(UserDto userDto) {
        return userDto.getRoleName().equals("admin");
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDto>> getAllTags() {

            List<CategoryDto> categories = categoryServices.buildCategoryTree();
            return ResponseEntity.ok(categories);
        }


    @PostMapping("/admin/edit-category")
    public ResponseEntity<CategoryEntity> updateTag(@RequestHeader("Authorization") String headerValue, @RequestBody CategoryDto updatedCategory) {
        if (isAdmin(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            CategoryEntity _updatedCategory = new CategoryEntity();
            _updatedCategory = categoryServices.updateTag(updatedCategory);
            return ResponseEntity.ok(_updatedCategory);
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/admin/new-category")
    public ResponseEntity<String> createCategory(@RequestHeader("Authorization") String headerValue, @RequestBody CategoryRequestDto categoryRequestDto){
        if (isAdmin(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            try {
                CategoryEntity specializationCategory = categoryServices.createOrRetrieveCategory(categoryRequestDto.getSpecialization(), "Specialization",categoryRequestDto.getMajorId());

                if (categoryRequestDto.getSemester() == null) {
                    // The user only wants to create a new Specialization category
                    return ResponseEntity.ok("Create successfully");
                }

                CategoryEntity semesterCategory = categoryServices.createOrRetrieveCategoryWithParent(categoryRequestDto.getSemester(), "Semester",specializationCategory.getId(), categoryRequestDto.getMajorId());

                if (categoryRequestDto.getSubject() == null) {
                    // The user only wants to create a new Semester category
                    return ResponseEntity.ok("Create successfully");
                }

                CategoryEntity subjectCategory = categoryServices.createOrRetrieveCategoryWithParent(categoryRequestDto.getSubject(), "Subject", semesterCategory.getId(),categoryRequestDto.getMajorId());

                return ResponseEntity.ok("Create successfully");
            } catch (Exception e) {
                return new ResponseEntity<>("Failed to create categories: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }


    @PostMapping("/admin/delete-category")
    public ResponseEntity<String> deleteCategory(@RequestHeader("Authorization") String headerValue, @RequestBody CategoryDto categoryDto) {
        if (isAdmin(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            try {
                categoryServices.deleteCategoryWithChildren(categoryDto.getId());
                return new ResponseEntity<>("Category and its children deleted successfully", HttpStatus.OK);
            } catch (Exception e) {
                if(e.getMessage().equals("could not execute statement; SQL [n/a]; constraint [null]")){
                    return new ResponseEntity<>("This category has at least 1 usage",HttpStatus.CONFLICT);
                }
                return new ResponseEntity<>("Failed to delete category: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }






}
