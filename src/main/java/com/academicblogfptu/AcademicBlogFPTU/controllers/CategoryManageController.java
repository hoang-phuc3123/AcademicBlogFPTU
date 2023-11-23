package com.academicblogfptu.AcademicBlogFPTU.controllers;


import com.academicblogfptu.AcademicBlogFPTU.config.UserAuthProvider;
import com.academicblogfptu.AcademicBlogFPTU.dtos.AdminDtos.ActivitiesLogDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.CategoryAndTagDtos.CategoryDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.CategoryAndTagDtos.CategoryRequestDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.CategoryAndTagDtos.CategoryListDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos.UserDto;
import com.academicblogfptu.AcademicBlogFPTU.entities.CategoryEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.SkillEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.TagEntity;
import com.academicblogfptu.AcademicBlogFPTU.exceptions.AppException;
import com.academicblogfptu.AcademicBlogFPTU.repositories.CategoryRepository;
import com.academicblogfptu.AcademicBlogFPTU.services.AdminServices;
import com.academicblogfptu.AcademicBlogFPTU.services.CategoryServices;
import com.academicblogfptu.AcademicBlogFPTU.services.PostServices;
import com.academicblogfptu.AcademicBlogFPTU.services.UserServices;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
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
    @Autowired
    private final AdminServices adminService;
    @Autowired
    private final CategoryRepository categoryRepository;
    @Autowired
    private final PostServices postService;

    public boolean isAdmin(UserDto userDto) {
        return userDto.getRoleName().equals("admin");
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDto>> getAllCategories() {

            List<CategoryDto> categories = categoryServices.buildCategoryTree();
            return ResponseEntity.ok(categories);
    }

    @GetMapping("/subject-categories-and-tags")
    public ResponseEntity<List<String>> getSubjectCategory() {
        List<String> categoriesAndTags = categoryServices.getSubjectCategoriesAndTag();
        return ResponseEntity.ok(categoriesAndTags);
    }


    @PostMapping("/admin/edit-category")
    public ResponseEntity<CategoryEntity> updateTag(@RequestHeader("Authorization") String headerValue, @RequestBody CategoryDto updatedCategory) {
        if (isAdmin(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            CategoryEntity _updatedCategory = new CategoryEntity();

            CategoryEntity oldCategory = categoryRepository.findById(updatedCategory.getId())
                    .orElseThrow(()->new AppException("Unknown category", HttpStatus.NOT_FOUND));
            // lưu vào activities_log
            ActivitiesLogDto activitiesLogDto = new ActivitiesLogDto();
            activitiesLogDto.setAction("Chỉnh sửa danh mục "+oldCategory.getCategoryName() +" thành "+updatedCategory.getCategoryName());
            long currentTimeMillis = System.currentTimeMillis();
            Timestamp expirationTimestamp = new Timestamp(currentTimeMillis);
            activitiesLogDto.setActionTime(expirationTimestamp);
            String username = userAuthProvider.getUser(headerValue.replace("Bearer ", ""));
            UserDto adminDto = userService.findByUsername(username);
            activitiesLogDto.setUserID(adminDto.getId());
            adminService.saveActivity(activitiesLogDto);

            _updatedCategory = categoryServices.updateCategory(updatedCategory);
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

                List<CategoryEntity> categoryEntityList = postService.getRelatedCategories(subjectCategory.getId());
                categoryEntityList.sort(Comparator.comparing(CategoryEntity::getId));

                List<String> newCategories = new ArrayList<>();
                for (CategoryEntity cate: categoryEntityList) {
                    newCategories.add(cate.getCategoryName());
                }

                String categories = String.join(", ", newCategories);

                ActivitiesLogDto activitiesLogDto = new ActivitiesLogDto();
                activitiesLogDto.setAction("Thêm mới danh mục: "+categories);
                long currentTimeMillis = System.currentTimeMillis();
                Timestamp expirationTimestamp = new Timestamp(currentTimeMillis);
                activitiesLogDto.setActionTime(expirationTimestamp);
                String username = userAuthProvider.getUser(headerValue.replace("Bearer ", ""));
                UserDto adminDto = userService.findByUsername(username);
                activitiesLogDto.setUserID(adminDto.getId());
                adminService.saveActivity(activitiesLogDto);

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
                CategoryEntity deleteCategory = categoryRepository.findById(categoryDto.getId())
                        .orElseThrow(()->new AppException("Unknown category", HttpStatus.NOT_FOUND));
                categoryServices.deleteCategoryWithChildren(categoryDto.getId());
                // lưu vào activities_log
                ActivitiesLogDto activitiesLogDto = new ActivitiesLogDto();
                activitiesLogDto.setAction("Xóa danh mục: "+deleteCategory.getCategoryName());
                long currentTimeMillis = System.currentTimeMillis();
                Timestamp expirationTimestamp = new Timestamp(currentTimeMillis);
                activitiesLogDto.setActionTime(expirationTimestamp);
                String username = userAuthProvider.getUser(headerValue.replace("Bearer ", ""));
                UserDto adminDto = userService.findByUsername(username);
                activitiesLogDto.setUserID(adminDto.getId());
                adminService.saveActivity(activitiesLogDto);
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

    @GetMapping("/categories/trending")
    public ResponseEntity<List<CategoryListDto>> getTrendingCategories() {
        List<CategoryListDto> categories = categoryServices.trendingCategories();
        return ResponseEntity.ok(categories);
    }
}
