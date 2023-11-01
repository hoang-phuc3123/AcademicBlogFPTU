package com.academicblogfptu.AcademicBlogFPTU.services;

import com.academicblogfptu.AcademicBlogFPTU.dtos.CommentDtos.CommentDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.PostDtos.*;
import com.academicblogfptu.AcademicBlogFPTU.entities.*;
import com.academicblogfptu.AcademicBlogFPTU.exceptions.AppException;
import com.academicblogfptu.AcademicBlogFPTU.repositories.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
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

    @Autowired
    private final CommentRepository commentRepository;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    ZoneId vietnamZone = ZoneId.of("Asia/Ho_Chi_Minh");

    // Get the current date and time in the specified time zone
    LocalDateTime localDateTime = LocalDateTime.now(vietnamZone);


    public List<PostListDto> viewAllPost() {
        List<PostEntity> list = postRepository.findAll();
        List<PostListDto> postList = new ArrayList<>();
        for (PostEntity post : list) {

            if (isApprove(post.getId())) {
                UserEntity user = userRepository.findById(post.getUser().getId())
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                TagEntity tag = tagRepository.findById(post.getTag().getId())
                        .orElseThrow(() -> new AppException("Unknown tag", HttpStatus.NOT_FOUND));

                if (!tag.getTagName().equalsIgnoreCase("Q&A")){
                    PostListDto postListDto = new PostListDto(post.getId(), user.getId(),userDetails.getFullName(), userDetails.getProfileURL(),post.getTitle(), post.getDescription(),
                            post.getDateOfPost().format(formatter), getRelatedCategories(post.getCategory().getId()), tag.getTagName(), post.getCoverURL() ,post.isRewarded(), post.getSlug());
                    postList.add(postListDto);
                }
            }
        }
        return postList;
    }

    public boolean isApprove(int id) {
        PostDetailsEntity post = postDetailsRepository.findByPostId(id)
                .orElseThrow(() -> new AppException("Unknown post", HttpStatus.NOT_FOUND));
        if (post.getType().equalsIgnoreCase("Approve")) {
            return true;
        }
        return false;
    }

    public PostDto viewPostById(String slug) {
        PostEntity post = postRepository.findBySlug(slug)
                .orElseThrow(() -> new AppException("Post with slug " + slug + " not found", HttpStatus.NOT_FOUND));

        UserEntity user = userRepository.findById(post.getUser().getId())
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
        UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));

        TagEntity tag = tagRepository.findById(post.getTag().getId())
                .orElseThrow(() -> new AppException("Unknown tag", HttpStatus.NOT_FOUND));

        int numOfUpvote = (post.getNumOfUpvote() != null) ? post.getNumOfUpvote() : 0;
        int numOfDownvote = (post.getNumOfDownvote() != null) ? post.getNumOfDownvote() : 0;

        return new PostDto(post.getId(),user.getId() ,userDetails.getFullName(), userDetails.getProfileURL() , post.getTitle(), post.getDescription() , post.getContent(),
                post.getDateOfPost().format(formatter), numOfUpvote, numOfDownvote, post.isRewarded(), post.isEdited(), post.isAllowComment(),
                getRelatedCategories(post.getCategory().getId()), tag.getTagName(), post.getCoverURL(), post.getSlug(), getCommentsForPost(post.getId()));
    }
    public List<CommentDto> getCommentsForPost(int postId) {
        List<CommentDto> comments = new ArrayList<>();

        List<CommentEntity> rootComments = commentRepository.findByPostIdAndParentCommentIsNull(postId);

        for (CommentEntity rootComment : rootComments) {
            Integer parentCommentId = (rootComment.getParentComment() !=  null) ? rootComment.getParentComment().getId() : 0;
            UserDetailsEntity userDetails = userDetailsRepository.findByUserId(rootComment.getUser().getId());
            CommentDto commentDto = new CommentDto(rootComment.getId(), userDetails.getFullName(), userDetails.getProfileURL(), rootComment.getContent(),
                    rootComment.isEdited(), rootComment.getNumOfUpvote(), rootComment.getNumOfDownvote(),
                    rootComment.getDateOfComment().format(formatter), rootComment.getPost().getId(), parentCommentId, null);

            List<CommentDto> replyComments = new ArrayList<>();
            getReplyComments(rootComment, replyComments);
            commentDto.setReplyComments(replyComments);

            comments.add(commentDto);
        }

        return comments;
    }

    private void getReplyComments(CommentEntity parentComment, List<CommentDto> replyComments) {
        List<CommentEntity> directReplyComments = commentRepository.findByParentComment(parentComment);

        for (CommentEntity replyComment : directReplyComments) {
            Integer parentCommentId = (replyComment.getParentComment() !=  null) ? replyComment.getParentComment().getId() : 0;
            UserDetailsEntity userDetails = userDetailsRepository.findByUserId(replyComment.getUser().getId());
            CommentDto commentDto = new CommentDto(replyComment.getId(), userDetails.getFullName(), userDetails.getProfileURL(), replyComment.getContent(),
                    replyComment.isEdited(), replyComment.getNumOfUpvote(), replyComment.getNumOfDownvote(),
                    replyComment.getDateOfComment().format(formatter), replyComment.getPost().getId(), parentCommentId, null);

            List<CommentDto> nestedReplyComments = new ArrayList<>();
            getReplyComments(replyComment, nestedReplyComments);
            commentDto.setReplyComments(nestedReplyComments);

            replyComments.add(commentDto);
        }
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
                .orElseThrow(() -> new AppException("Unknown post", HttpStatus.NOT_FOUND));
        postDetails.setType("Delete");
        postDetails.setDateOfAction(LocalDateTime.of(java.time.LocalDate.now(), java.time.LocalTime.now()));
        postDetails.setUser(null);
        postDetailsRepository.save(postDetails);
    }

    public PostDto requestPost(RequestPostDto requestPostDto, UserEntity userEntity){
        PostEntity newPostEntity = new PostEntity();

        newPostEntity.setTitle(requestPostDto.getTitle());
        if (!requestPostDto.getDescription().isEmpty()){
            newPostEntity.setDescription(requestPostDto.getDescription());
        }else {
            newPostEntity.setDescription(null);
        }
        newPostEntity.setContent(requestPostDto.getContent());
        newPostEntity.setDateOfPost(LocalDateTime.of(java.time.LocalDate.now(), java.time.LocalTime.now()));
        newPostEntity.setNumOfUpvote(0);
        newPostEntity.setNumOfDownvote(0);
        newPostEntity.setRewarded(false);
        newPostEntity.setEdited(false);
        newPostEntity.setAllowComment(requestPostDto.isAllowComment());
        newPostEntity.setLength(requestPostDto.getLength());
        UserEntity user = userRepository.findById(userEntity.getId())
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
        UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
        newPostEntity.setUser(user);
        CategoryEntity category = categoryRepository.findById(requestPostDto.getCategoryId())
                .orElseThrow(() -> new AppException("Unknown category", HttpStatus.NOT_FOUND));
        newPostEntity.setCategory(category);
        TagEntity tag = tagRepository.findById(requestPostDto.getTagId())
                .orElseThrow(() -> new AppException("Unknown tag", HttpStatus.NOT_FOUND));
        newPostEntity.setTag(tag);
        if (!requestPostDto.getCoverURL().isEmpty()){
            newPostEntity.setCoverURL(requestPostDto.getCoverURL());
        }else {
            newPostEntity.setCoverURL(null);
        }

        newPostEntity.setSlug(requestPostDto.getSlug());

        postRepository.save(newPostEntity);


        return new PostDto(newPostEntity.getId(), user.getId() ,userDetails.getFullName() , userDetails.getProfileURL(),newPostEntity.getTitle(), newPostEntity.getDescription(), newPostEntity.getContent(), newPostEntity.getDateOfPost().format(formatter)
        , newPostEntity.getNumOfUpvote(), newPostEntity.getNumOfDownvote(), newPostEntity.isRewarded(), newPostEntity.isEdited()
        , newPostEntity.isAllowComment() ,getRelatedCategories(newPostEntity.getCategory().getId()), newPostEntity.getTag().getTagName(),
                newPostEntity.getCoverURL(), newPostEntity.getSlug(), getCommentsForPost(newPostEntity.getId()));
    }

    public void postDetail(int postId, String type){
        Optional<PostEntity> post = postRepository.findById(postId);
        PostEntity postEntity = post.get();

        PostDetailsEntity newPostDetails = new PostDetailsEntity();
        newPostDetails.setDateOfAction(LocalDateTime.of(java.time.LocalDate.now(), java.time.LocalTime.now()));
        newPostDetails.setType(type);
        newPostDetails.setPost(postEntity);

        postDetailsRepository.save(newPostDetails);
    }

    public void postDetailLecturer(int postId, UserEntity user){
        Optional<PostEntity> post = postRepository.findById(postId);
        PostEntity postEntity = post.get();

        PostDetailsEntity newPostDetails = new PostDetailsEntity();
        newPostDetails.setDateOfAction(LocalDateTime.of(java.time.LocalDate.now(), java.time.LocalTime.now()));
        newPostDetails.setType("Approve");
        newPostDetails.setUser(user);
        newPostDetails.setPost(postEntity);

        postDetailsRepository.save(newPostDetails);
    }

    public PostDto editPost(EditPostDto editPostDto){
        PostEntity post = postRepository.findById(editPostDto.getPostId())
                .orElseThrow(() -> new AppException("Unknown post", HttpStatus.NOT_FOUND));
        post.setEdited(false);
        postRepository.save(post);

        //tạo bài post mới
        PostEntity newPost = new PostEntity();

        newPost.setTitle(editPostDto.getTitle());
        if (!editPostDto.getDescription().isEmpty()){
            newPost.setDescription(editPostDto.getDescription());
        }else {
            newPost.setDescription(null);
        }
        newPost.setContent(editPostDto.getContent());
        newPost.setDateOfPost(LocalDateTime.of(java.time.LocalDate.now(), java.time.LocalTime.now()));
        newPost.setNumOfUpvote(0);
        newPost.setNumOfDownvote(0);
        newPost.setRewarded(false);
        newPost.setEdited(true);
        newPost.setLength(editPostDto.getLength());
        newPost.setAllowComment(editPostDto.isAllowComment());
        UserEntity user = userRepository.findById(post.getUser().getId())
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
        UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
        newPost.setUser(user);
        newPost.setParentPost(post);
        CategoryEntity category = categoryRepository.findById(editPostDto.getCategoryId())
                .orElseThrow(() -> new AppException("Unknown category", HttpStatus.NOT_FOUND));
        newPost.setCategory(category);
        TagEntity tag = tagRepository.findById(editPostDto.getTagId())
                .orElseThrow(() -> new AppException("Unknown tag", HttpStatus.NOT_FOUND));
        newPost.setTag(tag);

        if (!editPostDto.getCoverURL().isEmpty()){
            newPost.setCoverURL(editPostDto.getCoverURL());
        }else {
            newPost.setCoverURL(null);
        }

        newPost.setSlug(editPostDto.getSlug());

        postRepository.save(newPost);

        PostDetailsEntity postDetails = postDetailsRepository.findByPostId(post.getId())
                .orElseThrow(() -> new AppException("Unknown post", HttpStatus.NOT_FOUND));

        postDetails.setType("Edit");
        postDetails.setUser(null);
        postDetails.setDateOfAction(LocalDateTime.of(java.time.LocalDate.now(), java.time.LocalTime.now()));
        postDetailsRepository.save(postDetails);

        return new PostDto(newPost.getId(), user.getId(),userDetails.getFullName() , userDetails.getProfileURL(),newPost.getTitle(), newPost.getDescription(),newPost.getContent(), newPost.getDateOfPost().format(formatter)
                , newPost.getNumOfUpvote(), newPost.getNumOfDownvote(), newPost.isRewarded(), newPost.isEdited()
                , newPost.isAllowComment() ,getRelatedCategories(newPost.getCategory().getId()), newPost.getTag().getTagName(), newPost.getCoverURL(), newPost.getSlug(), getCommentsForPost(newPost.getId()));
    }

    public List<PostListDto> viewRewardedPost() {
        List<PostEntity> list = postRepository.findAll();
        List<PostListDto> rewardedPostList = new ArrayList<>();
        for (PostEntity post : list) {

            if (isApprove(post.getId()) && post.isRewarded()) {
                UserEntity user = userRepository.findById(post.getUser().getId())
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                TagEntity tag = tagRepository.findById(post.getTag().getId())
                        .orElseThrow(() -> new AppException("Unknown tag", HttpStatus.NOT_FOUND));
                if (!tag.getTagName().equalsIgnoreCase("Q&A")){
                    PostListDto postListDto = new PostListDto(post.getId(), user.getId(),userDetails.getFullName(), userDetails.getProfileURL() ,post.getTitle(), post.getDescription() ,
                            post.getDateOfPost().format(formatter), getRelatedCategories(post.getCategory().getId()), tag.getTagName(), post.getCoverURL() ,post.isRewarded(), post.getSlug());
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
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                TagEntity tag = tagRepository.findById(post.getTag().getId())
                        .orElseThrow(() -> new AppException("Unknown tag", HttpStatus.NOT_FOUND));
                if (tag.getTagName().equalsIgnoreCase("Q&A")){

                    int numOfUpvote = (post.getNumOfUpvote() != null) ? post.getNumOfUpvote() : 0;
                    int numOfDownvote = (post.getNumOfDownvote() != null) ? post.getNumOfDownvote() : 0;

                    QuestionAnswerDto questionAnswerDto = new QuestionAnswerDto(post.getId(), user.getId() , userDetails.getFullName(), userDetails.getProfileURL(),post.getTitle(), post.getDescription(),post.getContent(),
                            post.getDateOfPost().format(formatter),numOfUpvote,numOfDownvote ,getRelatedCategories(post.getCategory().getId()),tag.getTagName(), post.getCoverURL(), post.isRewarded(), post.getSlug());
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
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                TagEntity tag = tagRepository.findById(post.getTag().getId())
                        .orElseThrow(() -> new AppException("Unknown tag", HttpStatus.NOT_FOUND));
                if (!tag.getTagName().equalsIgnoreCase("Q&A")) {
                    PostListDto postListDto = new PostListDto(post.getId(), user.getId() ,userDetails.getFullName(), userDetails.getProfileURL() ,post.getTitle(), post.getDescription(),
                            post.getDateOfPost().format(formatter), getRelatedCategories(post.getCategory().getId()), tag.getTagName(), post.getCoverURL(),post.isRewarded(), post.getSlug());
                    latestPost.add(postListDto);
                    latestPost.sort(Comparator.comparing(PostListDto::getDateOfPost).reversed());
                }
            }
        }
        return latestPost;
    }

    //View trending post
    public List<PostListTrendingDto> viewTrending(){
        List<PostEntity> postList = postRepository.findAll();
        List<PostListTrendingDto> trendingPost = new ArrayList<>();
        LocalDateTime now = LocalDateTime.of(java.time.LocalDate.now(), java.time.LocalTime.now());
        LocalDateTime sevenDaysAgo = LocalDateTime.of(java.time.LocalDate.now(), java.time.LocalTime.now()).minusDays(7);
        for (PostEntity post: postList) {
            if(isApprove(post.getId())) {
                if (post.getDateOfPost().format(formatter).compareTo(sevenDaysAgo.format(formatter)) > 0 && post.getDateOfPost().format(formatter).compareTo(now.format(formatter)) < 0) {
                    UserEntity user = userRepository.findById(post.getUser().getId())
                            .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                    UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                            .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                    TagEntity tag = tagRepository.findById(post.getTag().getId())
                            .orElseThrow(() -> new AppException("Unknown tag", HttpStatus.NOT_FOUND));
                    if (!tag.getTagName().equalsIgnoreCase("Q&A")) {
                        PostListTrendingDto postListTrendingDto = new PostListTrendingDto(post.getId(), user.getId() ,userDetails.getFullName(), userDetails.getProfileURL() ,post.getTitle(), post.getDescription(),
                                post.getDateOfPost().format(formatter), getRelatedCategories(post.getCategory().getId()), tag.getTagName(), post.getCoverURL(),post.isRewarded(), post.getNumOfUpvote(), post.getNumOfDownvote(), post.getSlug());
                        trendingPost.add(postListTrendingDto);
                        Collections.sort(trendingPost, Comparator.comparingInt(
                                trendingPosts -> trendingPosts.getNumOfUpVote() - trendingPosts.getNumOfDownVote()));
                        Collections.reverse(trendingPost);
                    }
                }
            }
        }
        return trendingPost;
    }

    public List<PostListDto> viewShort(){
        List<PostEntity> postList = postRepository.findAll();
        List<PostListDto> shortPost = new ArrayList<>();

        for (PostEntity post: postList) {
            if(isApprove(post.getId()) && post.getLength() <= 300) {
                UserEntity user = userRepository.findById(post.getUser().getId())
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                TagEntity tag = tagRepository.findById(post.getTag().getId())
                        .orElseThrow(() -> new AppException("Unknown tag", HttpStatus.NOT_FOUND));
                if (!tag.getTagName().equalsIgnoreCase("Q&A")) {
                    PostListDto postListDto = new PostListDto(post.getId(), user.getId() ,userDetails.getFullName(), userDetails.getProfileURL() ,post.getTitle(), post.getDescription(),
                            post.getDateOfPost().format(formatter), getRelatedCategories(post.getCategory().getId()), tag.getTagName(), post.getCoverURL(),post.isRewarded(), post.getSlug());
                    shortPost.add(postListDto);
                }
            }
        }
        return shortPost;
    }

    // View edit post history
    public List<PostDto> viewPostEditHistory(int postId) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException("Unknown post", HttpStatus.NOT_FOUND));
        List<PostDto> postEditHistoryList = new ArrayList<>();

        if (post.getParentPost() == null) {
            addPostToList(post, postEditHistoryList);
        } else {
            // Recursively find the parent posts
            findParentPosts(post, postEditHistoryList);
        }
        return postEditHistoryList;
    }

    private void findParentPosts(PostEntity postEntity, List<PostDto> postEditHistoryList) {
        // Find and add the parent post (if it exists)
        PostEntity parentPost = postEntity.getParentPost();
        if (parentPost != null) {
            addPostToList(parentPost, postEditHistoryList);

            // Continue the recursion if the parent has a parent
            if (parentPost.getParentPost() != null) {
                findParentPosts(parentPost, postEditHistoryList);
            }
        }
    }

    private void addPostToList(PostEntity postEntity, List<PostDto> postEditHistoryList) {
        UserEntity user = userRepository.findById(postEntity.getUser().getId())
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
        UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
        TagEntity tag = tagRepository.findById(postEntity.getTag().getId())
                .orElseThrow(() -> new AppException("Unknown tag", HttpStatus.NOT_FOUND));
        postEditHistoryList.add(new PostDto(postEntity.getId(), user.getId(),userDetails.getFullName(), userDetails.getProfileURL(), postEntity.getTitle(), postEntity.getDescription(),
                postEntity.getContent(), postEntity.getDateOfPost().format(formatter), postEntity.getNumOfUpvote(), postEntity.getNumOfDownvote(),
                postEntity.isRewarded(), postEntity.isEdited(), postEntity.isAllowComment(), getRelatedCategories(postEntity.getCategory().getId()),
                tag.getTagName(), postEntity.getCoverURL(), postEntity.getSlug(), null
        ));
    }

    public void commentToggle(int postId){
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException("Unknown post", HttpStatus.NOT_FOUND));
        if (post.isAllowComment()){
            post.setAllowComment(false);
        } else {
            post.setAllowComment(true);
        }
        postRepository.save(post);
    }

    public boolean isDraft(int id) {
        PostDetailsEntity post = postDetailsRepository.findByPostId(id)
                .orElseThrow(() -> new AppException("Unknown post", HttpStatus.NOT_FOUND));
        if (post.getType().equalsIgnoreCase("Draft")) {
            return true;
        }
        return false;
    }

    public List<PostListDto> viewDraft(int userId) {
        List<PostEntity> list = postRepository.findAll();
        List<PostListDto> draftList = new ArrayList<>();
        for (PostEntity post : list) {

            if (isDraft(post.getId()) && post.getUser().getId() == userId) {
                UserEntity user = userRepository.findById(post.getUser().getId())
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                TagEntity tag = tagRepository.findById(post.getTag().getId())
                        .orElseThrow(() -> new AppException("Unknown tag", HttpStatus.NOT_FOUND));

                if (!tag.getTagName().equalsIgnoreCase("Q&A")){
                    PostListDto postListDto = new PostListDto(post.getId(), user.getId(),userDetails.getFullName(), userDetails.getProfileURL(),post.getTitle(), post.getDescription(),
                            post.getDateOfPost().format(formatter), getRelatedCategories(post.getCategory().getId()), tag.getTagName(), post.getCoverURL() ,post.isRewarded(), post.getSlug());
                    draftList.add(postListDto);
                }
            }
        }
        return draftList;
    }


    // For lecturer
    // View pending post
    public boolean isPending(int id) {
        PostDetailsEntity post = postDetailsRepository.findByPostId(id)
                .orElseThrow(() -> new AppException("Unknown post", HttpStatus.NOT_FOUND));
        if (post.getType().equalsIgnoreCase("Request")) {
            return true;
        }
        return false;
    }

    public List<PostListDto> viewPendingPost(UserEntity userEntity){
        List<PostEntity> postList = postRepository.findAll();
        List<PostListDto> pendingPostList = new ArrayList<>();

        UserDetailsEntity userDetailsEntity = userDetailsRepository.findByUserAccount(userEntity)
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));

        for (PostEntity post: postList) {
            if(isPending(post.getId())) {
                UserEntity user = userRepository.findById(post.getUser().getId())
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                TagEntity tag = tagRepository.findById(post.getTag().getId())
                        .orElseThrow(() -> new AppException("Unknown tag", HttpStatus.NOT_FOUND));

                if (post.getCategory().getMajor().getId() == userDetailsEntity.getMajor().getId()) {
                    if (!tag.getTagName().equalsIgnoreCase("Q&A")) {
                        PostListDto postListDto = new PostListDto(post.getId(), user.getId(),userDetails.getFullName(), userDetails.getProfileURL() ,post.getTitle(), post.getDescription(),
                                post.getDateOfPost().format(formatter), getRelatedCategories(post.getCategory().getId()), tag.getTagName(), post.getCoverURL(),post.isRewarded(), post.getSlug());
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
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
        for (PostEntity post : list) {

            if (isApprove(post.getId())) {
                UserEntity user = userRepository.findById(post.getUser().getId())
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                TagEntity tag = tagRepository.findById(post.getTag().getId())
                        .orElseThrow(() -> new AppException("Unknown tag", HttpStatus.NOT_FOUND));

                if (post.getCategory().getMajor().getId() == userDetailsEntity.getMajor().getId()) {
                    if (!tag.getTagName().equalsIgnoreCase("Q&A")){
                        PostListDto postListDto = new PostListDto(post.getId(),user.getId(),userDetails.getFullName(), userDetails.getProfileURL(),post.getTitle(), post.getDescription(),
                                post.getDateOfPost().format(formatter), getRelatedCategories(post.getCategory().getId()), tag.getTagName(), post.getCoverURL() ,post.isRewarded(), post.getSlug());
                        approvePostList.add(postListDto);
                    }
                }
            }
        }
        return approvePostList;
    }

    public void approvePost(int postId, UserEntity user){
        PostDetailsEntity postDetails = postDetailsRepository.findByPostId(postId)
                .orElseThrow(() -> new AppException("Unknown post", HttpStatus.NOT_FOUND));

        postDetails.setType("Approve");
        postDetails.setDateOfAction(LocalDateTime.of(java.time.LocalDate.now(), java.time.LocalTime.now()));
        postDetails.setUser(user);
        postDetailsRepository.save(postDetails);

        PostEntity postEntity = postRepository.findById(postId)
                .orElseThrow(() -> new AppException("Unknown post", HttpStatus.NOT_FOUND));
        postEntity.setDateOfPost(postDetails.getDateOfAction());
        postRepository.save(postEntity);
    }

    public void declinePost(int postId, String reasonOfDecline, UserEntity user){
        PostDetailsEntity postDetails = postDetailsRepository.findByPostId(postId)
                .orElseThrow(() -> new AppException("Unknown post", HttpStatus.NOT_FOUND));

        postDetails.setType("Decline");
        postDetails.setReasonOfDeclination(reasonOfDecline);
        postDetails.setDateOfAction(LocalDateTime.of(java.time.LocalDate.now(), java.time.LocalTime.now()));
        postDetails.setUser(user);
        postDetailsRepository.save(postDetails);
    }

    // give reward
    public void giveReward(int postId){
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(()-> new AppException("Unknown post", HttpStatus.NOT_FOUND));

        if (!isApprove(post.getId()) && !isPending(post.getId())) {
            throw new AppException("Invalid postId", HttpStatus.NOT_FOUND);
        }

        post.setRewarded(true);
        postRepository.save(post);

    }
    // remove reward
    public void removeReward(int postId){
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(()-> new AppException("Unknown post", HttpStatus.NOT_FOUND));

        if (!isApprove(post.getId()) && !isPending(post.getId())) {
            throw new AppException("Invalid postId", HttpStatus.NOT_FOUND);
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
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                TagEntity tag = tagRepository.findById(post.getTag().getId())
                        .orElseThrow(() -> new AppException("Unknown tag", HttpStatus.NOT_FOUND));

                    if (tag.getTagName().equalsIgnoreCase("Q&A")) {
                        QuestionAnswerDto questionAnswerDto = new QuestionAnswerDto(post.getId(), user.getId() ,userDetails.getFullName(),userDetails.getProfileURL(),post.getTitle(), post.getDescription() ,post.getContent(),
                                post.getDateOfPost().format(formatter), post.getNumOfUpvote(), post.getNumOfDownvote() ,getRelatedCategories(post.getCategory().getId()), tag.getTagName(), post.getCoverURL(), post.isRewarded(), post.getSlug());
                        QApendingPostList.add(questionAnswerDto);
                    }
            }
        }
        return QApendingPostList;
    }

    public void approveQAPost(int postId, UserEntity user){
        PostDetailsEntity postDetails = postDetailsRepository.findByPostId(postId)
                .orElseThrow(() -> new AppException("Unknown post", HttpStatus.NOT_FOUND));
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException("Unknown post", HttpStatus.NOT_FOUND));
        if (post.getTag().getTagName().equalsIgnoreCase("Q&A")) {
            postDetails.setType("Approve");
            postDetails.setDateOfAction(LocalDateTime.of(java.time.LocalDate.now(), java.time.LocalTime.now()));
            postDetails.setUser(user);
            postDetailsRepository.save(postDetails);

            post.setDateOfPost(postDetails.getDateOfAction());
            postRepository.save(post);

        }else  {
            throw new AppException("This postId does not belong to Q&A tag", HttpStatus.NOT_FOUND);
        }

    }

    public void declineQAPost(int postId, String reasonOfDecline, UserEntity user){
        PostDetailsEntity postDetails = postDetailsRepository.findByPostId(postId)
                .orElseThrow(() -> new AppException("Unknown post", HttpStatus.NOT_FOUND));
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException("Unknown post", HttpStatus.NOT_FOUND));
        if (post.getTag().getTagName().equalsIgnoreCase("Q&A")) {
        postDetails.setType("Decline");
        postDetails.setReasonOfDeclination(reasonOfDecline);
            postDetails.setDateOfAction(LocalDateTime.of(java.time.LocalDate.now(), java.time.LocalTime.now()));
        postDetails.setUser(user);
        postDetailsRepository.save(postDetails);
        }else  {
            throw new AppException("This postId does not belong to Q&A tag", HttpStatus.NOT_FOUND);
        }
    }


}
