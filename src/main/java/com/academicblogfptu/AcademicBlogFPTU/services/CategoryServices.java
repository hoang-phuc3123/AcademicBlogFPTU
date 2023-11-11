package com.academicblogfptu.AcademicBlogFPTU.services;

import com.academicblogfptu.AcademicBlogFPTU.dtos.CategoryAndTagDtos.CategoryDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.CategoryAndTagDtos.CategoryListDto;
import com.academicblogfptu.AcademicBlogFPTU.entities.CategoryEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.MajorEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.TagEntity;
import com.academicblogfptu.AcademicBlogFPTU.exceptions.AppException;
import com.academicblogfptu.AcademicBlogFPTU.repositories.CategoryRepository;
import com.academicblogfptu.AcademicBlogFPTU.repositories.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryServices {
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private TagRepository tagRepository;

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

    public List<String> getSubjectCategoriesAndTag(){
        List<CategoryEntity> categoryEntities = categoryRepository.findByCategoryType("Subject");
        List<TagEntity> tagEntities = tagRepository.findAll();
        List<String> categoryAndTag = new ArrayList<>();
        for (CategoryEntity category: categoryEntities) {
            categoryAndTag.add(category.getCategoryName());
        }
        for(TagEntity tag :tagEntities){
            categoryAndTag.add(tag.getTagName());
        }
        return categoryAndTag;
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

    public CategoryEntity updateCategory(CategoryDto updatedCategory) {
        CategoryEntity existingCategory = categoryRepository.findById(updatedCategory.getId())
                .orElseThrow(() -> new AppException("Unknown category", HttpStatus.NOT_FOUND));
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

    public List<CategoryListDto> trendingCategories(){
        List<CategoryEntity> getTrendingCategories = categoryRepository.findTrendingCategoryForLast7Days();
        List<CategoryListDto> trendingCategories = new ArrayList<>();

        for (CategoryEntity category : getTrendingCategories) {
            CategoryListDto categoryListDto = new CategoryListDto(category.getId(), category.getCategoryName(), category.getCategoryType());
            trendingCategories.add(categoryListDto);
        }
        return trendingCategories;
    }
}
