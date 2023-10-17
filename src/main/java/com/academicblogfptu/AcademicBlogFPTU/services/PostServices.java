package com.academicblogfptu.AcademicBlogFPTU.services;

import com.academicblogfptu.AcademicBlogFPTU.dtos.PostDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.PostListDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.RequestPostDto;
import com.academicblogfptu.AcademicBlogFPTU.entities.*;
import com.academicblogfptu.AcademicBlogFPTU.exceptions.AppException;
import com.academicblogfptu.AcademicBlogFPTU.repositories.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.*;


@RequiredArgsConstructor
@Service
public class PostServices {

    @Autowired
    private final PostRepository postRepository;

    @Autowired
    private final TagRepository tagRepository;

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final PostDetailsRepository postDetailsRepository;

    @Autowired
    private final CategoryRepository categoryRepository;


    public List<PostListDto> viewAllPost() {
        List<PostEntity> list = postRepository.findAll();
        List<PostListDto> postList = new ArrayList<>();
        for (PostEntity post : list) {

            if (isApprove(post.getId())) {
                UserEntity user = userRepository.findById(post.getUser().getId())
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.UNAUTHORIZED));
                TagEntity tag = tagRepository.findById(post.getTag().getId())
                        .orElseThrow(() -> new AppException("Unknown tag", HttpStatus.UNAUTHORIZED));

                int numOfUpvote = (post.getNumOfUpvote() != null) ? post.getNumOfUpvote() : 0;
                int numOfDownvote = (post.getNumOfDownvote() != null) ? post.getNumOfDownvote() : 0;

                PostListDto postListDto = new PostListDto(post.getId(), post.getTitle(),
                        post.getDateOfPost().toString(), numOfUpvote, numOfDownvote, user.getUsername(), getRelatedCategories(post.getCategory().getId()), tag.getTagName());
                postList.add(postListDto);
            }
        }
        return postList;
    }

    public PostDto viewPostById(int postId) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException("Post with ID " + postId + " not found", HttpStatus.NOT_FOUND));

        if (!isApprove(post.getId())) {
            throw new AppException("Post with ID " + postId + " is not approved", HttpStatus.UNAUTHORIZED);
        }

        UserEntity user = userRepository.findById(post.getUser().getId())
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.UNAUTHORIZED));
        TagEntity tag = tagRepository.findById(post.getTag().getId())
                .orElseThrow(() -> new AppException("Unknown tag", HttpStatus.UNAUTHORIZED));

        int numOfUpvote = (post.getNumOfUpvote() != null) ? post.getNumOfUpvote() : 0;
        int numOfDownvote = (post.getNumOfDownvote() != null) ? post.getNumOfDownvote() : 0;

        return new PostDto(post.getId(), post.getTitle(), post.getContent(),
                post.getDateOfPost().toString(), numOfUpvote, numOfDownvote, post.isRewarded(), post.isEdited(), post.isAllowComment(), user.getUsername(), getRelatedCategories(post.getCategory().getId()), tag.getTagName());
    }

    public boolean isApprove(int id) {
        PostDetailsEntity post = postDetailsRepository.findByPostId(id)
                .orElseThrow(() -> new AppException("Unknown post", HttpStatus.UNAUTHORIZED));
        if (post.getType().equalsIgnoreCase("Approve")) {
            return true;
        }
        return false;
    }

    public List<CategoryEntity> getRelatedCategories(Integer categoryId) {
        List relatedCategories = new ArrayList<>();

        if (categoryId == null) {
            // Handle the case where categoryId is null (or perform appropriate error handling)
            return relatedCategories;
        }

        // Find the initial category by its ID
        CategoryEntity initialCategory = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category with ID " + categoryId + " not found"));

        // Add the initial category to the list
        relatedCategories.add(initialCategory.getCategoryName());
        if (initialCategory.getParentID() != null) {
            findParentCategory(initialCategory, relatedCategories);

            findChildCategories(initialCategory, relatedCategories);
        }

        relatedCategories.sort(Comparator.comparing(String::length).reversed());
        return relatedCategories;
    }

    private void findParentCategory(CategoryEntity category, List<String> relatedCategories) {
        // Find and add the parent category (if it exists)
        Integer parentID = category.getParentID();
        if (parentID != null) {
            CategoryEntity parentCategory = categoryRepository.findById(parentID).orElse(null);
            if (parentCategory != null && !relatedCategories.contains(parentCategory.getCategoryName())) {
                relatedCategories.add(parentCategory.getCategoryName());
                // Stop when the parent is the root category (has no parent)
                if (parentCategory.getParentID() != null) {
                    findParentCategory(parentCategory, relatedCategories);
                }
            }
        }
    }

    private void findChildCategories(CategoryEntity category, List<String> relatedCategories) {
        // Find and add child categories
        List<CategoryEntity> childCategories = categoryRepository.findByParentID(category.getId());
        for (CategoryEntity child : childCategories) {
            if (child != null && !relatedCategories.contains(child.getCategoryName())) {
                relatedCategories.add(child.getCategoryName());
                findChildCategories(child, relatedCategories);
            }
        }
    }

    public void deletePostById(int id){
        PostDetailsEntity postDetails = postDetailsRepository.findByPostId(id)
                .orElseThrow(() -> new AppException("Unknown post", HttpStatus.UNAUTHORIZED));
        postDetails.setType("Delete");
        postDetails.setDateOfAction(Date.valueOf(java.time.LocalDate.now()));
        postDetailsRepository.save(postDetails);
    }

    public PostDto requestPost(RequestPostDto requestPostDto){
        PostEntity newPostEntity = new PostEntity();

        newPostEntity.setTitle(requestPostDto.getTitle());
        newPostEntity.setContent(requestPostDto.getContent());
        newPostEntity.setDateOfPost(Date.valueOf(java.time.LocalDate.now()));
        newPostEntity.setNumOfUpvote(0);
        newPostEntity.setNumOfDownvote(0);
        newPostEntity.setRewarded(false);
        newPostEntity.setEdited(false);
        newPostEntity.setAllowComment(requestPostDto.isAllowComment());
        newPostEntity.setLength(countWords(requestPostDto.getContent()));
        UserEntity user = userRepository.findById(requestPostDto.getAccountId())
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.UNAUTHORIZED));
        newPostEntity.setUser(user);
        CategoryEntity category = categoryRepository.findById(requestPostDto.getCategoryId())
                .orElseThrow(() -> new AppException("Unknown category", HttpStatus.UNAUTHORIZED));
        newPostEntity.setCategory(category);
        TagEntity tag = tagRepository.findById(requestPostDto.getTagId())
                .orElseThrow(() -> new AppException("Unknown tag", HttpStatus.UNAUTHORIZED));
        newPostEntity.setTag(tag);

        postRepository.save(newPostEntity);

        return new PostDto(newPostEntity.getId(), newPostEntity.getTitle(), newPostEntity.getContent(), newPostEntity.getDateOfPost().toString()
        , newPostEntity.getNumOfUpvote(), newPostEntity.getNumOfDownvote(), newPostEntity.isRewarded(), newPostEntity.isEdited()
        , newPostEntity.isAllowComment(), newPostEntity.getUser().getUsername() ,getRelatedCategories(newPostEntity.getCategory().getId()), newPostEntity.getTag().getTagName());
    }
    public int countWords(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        String[] words = text.split("\\s+");
        return words.length;
    }

    public void postDetail(int postId){
        Optional<PostEntity> post = postRepository.findById(postId);
        PostEntity postEntity = post.get();

        PostDetailsEntity newPostDetails = new PostDetailsEntity();
        newPostDetails.setDateOfAction(postEntity.getDateOfPost());
        newPostDetails.setType("Request");
        newPostDetails.setPost(postEntity);

        postDetailsRepository.save(newPostDetails);
    }
}
