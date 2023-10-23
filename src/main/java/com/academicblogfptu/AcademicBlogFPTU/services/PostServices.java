package com.academicblogfptu.AcademicBlogFPTU.services;

import com.academicblogfptu.AcademicBlogFPTU.dtos.*;
import com.academicblogfptu.AcademicBlogFPTU.entities.*;
import com.academicblogfptu.AcademicBlogFPTU.exceptions.AppException;
import com.academicblogfptu.AcademicBlogFPTU.repositories.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.support.NullValue;
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

    @Autowired
    private final ImageRepository imageRepository;

    @Autowired
    private final VideoRepository videoRepository;

    @Autowired
    private final UserDetailsRepository userDetailsRepository;

    public List<PostListDto> viewAllPost() {
        List<PostEntity> list = postRepository.findAll();
        List<PostListDto> postList = new ArrayList<>();
        for (PostEntity post : list) {

            if (isApprove(post.getId())) {
                UserEntity user = userRepository.findById(post.getUser().getId())
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.UNAUTHORIZED));
                TagEntity tag = tagRepository.findById(post.getTag().getId())
                        .orElseThrow(() -> new AppException("Unknown tag", HttpStatus.UNAUTHORIZED));

                if (!tag.getTagName().equalsIgnoreCase("Q&A")){
                    PostListDto postListDto = new PostListDto(post.getId(),user.getUsername(),post.getTitle(), post.getDescription(),
                            post.getDateOfPost().toString(), getRelatedCategories(post.getCategory().getId()), tag.getTagName(), post.getCoverURL() ,post.isRewarded());
                    postList.add(postListDto);
                }
            }
        }
        return postList;
    }

    public PostDto viewPostById(int postId) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException("Post with ID " + postId + " not found", HttpStatus.NOT_FOUND));

        UserEntity user = userRepository.findById(post.getUser().getId())
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.UNAUTHORIZED));
        TagEntity tag = tagRepository.findById(post.getTag().getId())
                .orElseThrow(() -> new AppException("Unknown tag", HttpStatus.UNAUTHORIZED));

        int numOfUpvote = (post.getNumOfUpvote() != null) ? post.getNumOfUpvote() : 0;
        int numOfDownvote = (post.getNumOfDownvote() != null) ? post.getNumOfDownvote() : 0;

        return new PostDto(post.getId(), user.getUsername(), post.getTitle(), post.getDescription(),post.getContent(),
                post.getDateOfPost().toString(), numOfUpvote, numOfDownvote, post.isRewarded(), post.isEdited(), post.isAllowComment(),
                getRelatedCategories(post.getCategory().getId()), tag.getTagName(), post.getCoverURL() ,getImageURL(post.getId()), getVideoURL(post.getId()), post.getSlug());
    }

    public boolean isApprove(int id) {
        PostDetailsEntity post = postDetailsRepository.findByPostId(id)
                .orElseThrow(() -> new AppException("Unknown post", HttpStatus.UNAUTHORIZED));
        if (post.getType().equalsIgnoreCase("Approve")) {
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public List<CategoryEntity> getRelatedCategories(Integer categoryId) {
        List relatedCategories = new ArrayList<>();

        if (categoryId == null) {
            // Handle the case where categoryId is null (or perform appropriate error handling)
            return relatedCategories;
        }

        // Find the initial category by its id
        CategoryEntity initialCategory = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category with name " + categoryId + " not found"));

        // Add the initial category to the list
        relatedCategories.add(initialCategory.getCategoryName());
        if (initialCategory.getParentID() != null) {
            findParentCategory(initialCategory, relatedCategories);
            if (!initialCategory.getCategoryType().equals("Semester") ){
                findChildCategories(initialCategory, relatedCategories);
            }
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
        postDetails.setUser(null);
        postDetailsRepository.save(postDetails);
    }

    public PostDto requestPost(RequestPostDto requestPostDto){
        PostEntity newPostEntity = new PostEntity();

        newPostEntity.setTitle(requestPostDto.getTitle());
        if (!requestPostDto.getDescription().isEmpty()){
            newPostEntity.setDescription(requestPostDto.getDescription());
        }else {
            newPostEntity.setDescription(null);
        }
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
        if (!requestPostDto.getCoverURL().isEmpty()){
            newPostEntity.setCoverURL(requestPostDto.getCoverURL());
        }else {
            newPostEntity.setCoverURL(null);
        }

        newPostEntity.setSlug(requestPostDto.getSlug());

        postRepository.save(newPostEntity);

        if (!requestPostDto.getImageURL().isEmpty()){
            imageSave(newPostEntity.getId(), requestPostDto.getImageURL());
        }
        if (!requestPostDto.getVideoURL().isEmpty()){
            videoSave(newPostEntity.getId(), requestPostDto.getVideoURL());
        }

        return new PostDto(newPostEntity.getId(),newPostEntity.getUser().getUsername() ,newPostEntity.getTitle(), newPostEntity.getDescription(), newPostEntity.getContent(), newPostEntity.getDateOfPost().toString()
        , newPostEntity.getNumOfUpvote(), newPostEntity.getNumOfDownvote(), newPostEntity.isRewarded(), newPostEntity.isEdited()
        , newPostEntity.isAllowComment() ,getRelatedCategories(newPostEntity.getCategory().getId()), newPostEntity.getTag().getTagName(),
                newPostEntity.getCoverURL(),getImageURL(newPostEntity.getId()), getVideoURL(newPostEntity.getId()), newPostEntity.getSlug());
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

    public List<String> getImageURL(int postId) {
        List<String> imgURLs = new ArrayList<>();

        // Get image URLs
        List<ImageEntity> images = imageRepository.findByPostId(postId);
        for (ImageEntity image : images) {
            imgURLs.add(image.getImageURL());
        }
        return imgURLs;
    }

    public List<String> getVideoURL(int postId) {
        List<String> videoURLs = new ArrayList<>();

        // Get image URLs
        List<VideoEntity> videos = videoRepository.findByPostId(postId);
        for (VideoEntity video : videos) {
            videoURLs.add(video.getVideoURL());
        }
        return videoURLs;
    }

    public void imageSave(int postId, List<String> imageURL){
        Optional<PostEntity> post = postRepository.findById(postId);
        PostEntity postEntity = post.get();
        for (String imgURL : imageURL) {
            ImageEntity image = new ImageEntity();
            image.setImageURL(imgURL);
            image.setPost(postEntity);
            imageRepository.save(image);
        }
    }

    public void videoSave(int postId, List<String> videoURL){
        Optional<PostEntity> post = postRepository.findById(postId);
        PostEntity postEntity = post.get();
        for (String videoUrl : videoURL) {
            VideoEntity video = new VideoEntity();
            video.setVideoURL(videoUrl);
            video.setPost(postEntity);
            videoRepository.save(video);
        }
    }

    public PostDto editPost(EditPostDto editPostDto){
        PostEntity post = postRepository.findById(editPostDto.getPostId())
                .orElseThrow(() -> new AppException("Unknown post", HttpStatus.UNAUTHORIZED));

        post.setTitle(editPostDto.getTitle());
        if (!editPostDto.getDescription().isEmpty()){
            post.setDescription(editPostDto.getDescription());
        }else {
            post.setDescription(null);
        }
        post.setContent(editPostDto.getContent());
        post.setEdited(true);
        post.setLength(countWords(editPostDto.getContent()));
        post.setAllowComment(editPostDto.isAllowComment());
        CategoryEntity category = categoryRepository.findById(editPostDto.getCategoryId())
                .orElseThrow(() -> new AppException("Unknown category", HttpStatus.UNAUTHORIZED));
        post.setCategory(category);
        TagEntity tag = tagRepository.findById(editPostDto.getTagId())
                .orElseThrow(() -> new AppException("Unknown tag", HttpStatus.UNAUTHORIZED));
        post.setTag(tag);

        if (!editPostDto.getCoverURL().isEmpty()){
            post.setCoverURL(editPostDto.getCoverURL());
        }else {
            post.setCoverURL(null);
        }

        post.setSlug(editPostDto.getSlug());

            List<ImageEntity> images = imageRepository.findByPostId(post.getId());
            if (!images.isEmpty()){
                for (ImageEntity image : images) {
                    imageRepository.delete(image);
                }
            }

            List<VideoEntity> videos = videoRepository.findByPostId(post.getId());
            if (!videos.isEmpty()){
                for (VideoEntity video : videos) {
                    videoRepository.delete(video);
                }
            }

        postRepository.save(post);

        PostDetailsEntity postDetails = postDetailsRepository.findByPostId(post.getId())
                .orElseThrow(() -> new AppException("Unknown post", HttpStatus.UNAUTHORIZED));

        postDetails.setType("Request");
        postDetails.setUser(null);
        postDetails.setDateOfAction(Date.valueOf(java.time.LocalDate.now()));
        postDetailsRepository.save(postDetails);

        if (!editPostDto.getImageURL().isEmpty()){
            imageSave(post.getId(), editPostDto.getImageURL());
        }

        if (!editPostDto.getVideoURL().isEmpty()){
            videoSave(post.getId(), editPostDto.getVideoURL());
        }

        return new PostDto(post.getId(),post.getUser().getUsername() ,post.getTitle(), post.getDescription(),post.getContent(), post.getDateOfPost().toString()
                , post.getNumOfUpvote(), post.getNumOfDownvote(), post.isRewarded(), post.isEdited()
                , post.isAllowComment() ,getRelatedCategories(post.getCategory().getId()), post.getTag().getTagName(), post.getCoverURL(),getImageURL(post.getId()), getVideoURL(post.getId()), post.getSlug());
    }

    public List<PostListDto> viewRewardedPost() {
        List<PostEntity> list = postRepository.findAll();
        List<PostListDto> rewardedPostList = new ArrayList<>();
        for (PostEntity post : list) {

            if (isApprove(post.getId()) && post.isRewarded()) {
                UserEntity user = userRepository.findById(post.getUser().getId())
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.UNAUTHORIZED));
                TagEntity tag = tagRepository.findById(post.getTag().getId())
                        .orElseThrow(() -> new AppException("Unknown tag", HttpStatus.UNAUTHORIZED));
                if (!tag.getTagName().equalsIgnoreCase("Q&A")){
                    PostListDto postListDto = new PostListDto(post.getId(),user.getUsername() ,post.getTitle(), post.getDescription() ,
                            post.getDateOfPost().toString(), getRelatedCategories(post.getCategory().getId()), tag.getTagName(), post.getCoverURL() ,post.isRewarded());
                    rewardedPostList.add(postListDto);
                }
            }
        }
        return rewardedPostList;
    }

    public List<QuestionAnswerDto> viewQuestionAndAnswerPost() {
        List<PostEntity> list = postRepository.findAll();
        List<QuestionAnswerDto> QAPostList = new ArrayList<>();
        for (PostEntity post : list) {

            if (isApprove(post.getId())) {
                UserEntity user = userRepository.findById(post.getUser().getId())
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.UNAUTHORIZED));
                TagEntity tag = tagRepository.findById(post.getTag().getId())
                        .orElseThrow(() -> new AppException("Unknown tag", HttpStatus.UNAUTHORIZED));
                if (tag.getTagName().equalsIgnoreCase("Q&A")){

                    int numOfUpvote = (post.getNumOfUpvote() != null) ? post.getNumOfUpvote() : 0;
                    int numOfDownvote = (post.getNumOfDownvote() != null) ? post.getNumOfDownvote() : 0;

                    QuestionAnswerDto questionAnswerDto = new QuestionAnswerDto(post.getId(),user.getUsername() ,post.getTitle(), post.getContent(),
                            post.getDateOfPost().toString(),numOfUpvote,numOfDownvote ,getRelatedCategories(post.getCategory().getId()),tag.getTagName(), post.getCoverURL(), post.isRewarded());
                    QAPostList.add(questionAnswerDto);
                }
            }
        }
        return QAPostList;
    }

    public List<PostListDto> viewByLatestPost(){
        List<PostEntity> postList = postRepository.findAll();
        List<PostListDto> latestPost = new ArrayList<>();

        for (PostEntity post: postList) {
            if(isApprove(post.getId())) {
                UserEntity user = userRepository.findById(post.getUser().getId())
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.UNAUTHORIZED));
                TagEntity tag = tagRepository.findById(post.getTag().getId())
                        .orElseThrow(() -> new AppException("Unknown tag", HttpStatus.UNAUTHORIZED));
                if (!tag.getTagName().equalsIgnoreCase("Q&A")) {
                    PostListDto postListDto = new PostListDto(post.getId(), user.getUsername() ,post.getTitle(), post.getDescription(),
                            post.getDateOfPost().toString(), getRelatedCategories(post.getCategory().getId()), tag.getTagName(), post.getCoverURL(),post.isRewarded());
                    latestPost.add(postListDto);
                    latestPost.sort(Comparator.comparing(PostListDto::getDateOfPost).reversed());
                }
            }
        }
        return latestPost;
    }



    // For lecturer
    // View pending post
    public boolean isPending(int id) {
        PostDetailsEntity post = postDetailsRepository.findByPostId(id)
                .orElseThrow(() -> new AppException("Unknown post", HttpStatus.UNAUTHORIZED));
        if (post.getType().equalsIgnoreCase("Request")) {
            return true;
        }
        return false;
    }

    public List<PostListDto> viewPendingPost(UserEntity userEntity){
        List<PostEntity> postList = postRepository.findAll();
        List<PostListDto> pendingPostList = new ArrayList<>();

        UserDetailsEntity userDetailsEntity = userDetailsRepository.findByUserAccount(userEntity)
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.UNAUTHORIZED));

        for (PostEntity post: postList) {
            if(isPending(post.getId())) {
                UserEntity user = userRepository.findById(post.getUser().getId())
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.UNAUTHORIZED));
                TagEntity tag = tagRepository.findById(post.getTag().getId())
                        .orElseThrow(() -> new AppException("Unknown tag", HttpStatus.UNAUTHORIZED));

                if (post.getCategory().getMajor().getId() == userDetailsEntity.getMajor().getId()) {
                    if (!tag.getTagName().equalsIgnoreCase("Q&A")) {
                        PostListDto postListDto = new PostListDto(post.getId(), user.getUsername() ,post.getTitle(), post.getDescription(),
                                post.getDateOfPost().toString(), getRelatedCategories(post.getCategory().getId()), tag.getTagName(), post.getCoverURL(),post.isRewarded());
                        pendingPostList.add(postListDto);
                    }
                }
            }
        }
        return pendingPostList;
    }

    public List<PostListDto> viewApprovedPost(UserEntity userEntity) {
        List<PostEntity> list = postRepository.findAll();
        List<PostListDto> approvePostList = new ArrayList<>();

        UserDetailsEntity userDetailsEntity = userDetailsRepository.findByUserAccount(userEntity)
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.UNAUTHORIZED));
        for (PostEntity post : list) {

            if (isApprove(post.getId())) {
                UserEntity user = userRepository.findById(post.getUser().getId())
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.UNAUTHORIZED));
                TagEntity tag = tagRepository.findById(post.getTag().getId())
                        .orElseThrow(() -> new AppException("Unknown tag", HttpStatus.UNAUTHORIZED));

                if (post.getCategory().getMajor().getId() == userDetailsEntity.getMajor().getId()) {
                    if (!tag.getTagName().equalsIgnoreCase("Q&A")){
                        PostListDto postListDto = new PostListDto(post.getId(),user.getUsername(),post.getTitle(), post.getDescription(),
                                post.getDateOfPost().toString(), getRelatedCategories(post.getCategory().getId()), tag.getTagName(), post.getCoverURL() ,post.isRewarded());
                        approvePostList.add(postListDto);
                    }
                }
            }
        }
        return approvePostList;
    }

    public void approvePost(int postId, UserEntity user){
        PostDetailsEntity postDetails = postDetailsRepository.findByPostId(postId)
                .orElseThrow(() -> new AppException("Unknown post", HttpStatus.UNAUTHORIZED));

        postDetails.setType("Approve");
        postDetails.setDateOfAction(Date.valueOf(java.time.LocalDate.now()));
        postDetails.setUser(user);
        postDetailsRepository.save(postDetails);
    }

    public void declinePost(int postId, String reasonOfDecline, UserEntity user){
        PostDetailsEntity postDetails = postDetailsRepository.findByPostId(postId)
                .orElseThrow(() -> new AppException("Unknown post", HttpStatus.UNAUTHORIZED));

        postDetails.setType("Decline");
        postDetails.setReasonOfDeclination(reasonOfDecline);
        postDetails.setDateOfAction(Date.valueOf(java.time.LocalDate.now()));
        postDetails.setUser(user);
        postDetailsRepository.save(postDetails);
    }

    // give reward
    public void giveReward(int postId){
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(()-> new AppException("Unknown post", HttpStatus.UNAUTHORIZED));

        if (!isApprove(post.getId()) && !isPending(post.getId())) {
            throw new AppException("Invalid postId", HttpStatus.UNAUTHORIZED);
        }

        post.setRewarded(true);
        postRepository.save(post);

    }
    // remove reward
    public void removeReward(int postId){
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(()-> new AppException("Unknown post", HttpStatus.UNAUTHORIZED));

        if (!isApprove(post.getId()) && !isPending(post.getId())) {
            throw new AppException("Invalid postId", HttpStatus.UNAUTHORIZED);
        }

        post.setRewarded(false);
        postRepository.save(post);
    }

    // view Q&A pending list
    public List<QuestionAnswerDto> viewQAPendingPost(){
        List<PostEntity> postList = postRepository.findAll();
        List<QuestionAnswerDto> QApendingPostList = new ArrayList<>();

        for (PostEntity post: postList) {
            if(isPending(post.getId())) {
                UserEntity user = userRepository.findById(post.getUser().getId())
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.UNAUTHORIZED));
                TagEntity tag = tagRepository.findById(post.getTag().getId())
                        .orElseThrow(() -> new AppException("Unknown tag", HttpStatus.UNAUTHORIZED));

                    if (tag.getTagName().equalsIgnoreCase("Q&A")) {
                        QuestionAnswerDto questionAnswerDto = new QuestionAnswerDto(post.getId(), user.getUsername() ,post.getTitle(), post.getContent(),
                                post.getDateOfPost().toString(), post.getNumOfUpvote(), post.getNumOfDownvote() ,getRelatedCategories(post.getCategory().getId()), tag.getTagName(), post.getCoverURL(), post.isRewarded());
                        QApendingPostList.add(questionAnswerDto);
                    }
            }
        }
        return QApendingPostList;
    }

    public void approveQAPost(int postId, UserEntity user){
        PostDetailsEntity postDetails = postDetailsRepository.findByPostId(postId)
                .orElseThrow(() -> new AppException("Unknown post", HttpStatus.UNAUTHORIZED));
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException("Unknown post", HttpStatus.UNAUTHORIZED));
        if (post.getTag().getTagName().equalsIgnoreCase("Q&A")) {
            postDetails.setType("Approve");
            postDetails.setDateOfAction(Date.valueOf(java.time.LocalDate.now()));
            postDetails.setUser(user);
            postDetailsRepository.save(postDetails);
        }else  {
            throw new AppException("This postId does not belong to Q&A tag", HttpStatus.UNAUTHORIZED);
        }

    }

    public void declineQAPost(int postId, String reasonOfDecline, UserEntity user){
        PostDetailsEntity postDetails = postDetailsRepository.findByPostId(postId)
                .orElseThrow(() -> new AppException("Unknown post", HttpStatus.UNAUTHORIZED));
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException("Unknown post", HttpStatus.UNAUTHORIZED));
        if (post.getTag().getTagName().equalsIgnoreCase("Q&A")) {
        postDetails.setType("Decline");
        postDetails.setReasonOfDeclination(reasonOfDecline);
        postDetails.setDateOfAction(Date.valueOf(java.time.LocalDate.now()));
        postDetails.setUser(user);
        postDetailsRepository.save(postDetails);
        }else  {
            throw new AppException("This postId does not belong to Q&A tag", HttpStatus.UNAUTHORIZED);
        }
    }


}
