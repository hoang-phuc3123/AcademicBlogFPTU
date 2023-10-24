package com.academicblogfptu.AcademicBlogFPTU.services;

import com.academicblogfptu.AcademicBlogFPTU.dtos.CategoryDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.TagDto;
import com.academicblogfptu.AcademicBlogFPTU.entities.CategoryEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.MajorEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.TagEntity;
import com.academicblogfptu.AcademicBlogFPTU.exceptions.AppException;
import com.academicblogfptu.AcademicBlogFPTU.repositories.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryServices {
    @Autowired
    private CategoryRepository categoryRepository;

    public List<CategoryDto> buildCategoryTree(){
        List<CategoryEntity> rootCategories = categoryRepository.findByParentIDIsNull();
        List<CategoryDto> categoryDtos = new ArrayList<>();
        for (CategoryEntity rootCategory: rootCategories) {
            CategoryDto categoryDto = buildCategoryDto(rootCategory);
            categoryDtos.add(categoryDto);
        }
        return categoryDtos;

    }

    private CategoryDto buildCategoryDto(CategoryEntity category) {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setId(category.getId());
        categoryDto.setCategoryName(category.getCategoryName());
        categoryDto.setCategoryType(category.getCategoryType());
        categoryDto.setMajorName(category.getMajor().getMajorName());

        List<CategoryEntity> childCategories = categoryRepository.findByParentID(category.getId());
        List<CategoryDto> childDtos = new ArrayList<>();
        for(CategoryEntity childCategory :childCategories){
            CategoryDto childCategoryDto = buildCategoryDto(childCategory);
            childDtos.add(childCategoryDto);
        }
        categoryDto.setChildCategories(childDtos);
        return categoryDto;

    }

    public void deleteCategoryWithChildren(int categoryId) {
        CategoryEntity category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppException("Category not found", HttpStatus.UNAUTHORIZED));

        deleteChildCategories(category);
        categoryRepository.delete(category);
    }

    private void deleteChildCategories(CategoryEntity category) {
        List<CategoryEntity> childCategories = categoryRepository.findByParentID(category.getId());
        for (CategoryEntity childCategory : childCategories) {
            deleteChildCategories(childCategory); // Recursively delete children
            categoryRepository.delete(childCategory);
        }
    }

    public CategoryEntity updateTag(CategoryDto updatedCategory) {
        CategoryEntity existingCategory = categoryRepository.findById(updatedCategory.getId())
                .orElseThrow(() -> new AppException("Unknown category", HttpStatus.UNAUTHORIZED));
        existingCategory.setCategoryName(updatedCategory.getCategoryName());
        return categoryRepository.save(existingCategory);
    }

    public CategoryEntity createOrRetrieveCategoryWithParent(String categoryName, String categoryType, Integer parentID,Integer majorId) {
        // Check if a category with the same name and type exists under the specified parent
        CategoryEntity existingCategory = categoryRepository.findByCategoryNameAndCategoryTypeAndParentID(categoryName, categoryType, parentID);

        if (existingCategory != null) {
            // If the category already exists, return it
            return existingCategory;
        } else {
            // Create a new category with the specified name, type, and parent
            CategoryEntity newCategory = new CategoryEntity();
            newCategory.setCategoryName(categoryName);
            newCategory.setCategoryType(categoryType);
            newCategory.setParentID(parentID);
            MajorEntity major = new MajorEntity();
            major.setId(majorId);
            newCategory.setMajor(major);

            // Save the new category to the database
            return categoryRepository.save(newCategory);
        }
    }
    public CategoryEntity createOrRetrieveCategory(String categoryName, String categoryType,Integer majorId) {
        // Check if a category with the same name and type exists
        CategoryEntity existingCategory = categoryRepository.findByCategoryNameAndCategoryType(categoryName, categoryType);

        if (existingCategory != null) {
            // If the category already exists, return it
            return existingCategory;
        } else {
            // Create a new category with the specified name and type
            CategoryEntity newCategory = new CategoryEntity();
            newCategory.setCategoryName(categoryName);
            newCategory.setCategoryType(categoryType);
            // Set any other properties you need to set for the new category
            MajorEntity major = new MajorEntity();
            major.setId(majorId);
            newCategory.setMajor(major);

            // Save the new category to the database
            return categoryRepository.save(newCategory);
        }
    }




}
