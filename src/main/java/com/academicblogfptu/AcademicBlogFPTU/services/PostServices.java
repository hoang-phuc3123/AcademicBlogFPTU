package com.academicblogfptu.AcademicBlogFPTU.services;

import com.academicblogfptu.AcademicBlogFPTU.dtos.CategoryAndTagDtos.CategoryListDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.CategoryAndTagDtos.TagDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.CommentDtos.CommentDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.MailStructureDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.PostDtos.*;
import com.academicblogfptu.AcademicBlogFPTU.dtos.SearchMultipleDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos.RewarderDto;
import com.academicblogfptu.AcademicBlogFPTU.entities.*;
import com.academicblogfptu.AcademicBlogFPTU.exceptions.AppException;
import com.academicblogfptu.AcademicBlogFPTU.repositories.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;


import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


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

    @Autowired
    private final FollowerRepository followerRepository;

    @Autowired
    private final FollowerServices followerServices;
    @Autowired
    private NotifyByMailServices notifyByMailServices;
    @Autowired
    private final BadgeServices badgeServices;

    @Autowired
    private final SkillRepository skillRepository;

    @Autowired
    private final PostSkillRepository postSkillRepository;

    @Autowired
    private final PostRewardRepository postRewardRepository;

    @Autowired
    private final UserSkillRepository userSkillRepository;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    ZoneId vietnamZone = ZoneId.of("Asia/Ho_Chi_Minh");

    // Get the current date and time in the specified time zone
    LocalDateTime localDateTime = LocalDateTime.now(vietnamZone);


    public Map<String, Object> viewAllPost(int page, int postOfPage) {
        int offSet = (page - 1)*postOfPage;
        List<PostEntity> list = postRepository.findPostsPaged(offSet, postOfPage);
        List<PostListDto> postList = new ArrayList<>();
        Map<String, Object> result = new HashMap<>();
        for (PostEntity post : list) {

            if (isApprove(post.getId())) {
                UserEntity user = userRepository.findById(post.getUser().getId())
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                TagEntity tag = tagRepository.findById(post.getTag().getId())
                        .orElseThrow(() -> new AppException("Unknown tag", HttpStatus.NOT_FOUND));

                List<PostSkillEntity> postSkill = postSkillRepository.findByPost(post);

                if (!tag.getTagName().equalsIgnoreCase("Q&A")){
                    PostListDto postListDto = new PostListDto(post.getId(), user.getId(),userDetails.getFullName(), userDetails.getProfileURL(),post.getTitle(), post.getDescription(),
                            post.getDateOfPost().format(formatter), getCategoriesOfPost(getRelatedCategories(post.getCategory().getId())), getTagOfPost(tag), post.getCoverURL() ,post.isRewarded(), post.getSlug());
                    postList.add(postListDto);
                }
            }
        }
        result.put("Posts", postList);
        result.put("TotalPost", postRepository.countTotalPost());

        return result;
    }

    public boolean isApprove(int id) {
        PostDetailsEntity post = postDetailsRepository.findByPostId(id)
                .orElseThrow(() -> new AppException("Unknown post", HttpStatus.NOT_FOUND));
        if (post.getType().equalsIgnoreCase("Approve")) {
            return true;
        }
        return false;
    }

    public boolean isDelete(int id) {
        PostDetailsEntity post = postDetailsRepository.findByPostId(id)
                .orElseThrow(() -> new AppException("Unknown post", HttpStatus.NOT_FOUND));
        if (post.getType().equalsIgnoreCase("Delete")) {
            return true;
        }
        return false;
    }

    public PostDto viewPostBySlug(String slug, UserEntity userEntity) {

        PostEntity post = postRepository.findBySlug(slug)
                .orElseThrow(() -> new AppException("Post with slug " + slug + " not found", HttpStatus.NOT_FOUND));

        PostDetailsEntity postDetails = postDetailsRepository.findByPostId(post.getId())
                .orElseThrow(() -> new AppException("Post details not found", HttpStatus.NOT_FOUND));

        if (isDelete(post.getId())){
            throw new AppException("Post not found", HttpStatus.NOT_FOUND);
        }

        UserEntity user = userRepository.findById(post.getUser().getId())
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
        UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));

        if ((isDraft(post.getId()) || isDeclined(post.getId())) && !(post.getUser().getId() == userEntity.getId()) ){
            throw new AppException("You can not see other user's draft or decline post", HttpStatus.NOT_FOUND);
        }

        TagEntity tag = tagRepository.findById(post.getTag().getId())
                .orElseThrow(() -> new AppException("Unknown tag", HttpStatus.NOT_FOUND));

        int numOfUpvote = (post.getNumOfUpvote() != null) ? post.getNumOfUpvote() : 0;
        int numOfDownvote = (post.getNumOfDownvote() != null) ? post.getNumOfDownvote() : 0;

        boolean isPending ;
        if (postDetails.getType().equalsIgnoreCase("Request")){
            isPending = true;
        }else {
            isPending = false;
        }

        List<PostSkillEntity> postSkills = postSkillRepository.findByPost(post);

        List<PostRewardEntity> postRewards = postRewardRepository.findByPost(post);

        List<BadgeEntity> badges = badgeServices.findBadgesByUserId(user.getId());

        String reasonOfDecline = (postDetails.getReasonOfDeclination() != null) ? postDetails.getReasonOfDeclination() : null;
             return new PostDto(post.getId(),user.getId() ,userDetails.getFullName(), userDetails.getProfileURL() , post.getTitle(), post.getDescription() , post.getContent(),
                    post.getDateOfPost().format(formatter), numOfUpvote, numOfDownvote, post.isRewarded(), post.isEdited(), post.isAllowComment(),
                    getCategoriesOfPost(getRelatedCategories(post.getCategory().getId())), getTagOfPost(tag), post.getCoverURL(), post.getSlug(),getCommentsForPost(post.getId()), reasonOfDecline, getSkillOfPost(postSkills), getRewarderForPost(postRewards), badges, isPending);

    }
    public List<CommentDto> getCommentsForPost(int postId) {
        List<CommentDto> comments = new ArrayList<>();

        List<CommentEntity> rootComments = commentRepository.findByPostId(postId);

        for (CommentEntity rootComment : rootComments) {
            Integer parentCommentId = (rootComment.getParentComment() !=  null) ? rootComment.getParentComment().getId() : null;
            UserDetailsEntity userDetails = userDetailsRepository.findByUserId(rootComment.getUser().getId());
            List<BadgeEntity> userBadges = badgeServices.findBadgesByUserId(rootComment.getUser().getId());
            CommentDto commentDto = new CommentDto(rootComment.getId(), rootComment.getUser().getId(),userDetails.getFullName(), userDetails.getProfileURL(), rootComment.getContent(),
                    rootComment.isEdited(), rootComment.getNumOfUpvote(), rootComment.getNumOfDownvote(),
                    rootComment.getDateOfComment().format(formatter), rootComment.getPost().getId(), parentCommentId, userBadges);

            comments.add(commentDto);
        }

        return comments;
    }

    public List<CategoryListDto> getCategoriesOfPost(List<CategoryEntity> getRelatedCategories){
        List<CategoryListDto> getCategories = new ArrayList<>();
        for (CategoryEntity category: getRelatedCategories) {
            CategoryListDto categoryListDto = new CategoryListDto(category.getId(), category.getCategoryName(), category.getCategoryType());
            getCategories.add(categoryListDto);
        }
        getCategories.sort(Comparator.comparing(CategoryListDto::getCategoryId));
        return getCategories;
    }

    public List<SkillEntity> getSkillOfPost(List<PostSkillEntity> postSkills){
        List<SkillEntity> skill = new ArrayList<>();
        for (PostSkillEntity skills: postSkills) {
            SkillEntity skillEntity = skillRepository.findById(skills.getSkill().getId())
                    .orElseThrow(()-> new AppException("Unknown skill", HttpStatus.NOT_FOUND));
            skill.add(skillEntity);
        }
        skill.sort(Comparator.comparing(SkillEntity::getId));
        return skill;
    }

    public List<RewarderDto> getRewarderForPost(List<PostRewardEntity> postRewards){
        List<RewarderDto> rewarder = new ArrayList<>();
        for (PostRewardEntity postRewardEntity: postRewards) {
            if (postRewardEntity.getStatus().equalsIgnoreCase("Accepted") || postRewardEntity.getStatus().equalsIgnoreCase("Pending")){
                List<BadgeEntity> userBadges = badgeServices.findBadgesByUserId(postRewardEntity.getUser().getId());
                UserEntity user = userRepository.findById(postRewardEntity.getUser().getId())
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                rewarder.add(new RewarderDto(user.getId(), userDetails.getProfileURL(), userDetails.getFullName(), userBadges));
            }
        }
        rewarder.sort(Comparator.comparing(RewarderDto::getUserId));
        return rewarder;
    }

    public TagDto getTagOfPost(TagEntity tag){
        return new TagDto(tag.getId(), tag.getTagName());
    }


    public List<CategoryEntity> getRelatedCategories(Integer categoryId) {
        List<CategoryEntity> relatedCategories = new ArrayList<>();

        if (categoryId == null) {
            // Handle the case where categoryId is null (or perform appropriate error handling)
            return relatedCategories;
        }

        // Find the initial category by its id
        CategoryEntity initialCategory = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category with name " + categoryId + " not found"));

        // Add the initial category to the list
        relatedCategories.add(initialCategory);
        if (initialCategory.getParentID() != null) {
            findParentCategory(initialCategory, relatedCategories);
            if (!initialCategory.getCategoryType().equals("Semester") ){
                findChildCategories(initialCategory, relatedCategories);
            }
        }

        //relatedCategories.sort(Comparator.comparing(String::length).reversed());
        return relatedCategories;
    }

    private void findParentCategory(CategoryEntity category, List<CategoryEntity> relatedCategories) {
        // Find and add the parent category (if it exists)
        Integer parentID = category.getParentID();
        if (parentID != null) {
            CategoryEntity parentCategory = categoryRepository.findById(parentID).orElse(null);
            if (parentCategory != null && !relatedCategories.contains(parentCategory.getCategoryName())) {
                relatedCategories.add(parentCategory);
                // Stop when the parent is the root category (has no parent)
                if (parentCategory.getParentID() != null) {
                    findParentCategory(parentCategory, relatedCategories);
                }
            }
        }
    }

    private void findChildCategories(CategoryEntity category, List<CategoryEntity> relatedCategories) {
        // Find and add child categories
        List<CategoryEntity> childCategories = categoryRepository.findByParentID(category.getId());
        for (CategoryEntity child : childCategories) {
            if (child != null && !relatedCategories.contains(child.getCategoryName())) {
                relatedCategories.add(child);
                findChildCategories(child, relatedCategories);
            }
        }
    }

    public void deletePostById(int id){

        PostEntity post = postRepository.findById(id)
                .orElseThrow(() -> new AppException("Unknown post", HttpStatus.NOT_FOUND));

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
        if (requestPostDto.getCoverURL() != null){
            newPostEntity.setCoverURL(requestPostDto.getCoverURL());
        }else {
            newPostEntity.setCoverURL(null);
        }

        newPostEntity.setSlug(requestPostDto.getSlug());

        postRepository.save(newPostEntity);

        for (String postSkill: requestPostDto.getPostSkill()) {
            PostSkillEntity postSkillEntity = new PostSkillEntity();
            SkillEntity skill = skillRepository.findBySkillName(postSkill)
                    .orElseThrow(()-> new AppException("Unknown skill", HttpStatus.NOT_FOUND));
            postSkillEntity.setPost(newPostEntity);
            postSkillEntity.setSkill(skill);
            postSkillRepository.save(postSkillEntity);
        }

        List<PostSkillEntity> postSkills = postSkillRepository.findByPost(newPostEntity);

        return new PostDto(newPostEntity.getId(), user.getId() ,userDetails.getFullName() , userDetails.getProfileURL(),newPostEntity.getTitle(), newPostEntity.getDescription(), newPostEntity.getContent(), newPostEntity.getDateOfPost().format(formatter)
        , newPostEntity.getNumOfUpvote(), newPostEntity.getNumOfDownvote(), newPostEntity.isRewarded(), newPostEntity.isEdited()
        , newPostEntity.isAllowComment() ,getCategoriesOfPost(getRelatedCategories(newPostEntity.getCategory().getId())), getTagOfPost(newPostEntity.getTag()),
                newPostEntity.getCoverURL(), newPostEntity.getSlug(), getCommentsForPost(newPostEntity.getId()), null, getSkillOfPost(postSkills), null, null, true);
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
        PostEntity editPost = postRepository.findById(editPostDto.getPostId())
                .orElseThrow(() -> new AppException("Unknown post", HttpStatus.NOT_FOUND));
        PostDetailsEntity editPostDetails = postDetailsRepository.findByPostId(editPost.getId())
                .orElseThrow(() -> new AppException("Unknown post", HttpStatus.NOT_FOUND));

        if (editPostDetails.getType().equalsIgnoreCase("Request")) {
            editPostDetails.setDateOfAction(LocalDateTime.of(java.time.LocalDate.now(), java.time.LocalTime.now()));
            postDetailsRepository.save(editPostDetails);
            return editDraft(editPostDto);
        } else {
            //tạo bài post mới lưu bài viết cũ vào database để view edit history
            PostEntity newPost = new PostEntity();

            newPost.setTitle(editPost.getTitle());
            if (!editPost.getDescription().isEmpty()){
                newPost.setDescription(editPost.getDescription());
            }else {
                newPost.setDescription(null);
            }
            newPost.setContent(editPost.getContent());
            newPost.setDateOfPost(LocalDateTime.of(java.time.LocalDate.now(), java.time.LocalTime.now()));
            newPost.setNumOfUpvote(0);
            newPost.setNumOfDownvote(0);
            newPost.setRewarded(false);
            newPost.setEdited(false);
            newPost.setLength(editPost.getLength());
            newPost.setAllowComment(editPost.isAllowComment());
            UserEntity user = userRepository.findById(editPost.getUser().getId())
                    .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
            UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                    .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
            newPost.setUser(user);
            newPost.setParentPost(editPost);
            CategoryEntity category = categoryRepository.findById(editPost.getCategory().getId())
                    .orElseThrow(() -> new AppException("Unknown category", HttpStatus.NOT_FOUND));
            newPost.setCategory(category);
            TagEntity tag = tagRepository.findById(editPost.getTag().getId())
                    .orElseThrow(() -> new AppException("Unknown tag", HttpStatus.NOT_FOUND));
            newPost.setTag(tag);

            if (editPost.getCoverURL() != null){
                newPost.setCoverURL(editPost.getCoverURL());
            }else {
                newPost.setCoverURL(null);
            }

            //newPost.setSlug(editPost.getSlug());
            newPost.setSlug(null);

            postRepository.save(newPost);

            List<PostSkillEntity> postSkills = postSkillRepository.findByPost(editPost);
            List<String> postSkillList = new ArrayList<>();
            for (PostSkillEntity postSkill: postSkills) {
                postSkillList.add(postSkill.getSkill().getSkillName());
            }

            // thêm post Skill mới vào

            for (String postSkill: postSkillList) {
                PostSkillEntity postSkillEntity = new PostSkillEntity();
                SkillEntity skill = skillRepository.findBySkillName(postSkill)
                        .orElseThrow(()-> new AppException("Unknown skill", HttpStatus.NOT_FOUND));
                postSkillEntity.setPost(newPost);
                postSkillEntity.setSkill(skill);
                postSkillRepository.save(postSkillEntity);
            }

            postDetail(newPost.getId(), "Edit");

            //vừa edit trục tiếp trong database vừa lưu vào bài trc khi edit
            PostDto postDto = editPendingPost(editPostDto);

            if (user.getRole().getRoleName().equalsIgnoreCase("Lecturer")){
                editPostDetails.setDateOfAction(LocalDateTime.of(java.time.LocalDate.now(), java.time.LocalTime.now()));
                editPostDetails.setType("Approve");
                editPostDetails.setUser(user);
                postDetailsRepository.save(editPostDetails);
            }else {
                editPostDetails.setDateOfAction(LocalDateTime.of(java.time.LocalDate.now(), java.time.LocalTime.now()));
                editPostDetails.setType("Request");
                editPostDetails.setUser(null);
                postDetailsRepository.save(editPostDetails);
            }
            return postDto;
        }
    }

    public PostDto editPendingPost(EditPostDto editPostDto){

        PostEntity editPost = postRepository.findById(editPostDto.getPostId())
                .orElseThrow(()-> new AppException("Unknown post", HttpStatus.NOT_FOUND));

        editPost.setTitle(editPostDto.getTitle());
        if (!editPostDto.getDescription().isEmpty()){
            editPost.setDescription(editPostDto.getDescription());
        }else {
            editPost.setDescription(null);
        }
        editPost.setContent(editPostDto.getContent());
        editPost.setDateOfPost(LocalDateTime.of(java.time.LocalDate.now(), java.time.LocalTime.now()));
        editPost.setEdited(true);
        editPost.setLength(editPostDto.getLength());
        editPost.setAllowComment(editPostDto.isAllowComment());
        UserEntity user = userRepository.findById(editPost.getUser().getId())
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
        UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
        editPost.setUser(user);
        editPost.setParentPost(null);
        CategoryEntity category = categoryRepository.findById(editPostDto.getCategoryId())
                .orElseThrow(() -> new AppException("Unknown category", HttpStatus.NOT_FOUND));
        editPost.setCategory(category);
        TagEntity tag = tagRepository.findById(editPostDto.getTagId())
                .orElseThrow(() -> new AppException("Unknown tag", HttpStatus.NOT_FOUND));
        editPost.setTag(tag);

        if (editPostDto.getCoverURL() != null){
            editPost.setCoverURL(editPostDto.getCoverURL());
        }else {
            editPost.setCoverURL(null);
        }

        //editPost.setSlug(editPostDto.getSlug());

        postRepository.save(editPost);

        // xóa post skill cũ đi
        List<PostSkillEntity> postSkills = postSkillRepository.findByPost(editPost);

        if (postSkills != null) {
            for (PostSkillEntity postSkill: postSkills) {
                postSkillRepository.delete(postSkill);
            }
        }

        // thêm post Skill mới vào

        for (String postSkill: editPostDto.getPostSkill()) {
            PostSkillEntity postSkillEntity = new PostSkillEntity();
            SkillEntity skill = skillRepository.findBySkillName(postSkill)
                    .orElseThrow(()-> new AppException("Unknown skill", HttpStatus.NOT_FOUND));
            postSkillEntity.setPost(editPost);
            postSkillEntity.setSkill(skill);
            postSkillRepository.save(postSkillEntity);
        }

        List<PostSkillEntity> editPostSkills = postSkillRepository.findByPost(editPost);

        List<PostRewardEntity> postRewards = postRewardRepository.findByPost(editPost);

        List<BadgeEntity> badges = badgeServices.findBadgesByUserId(user.getId());

        return new PostDto(editPost.getId(), user.getId(),userDetails.getFullName() , userDetails.getProfileURL(),editPost.getTitle(), editPost.getDescription(),editPost.getContent(), editPost.getDateOfPost().format(formatter)
                , editPost.getNumOfUpvote(), editPost.getNumOfDownvote(), editPost.isRewarded(), editPost.isEdited()
                , editPost.isAllowComment() ,getCategoriesOfPost(getRelatedCategories(editPost.getCategory().getId())), getTagOfPost(editPost.getTag()), editPost.getCoverURL(), editPost.getSlug(),getCommentsForPost(editPost.getId()), null, getSkillOfPost(editPostSkills), getRewarderForPost(postRewards), badges, true);
    }

    public List<PostListDto> viewRewardedPost() {
        List<PostEntity> list = postRepository.getAllApprovedPost();

        list.sort(Comparator
                .comparingInt((PostEntity post) -> post.getNumOfUpvote() - post.getNumOfDownvote())
                .thenComparing(PostEntity::getDateOfPost).reversed());

        List<PostListDto> rewardedPostList = new ArrayList<>();
        for (PostEntity post : list) {

            if (post.isRewarded()) {
                UserEntity user = userRepository.findById(post.getUser().getId())
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                TagEntity tag = tagRepository.findById(post.getTag().getId())
                        .orElseThrow(() -> new AppException("Unknown tag", HttpStatus.NOT_FOUND));
                if (!tag.getTagName().equalsIgnoreCase("Q&A")){
                    PostListDto postListDto = new PostListDto(post.getId(), user.getId(),userDetails.getFullName(), userDetails.getProfileURL() ,post.getTitle(), post.getDescription() ,
                            post.getDateOfPost().format(formatter), getCategoriesOfPost(getRelatedCategories(post.getCategory().getId())), getTagOfPost(tag), post.getCoverURL() ,post.isRewarded(), post.getSlug());
                    rewardedPostList.add(postListDto);
                }
            }
        }
        return rewardedPostList;
    }

    public List<QuestionAnswerDto> viewQuestionAndAnswerPost() {
        List<PostEntity> list = postRepository.getAllApprovedPost();
        List<QuestionAnswerDto> QAPostList = new ArrayList<>();

        list.sort(Comparator
                .comparingInt((PostEntity post) -> post.getNumOfUpvote() - post.getNumOfDownvote())
                .thenComparing(PostEntity::getDateOfPost).reversed());

        for (PostEntity post : list) {

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
                            post.getDateOfPost().format(formatter),numOfUpvote,numOfDownvote ,getCategoriesOfPost(getRelatedCategories(post.getCategory().getId())), getTagOfPost(tag), post.getCoverURL(), post.isRewarded(), post.getSlug(), commentRepository.countNumOfCommentForPost(post.getId()));
                    QAPostList.add(questionAnswerDto);
                }
        }
        return QAPostList;
    }

    public List<PostListDto> viewByLatestPost(){
        List<PostEntity> postList = postRepository.getAllApprovedPost();
        List<PostListDto> latestPost = new ArrayList<>();

        postList.sort(Comparator
                .comparing(PostEntity::getDateOfPost).reversed());

        for (PostEntity post: postList) {

                UserEntity user = userRepository.findById(post.getUser().getId())
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                TagEntity tag = tagRepository.findById(post.getTag().getId())
                        .orElseThrow(() -> new AppException("Unknown tag", HttpStatus.NOT_FOUND));
                if (!tag.getTagName().equalsIgnoreCase("Q&A")) {
                    PostListDto postListDto = new PostListDto(post.getId(), user.getId() ,userDetails.getFullName(), userDetails.getProfileURL() ,post.getTitle(), post.getDescription(),
                            post.getDateOfPost().format(formatter), getCategoriesOfPost(getRelatedCategories(post.getCategory().getId())), getTagOfPost(tag), post.getCoverURL(),post.isRewarded(), post.getSlug());
                    latestPost.add(postListDto);
                    //latestPost.sort(Comparator.comparing(PostListDto::getDateOfPost).reversed());
                }
        }
        return latestPost;
    }

    //View trending post
    public List<PostListTrendingDto> viewTrending(){
        List<PostEntity> postList = postRepository.findPostForLast7Days();
        List<PostListTrendingDto> trendingPost = new ArrayList<>();
        for (PostEntity post: postList) {
            if(isApprove(post.getId())) {
                UserEntity user = userRepository.findById(post.getUser().getId())
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                TagEntity tag = tagRepository.findById(post.getTag().getId())
                        .orElseThrow(() -> new AppException("Unknown tag", HttpStatus.NOT_FOUND));
                if (!tag.getTagName().equalsIgnoreCase("Q&A")) {
                    PostListTrendingDto postListTrendingDto = new PostListTrendingDto(post.getId(), user.getId() ,userDetails.getFullName(), userDetails.getProfileURL() ,post.getTitle(), post.getDescription(),
                            post.getDateOfPost().format(formatter), getCategoriesOfPost(getRelatedCategories(post.getCategory().getId())), getTagOfPost(tag), post.getCoverURL(),post.isRewarded(), post.getNumOfUpvote(), post.getNumOfDownvote(), post.getSlug());
                    trendingPost.add(postListTrendingDto);
                    trendingPost.sort(Comparator.comparingInt(
                            trendingPosts -> trendingPosts.getNumOfUpVote() - trendingPosts.getNumOfDownVote()));
                    Collections.reverse(trendingPost);
                }
            }
        }
        return trendingPost;
    }

    public List<PostListDto> viewShort(){
        List<PostEntity> postList = postRepository.getAllApprovedPost();
        List<PostListDto> shortPost = new ArrayList<>();

        postList.sort(Comparator
                .comparingInt((PostEntity post) -> post.getNumOfUpvote() - post.getNumOfDownvote())
                .thenComparing(PostEntity::getDateOfPost).reversed());

        for (PostEntity post: postList) {
            if(post.getLength() <= 300) {
                UserEntity user = userRepository.findById(post.getUser().getId())
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                TagEntity tag = tagRepository.findById(post.getTag().getId())
                        .orElseThrow(() -> new AppException("Unknown tag", HttpStatus.NOT_FOUND));
                if (!tag.getTagName().equalsIgnoreCase("Q&A")) {
                    PostListDto postListDto = new PostListDto(post.getId(), user.getId() ,userDetails.getFullName(), userDetails.getProfileURL() ,post.getTitle(), post.getDescription(),
                            post.getDateOfPost().format(formatter), getCategoriesOfPost(getRelatedCategories(post.getCategory().getId())), getTagOfPost(tag), post.getCoverURL(),post.isRewarded(), post.getSlug());
                    shortPost.add(postListDto);
                }
            }
        }
        return shortPost;
    }

    public List<PostListDto> filterPosts(Integer categoryId, Integer tagId, String title) {
        List<PostEntity> postList;
        List<PostListDto> filterPost = new ArrayList<>();

        if (categoryId == null && tagId == null && title.isEmpty()){
            return  filterPost;
        }

        if (categoryId != null) {
            // Check if the categoryId has a parent ID
            CategoryEntity category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new AppException("Unknown Category", HttpStatus.NOT_FOUND));

            if (category.getParentID() == null && category.getCategoryType().equalsIgnoreCase("Specialization")) {

                postList = postRepository.findPostsByCategoryIdsAndTagIdAndTitle(categoryId, tagId, title);
            } else {
                // If it's not a specialization category, just fetch posts for the specified category
                postList = postRepository.findPostsByCategoryIdAndTagIdAndTitle(categoryId, tagId, title);
            }
        } else {
            // If categoryId is null, fetch posts based on tagId and title
            postList = postRepository.findPostsByCategoryIdAndTagIdAndTitle(null, tagId, title);
        }

        postList.sort(Comparator
                .comparingInt((PostEntity post) -> post.getNumOfUpvote() - post.getNumOfDownvote())
                .thenComparing(PostEntity::getDateOfPost).reversed());

        for (PostEntity post : postList) {
            UserEntity user = userRepository.findById(post.getUser().getId())
                    .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));

            UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                    .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));

            TagEntity tag = tagRepository.findById(post.getTag().getId())
                    .orElseThrow(() -> new AppException("Unknown tag", HttpStatus.NOT_FOUND));

            if (!tag.getTagName().equalsIgnoreCase("Q&A")) {
                PostListDto postListDto = new PostListDto(post.getId(),  user.getId(), userDetails.getFullName(), userDetails.getProfileURL(), post.getTitle(), post.getDescription(),
                        post.getDateOfPost().format(formatter), getCategoriesOfPost(getRelatedCategories(post.getCategory().getId())),
                        getTagOfPost(tag), post.getCoverURL(), post.isRewarded(), post.getSlug());
                filterPost.add(postListDto);
            }
        }
        return filterPost;
    }

    public List<PostListDto> filterPostsBySkill(String skill) {
        List<PostEntity> postList;
        List<PostListDto> filterPost = new ArrayList<>();

        SkillEntity skillEntity = skillRepository.findBySkillName(skill)
                        .orElseThrow(()-> new AppException("Unknown skill", HttpStatus.NOT_FOUND));
        
        postList = postRepository.findPostBySkill(skillEntity.getId());

        postList.sort(Comparator
                .comparingInt((PostEntity post) -> post.getNumOfUpvote() - post.getNumOfDownvote())
                .thenComparing(PostEntity::getDateOfPost).reversed());

        for (PostEntity post : postList) {
            UserEntity user = userRepository.findById(post.getUser().getId())
                    .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));

            UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                    .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));

            TagEntity tag = tagRepository.findById(post.getTag().getId())
                    .orElseThrow(() -> new AppException("Unknown tag", HttpStatus.NOT_FOUND));

            if (!tag.getTagName().equalsIgnoreCase("Q&A")) {
                PostListDto postListDto = new PostListDto(post.getId(),  user.getId(), userDetails.getFullName(), userDetails.getProfileURL(), post.getTitle(), post.getDescription(),
                        post.getDateOfPost().format(formatter), getCategoriesOfPost(getRelatedCategories(post.getCategory().getId())),
                        getTagOfPost(tag), post.getCoverURL(), post.isRewarded(), post.getSlug());
                filterPost.add(postListDto);
            }
        }
        return filterPost;
    }



    public List<QuestionAnswerDto> filterQA(Integer categoryId, Integer tagId, String title) {
        List<PostEntity> postList;
        List<QuestionAnswerDto> filterQA = new ArrayList<>();

        if (categoryId == null && tagId == null && title.isEmpty()){
            return filterQA;
        }

        if (categoryId != null) {
            // Check if the categoryId has a parent ID
            CategoryEntity category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new AppException("Unknown Category", HttpStatus.NOT_FOUND));

            if (category.getParentID() == null && category.getCategoryType().equalsIgnoreCase("Specialization")) {

                postList = postRepository.findPostsByCategoryIdsAndTagIdAndTitle(categoryId, tagId, title);
            } else {
                // If it's not a specialization category, just fetch posts for the specified category
                postList = postRepository.findPostsByCategoryIdAndTagIdAndTitle(categoryId, tagId, title);
            }
        } else {
            // If categoryId is null, fetch posts based on tagId and title
            postList = postRepository.findPostsByCategoryIdAndTagIdAndTitle(null, tagId, title);
        }

        postList.sort(Comparator
                .comparingInt((PostEntity post) -> post.getNumOfUpvote() - post.getNumOfDownvote())
                .thenComparing(PostEntity::getDateOfPost).reversed());

        for (PostEntity post : postList) {
            UserEntity user = userRepository.findById(post.getUser().getId())
                    .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));

            UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                    .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));

            TagEntity tag = tagRepository.findById(post.getTag().getId())
                    .orElseThrow(() -> new AppException("Unknown tag", HttpStatus.NOT_FOUND));

            if (tag.getTagName().equalsIgnoreCase("Q&A")) {
                QuestionAnswerDto questionAnswerDto = new QuestionAnswerDto(post.getId(),  user.getId(), userDetails.getFullName(), userDetails.getProfileURL(), post.getTitle(), post.getDescription(),
                       post.getContent() ,post.getDateOfPost().format(formatter),post.getNumOfUpvote(), post.getNumOfDownvote(),getCategoriesOfPost(getRelatedCategories(post.getCategory().getId())),
                        getTagOfPost(tag), post.getCoverURL(), post.isRewarded(), post.getSlug(), commentRepository.countNumOfCommentForPost(post.getId()));
                filterQA.add(questionAnswerDto);
            }
        }
        return filterQA;
    }

    // View edit post history
    public List<PostDto> viewPostEditHistory(int postId) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException("Unknown post", HttpStatus.NOT_FOUND));
        List<PostDto> postEditHistoryList = new ArrayList<>();

        findByParentPostId(post, postEditHistoryList);

        postEditHistoryList.sort(Comparator.comparing(PostDto::getDateOfPost).reversed());

        return postEditHistoryList;
    }

    private void findByParentPostId(PostEntity postEntity, List<PostDto> postEditHistoryList) {
        // Find and add the parent post (if it exists)
        List<PostEntity> childPosts = postRepository.findPostsByParentPost(postEntity);
        //PostEntity parentPost = postEntity.getParentPost();
        for (PostEntity childPost: childPosts) {
            if (isEdit(childPost.getId())){
                addPostToList(childPost, postEditHistoryList);
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

        PostDetailsEntity postDetails = postDetailsRepository.findByPostId(postEntity.getId())
                        .orElseThrow(()-> new AppException("Unknown post details", HttpStatus.NOT_FOUND));

        String reasonOfDecline = (postDetails.getReasonOfDeclination() != null) ? postDetails.getReasonOfDeclination() : null;

        List<PostSkillEntity> postSkills = postSkillRepository.findByPost(postEntity);

        postEditHistoryList.add(new PostDto(postEntity.getId(),user.getId() ,userDetails.getFullName(), userDetails.getProfileURL() , postEntity.getTitle(), postEntity.getDescription() , postEntity.getContent(),
                postEntity.getDateOfPost().format(formatter), postEntity.getNumOfUpvote(), postEntity.getNumOfDownvote(), postEntity.isRewarded(), postEntity.isEdited(), postEntity.isAllowComment(),
                getCategoriesOfPost(getRelatedCategories(postEntity.getCategory().getId())), getTagOfPost(tag), postEntity.getCoverURL(), postEntity.getSlug(), getCommentsForPost(postEntity.getId()), reasonOfDecline, getSkillOfPost(postSkills), null, null, false));
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

    public boolean isEdit(int id){
        PostDetailsEntity post = postDetailsRepository.findByPostId(id)
                .orElseThrow(() -> new AppException("Unknown post", HttpStatus.NOT_FOUND));
        if (post.getType().equalsIgnoreCase("Edit")) {
            return true;
        }
        return false;
    }

    public boolean isDraft(int id) {
        PostDetailsEntity post = postDetailsRepository.findByPostId(id)
                .orElseThrow(() -> new AppException("Unknown post", HttpStatus.NOT_FOUND));
        if (post.getType().equalsIgnoreCase("Draft")) {
            return true;
        }
        return false;
    }

    public boolean isDeclined(int id) {
        PostDetailsEntity post = postDetailsRepository.findByPostId(id)
                .orElseThrow(() -> new AppException("Unknown post", HttpStatus.NOT_FOUND));
        if (post.getType().equalsIgnoreCase("Decline")) {
            return true;
        }
        return false;
    }

    public  Map<String, List<?>> viewDraft(int userId) {
        List<PostEntity> list = postRepository.findAll();
        List<PostListDto> draftList = new ArrayList<>();
        List<PostListDeclineDto> declineList = new ArrayList<>();
        Map<String, List<?>> result = new HashMap<>();

        list.sort(Comparator
                .comparing(PostEntity::getDateOfPost).reversed());

        for (PostEntity post : list) {

            if (((isDraft(post.getId()) || isDeclined(post.getId())) && post.getUser().getId() == userId)) {
                UserEntity user = userRepository.findById(post.getUser().getId())
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                TagEntity tag = tagRepository.findById(post.getTag().getId())
                        .orElseThrow(() -> new AppException("Unknown tag", HttpStatus.NOT_FOUND));
                PostDetailsEntity postDetails = postDetailsRepository.findByPostId(post.getId())
                        .orElseThrow(() -> new AppException("Unknown post", HttpStatus.NOT_FOUND));
                if (!(tag.getTagName().equalsIgnoreCase("Q&A") && isDeclined(post.getId()))){
                    PostListDto postListDto = new PostListDto(post.getId(), user.getId(),userDetails.getFullName(), userDetails.getProfileURL(),post.getTitle(), post.getDescription(),
                            post.getDateOfPost().format(formatter), getCategoriesOfPost(getRelatedCategories(post.getCategory().getId())), getTagOfPost(tag), post.getCoverURL() ,post.isRewarded(), post.getSlug());

                    PostListDeclineDto postListDeclineDto = new PostListDeclineDto(post.getId(), user.getId(),userDetails.getFullName(), userDetails.getProfileURL(),post.getTitle(), post.getDescription(),
                            post.getDateOfPost().format(formatter), getCategoriesOfPost(getRelatedCategories(post.getCategory().getId())), getTagOfPost(tag), post.getCoverURL() ,post.isRewarded(), postDetails.getReasonOfDeclination(), post.getSlug());

                        if (isDraft(post.getId())){
                            draftList.add(postListDto);
                        }else if (isDeclined(post.getId())){
                            declineList.add(postListDeclineDto);
                        }
                }
            }
        }
        result.put("DraftList", draftList);
        result.put("DeclinePostList", declineList);
        return result;
    }

    public PostDto editDraft(EditPostDto editPostDto){
        PostEntity draft = postRepository.findById(editPostDto.getPostId())
                .orElseThrow(()-> new AppException("Unknown post", HttpStatus.NOT_FOUND));

        draft.setTitle(editPostDto.getTitle());
        if (!editPostDto.getDescription().isEmpty()){
            draft.setDescription(editPostDto.getDescription());
        }else {
            draft.setDescription(null);
        }
        draft.setContent(editPostDto.getContent());
        draft.setDateOfPost(LocalDateTime.of(java.time.LocalDate.now(), java.time.LocalTime.now()));
        draft.setLength(editPostDto.getLength());
        draft.setAllowComment(editPostDto.isAllowComment());
        UserEntity user = userRepository.findById(draft.getUser().getId())
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
        UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
        draft.setUser(user);
        draft.setParentPost(null);
        CategoryEntity category = categoryRepository.findById(editPostDto.getCategoryId())
                .orElseThrow(() -> new AppException("Unknown category", HttpStatus.NOT_FOUND));
        draft.setCategory(category);
        TagEntity tag = tagRepository.findById(editPostDto.getTagId())
                .orElseThrow(() -> new AppException("Unknown tag", HttpStatus.NOT_FOUND));
        draft.setTag(tag);

        if (editPostDto.getCoverURL() != null){
            draft.setCoverURL(editPostDto.getCoverURL());
        }else {
            draft.setCoverURL(null);
        }

        //draft.setSlug(editPostDto.getSlug());

        postRepository.save(draft);

        List<PostSkillEntity> postSkills = postSkillRepository.findByPost(draft);

        if (postSkills != null) {
            for (PostSkillEntity postSkill: postSkills) {
                postSkillRepository.delete(postSkill);
            }
        }

        // thêm post Skill mới vào

        for (String postSkill: editPostDto.getPostSkill()) {
            PostSkillEntity postSkillEntity = new PostSkillEntity();
            SkillEntity skill = skillRepository.findBySkillName(postSkill)
                    .orElseThrow(()-> new AppException("Unknown skill", HttpStatus.NOT_FOUND));
            postSkillEntity.setPost(draft);
            postSkillEntity.setSkill(skill);
            postSkillRepository.save(postSkillEntity);
        }

        List<PostSkillEntity> editDraftSkill = postSkillRepository.findByPost(draft);

        List<PostRewardEntity> draftReward = postRewardRepository.findByPost(draft);

        List<BadgeEntity> badges = badgeServices.findBadgesByUserId(user.getId());

        return new PostDto(draft.getId(), user.getId(),userDetails.getFullName() , userDetails.getProfileURL(),draft.getTitle(), draft.getDescription(),draft.getContent(), draft.getDateOfPost().format(formatter)
                , draft.getNumOfUpvote(), draft.getNumOfDownvote(), draft.isRewarded(), draft.isEdited()
                , draft.isAllowComment() ,getCategoriesOfPost(getRelatedCategories(draft.getCategory().getId())), getTagOfPost(draft.getTag()), draft.getCoverURL(), draft.getSlug(),getCommentsForPost(draft.getId()), null, getSkillOfPost(editDraftSkill), getRewarderForPost(draftReward), badges, false);
    }

    public void sendDraft(int postId){

        PostEntity draft = postRepository.findById(postId)
                .orElseThrow(()-> new AppException("Unknown post", HttpStatus.NOT_FOUND));

        draft.setDateOfPost(LocalDateTime.of(java.time.LocalDate.now(), java.time.LocalTime.now()));

        postRepository.save(draft);

        PostDetailsEntity postDetails = postDetailsRepository.findByPostId(postId)
                .orElseThrow(() -> new AppException("Unknown post", HttpStatus.NOT_FOUND));

        String reasonOfDecline = (postDetails.getReasonOfDeclination() != null) ? postDetails.getReasonOfDeclination() : null;

        postDetails.setType("Request");
        postDetails.setReasonOfDeclination(reasonOfDecline);
        postDetails.setDateOfAction(LocalDateTime.of(java.time.LocalDate.now(), java.time.LocalTime.now()));
        postDetails.setUser(null);
        postDetailsRepository.save(postDetails);
    }

    public void sendDraftLecturer(int postId, UserEntity user){

        PostEntity draft = postRepository.findById(postId)
                .orElseThrow(()-> new AppException("Unknown post", HttpStatus.NOT_FOUND));

        draft.setDateOfPost(LocalDateTime.of(java.time.LocalDate.now(), java.time.LocalTime.now()));

        postRepository.save(draft);

        PostDetailsEntity postDetails = postDetailsRepository.findByPostId(postId)
                .orElseThrow(() -> new AppException("Unknown post", HttpStatus.NOT_FOUND));

        postDetails.setDateOfAction(LocalDateTime.of(java.time.LocalDate.now(), java.time.LocalTime.now()));
        postDetails.setType("Approve");
        postDetails.setUser(user);

        postDetailsRepository.save(postDetails);
    }

    public List<PostListDto> viewFollowedPost(int userId) {
        List<FollowerEntity> followedAccount = followerRepository.findByFollowedBy(userId);

        List<PostEntity> posts = postRepository.getAllApprovedPost();

        posts.sort(Comparator
                .comparingInt((PostEntity post) -> post.getNumOfUpvote() - post.getNumOfDownvote())
                .thenComparing(PostEntity::getDateOfPost).reversed());

        List<PostListDto> followedPost = new ArrayList<>();

        for (PostEntity post: posts) {
            for (FollowerEntity following: followedAccount) {
                if (post.getUser().getId() == following.getUser().getId()){

                    UserEntity user = userRepository.findById(post.getUser().getId())
                            .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                    UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                            .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                    TagEntity tag = tagRepository.findById(post.getTag().getId())
                            .orElseThrow(() -> new AppException("Unknown tag", HttpStatus.NOT_FOUND));

                    if (!tag.getTagName().equalsIgnoreCase("Q&A")){
                        PostListDto postListDto = new PostListDto(post.getId(), user.getId(),userDetails.getFullName(), userDetails.getProfileURL(),post.getTitle(), post.getDescription(),
                                post.getDateOfPost().format(formatter), getCategoriesOfPost(getRelatedCategories(post.getCategory().getId())), getTagOfPost(tag), post.getCoverURL() ,post.isRewarded(), post.getSlug());
                        followedPost.add(postListDto);
                    }
                }
            }
        }
        return followedPost;
    }

    public List<QuestionAnswerDto> viewFollowedQA(int userId) {
        List<FollowerEntity> followedAccount = followerRepository.findByFollowedBy(userId);

        List<PostEntity> posts = postRepository.getAllApprovedPost();

        posts.sort(Comparator
                .comparingInt((PostEntity post) -> post.getNumOfUpvote() - post.getNumOfDownvote())
                .thenComparing(PostEntity::getDateOfPost).reversed());

        List<QuestionAnswerDto> followedQA = new ArrayList<>();

        for (PostEntity post: posts) {
            for (FollowerEntity following: followedAccount) {
                if (post.getUser().getId() == following.getUser().getId()){

                    UserEntity user = userRepository.findById(post.getUser().getId())
                            .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                    UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                            .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                    TagEntity tag = tagRepository.findById(post.getTag().getId())
                            .orElseThrow(() -> new AppException("Unknown tag", HttpStatus.NOT_FOUND));

                    if (tag.getTagName().equalsIgnoreCase("Q&A")){
                        QuestionAnswerDto questionAnswerDto = new QuestionAnswerDto(post.getId(), user.getId(),userDetails.getFullName(), userDetails.getProfileURL(),post.getTitle(), post.getDescription(), post.getContent(),
                                post.getDateOfPost().format(formatter), post.getNumOfUpvote(), post.getNumOfDownvote()
                                ,getCategoriesOfPost(getRelatedCategories(post.getCategory().getId())), getTagOfPost(tag), post.getCoverURL() ,post.isRewarded(), post.getSlug(), commentRepository.countNumOfCommentForPost(post.getId()));
                        followedQA.add(questionAnswerDto);
                    }
                }
            }
        }
        return followedQA;
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

    public Map<String, List<PostListDto>> viewPendingPost(UserEntity userEntity){
        List<PostEntity> postList = postRepository.getAllPendingPost();

        Map<String, List<PostListDto>> result = new HashMap<>();
        List<PostListDto> pendingPostList = new ArrayList<>();
        List<PostListDto> pendingPostListWithReward = new ArrayList<>();

        UserDetailsEntity userDetailsEntity = userDetailsRepository.findByUserAccount(userEntity)
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));

        List<Integer> rewardedPostIds = postRewardRepository.findPostIdByUserIdAndStatus(userEntity.getId(), "Pending");
//        System.out.println("Rewarded Post IDs: " + rewardedPostIds);

        List<Integer> acceptedPostIds = postRewardRepository.findPostIdByUserIdAndStatus(userEntity.getId(), "Accepted");
//        System.out.println("Accepted Post IDs: " + acceptedPostIds);

        List<Integer> dismissPostIds = postRewardRepository.findPostIdByUserIdAndStatus(userEntity.getId(), "Dismiss");
        //System.out.println("Dismiss Post IDs: " + dismissPostIds);


        for (PostEntity post: postList) {

                List<Integer> rewardPostEntity = postRewardRepository.findByPostId(post.getId());

                UserEntity user = userRepository.findById(post.getUser().getId())
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                TagEntity tag = tagRepository.findById(post.getTag().getId())
                        .orElseThrow(() -> new AppException("Unknown tag", HttpStatus.NOT_FOUND));

                List<PostSkillEntity> postSkills = postSkillRepository.findByPost(post);

                List<UserSkillEntity> userSkills = userSkillRepository.findByUser(userEntity);

              boolean hasCommonSkills = postSkills.stream()
                    .map(PostSkillEntity::getSkill)
                    .allMatch(skill -> userSkills.stream().anyMatch(userSkill -> userSkill.getSkill().equals(skill)));

                if (dismissPostIds.contains(post.getId())) {
                    continue;
                }

                if (hasCommonSkills && !rewardedPostIds.contains(post.getId()) && !acceptedPostIds.contains(post.getId()) && !rewardPostEntity.contains(post.getId())) {
                    if (!tag.getTagName().equalsIgnoreCase("Q&A")) {
                        PostListDto postListDto = new PostListDto(post.getId(), user.getId(),userDetails.getFullName(), userDetails.getProfileURL() ,post.getTitle(), post.getDescription(),
                                post.getDateOfPost().format(formatter), getCategoriesOfPost(getRelatedCategories(post.getCategory().getId())), getTagOfPost(tag), post.getCoverURL(),post.isRewarded(), post.getSlug());
                        pendingPostList.add(postListDto);
                    }
                }

                if ((rewardedPostIds.contains(post.getId()) || acceptedPostIds.contains(post.getId()))) {
                    if (!tag.getTagName().equalsIgnoreCase("Q&A")) {
                        PostListDto postListDto = new PostListDto(post.getId(), user.getId(),userDetails.getFullName(), userDetails.getProfileURL() ,post.getTitle(), post.getDescription(),
                                post.getDateOfPost().format(formatter), getCategoriesOfPost(getRelatedCategories(post.getCategory().getId())), getTagOfPost(tag), post.getCoverURL(),post.isRewarded(), post.getSlug());
                        pendingPostListWithReward.add(postListDto);
                    }
                }
        }
        pendingPostList.sort(Comparator.comparing(PostListDto::getDateOfPost).reversed());

        result.put("PendingPost", pendingPostList);
        result.put("PendingRewardedPost", pendingPostListWithReward);

        return result;
    }

    public List<PostListDto> viewApprovedPost(UserEntity userEntity) {
        List<PostEntity> list = postRepository.getAllApprovedPost();
        List<PostListDto> approvePostList = new ArrayList<>();

        list.sort(Comparator
                .comparingInt((PostEntity post) -> post.getNumOfUpvote() - post.getNumOfDownvote())
                .thenComparing(PostEntity::getDateOfPost).reversed());

        UserDetailsEntity userDetailsEntity = userDetailsRepository.findByUserAccount(userEntity)
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
        for (PostEntity post : list) {

                UserEntity user = userRepository.findById(post.getUser().getId())
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                TagEntity tag = tagRepository.findById(post.getTag().getId())
                        .orElseThrow(() -> new AppException("Unknown tag", HttpStatus.NOT_FOUND));

                    if (!tag.getTagName().equalsIgnoreCase("Q&A")){
                        PostListDto postListDto = new PostListDto(post.getId(),user.getId(),userDetails.getFullName(), userDetails.getProfileURL(),post.getTitle(), post.getDescription(),
                                post.getDateOfPost().format(formatter), getCategoriesOfPost(getRelatedCategories(post.getCategory().getId())), getTagOfPost(tag), post.getCoverURL() ,post.isRewarded(), post.getSlug());
                        approvePostList.add(postListDto);
                    }
        }
        //System.out.println(approvePostList.size());
        return approvePostList;
    }

    public void approvePost(int postId, UserEntity user){
        PostDetailsEntity postDetails = postDetailsRepository.findByPostId(postId)
                .orElseThrow(() -> new AppException("Unknown post", HttpStatus.NOT_FOUND));

        postDetails.setType("Approve");
        postDetails.setDateOfAction(LocalDateTime.of(java.time.LocalDate.now(), java.time.LocalTime.now()));
        postDetails.setUser(user);
        postDetails.setReasonOfDeclination(null);
        postDetailsRepository.save(postDetails);

        PostEntity postEntity = postRepository.findById(postId)
                .orElseThrow(() -> new AppException("Unknown post", HttpStatus.NOT_FOUND));
        postEntity.setDateOfPost(postDetails.getDateOfAction());
        postRepository.save(postEntity);

        //sent mail notify
//        MailStructureDto mail = new MailStructureDto();
//        mail.setTriggerId(user.getId());
//        mail.setReceiverId(postEntity.getUser().getId());
//        mail.setMailType("Approve-post");
//        mail.setPostLink("https://fblog.site/view/"+postEntity.getSlug());
//        notifyByMailServices.sendMail(mail);

    }

    public void declinePost(int postId, String reasonOfDecline, UserEntity user){

        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException("Unknown post", HttpStatus.NOT_FOUND));

        PostDetailsEntity postDetails = postDetailsRepository.findByPostId(postId)
                .orElseThrow(() -> new AppException("Unknown post details", HttpStatus.NOT_FOUND));

        postDetails.setType("Decline");
        postDetails.setReasonOfDeclination(reasonOfDecline);
        postDetails.setDateOfAction(LocalDateTime.of(java.time.LocalDate.now(), java.time.LocalTime.now()));
        postDetails.setUser(user);
        postDetailsRepository.save(postDetails);
        //send mail
//        MailStructureDto mail = new MailStructureDto();
//        mail.setTriggerId(user.getId());
//        mail.setReceiverId(postDetails.getUser().getId());
//        mail.setMailType("Decline-post");
//        mail.setPostLink(postDetails.getReasonOfDeclination());
//        notifyByMailServices.sendMail(mail);
    }

    //
    public List<PostListDto> getPendingRewardPost(UserEntity userEntity){
        List<PostEntity> posts = postRepository.getAllApprovedPost();
        List<PostListDto> rewardedPostList = new ArrayList<>();
        UserDetailsEntity userDetailsEntity = userDetailsRepository.findByUserAccount(userEntity)
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));

        // Get the posts that the user has already rewarded
        List<Integer> rewardedPostIds = postRewardRepository.findPostIdByUserIdAndStatus(userEntity.getId(), "Pending");
        //System.out.println("Rewarded Post IDs: " + rewardedPostIds);

        // Get the posts that the user has dismissed from rewarding
        List<Integer> dismissedPostIds = postRewardRepository.findPostIdByUserIdAndStatus(userEntity.getId(), "Dismiss");
        //System.out.println("Dismissed Post IDs: " + dismissedPostIds);
        for (PostEntity post: posts) {

            if (postRewardRepository.countNumOfReward(post.getId()) == 1) {
                if (!rewardedPostIds.contains(post.getId()) && !dismissedPostIds.contains(post.getId())){
                    UserEntity user = userRepository.findById(post.getUser().getId())
                            .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                    UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                            .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                    TagEntity tag = tagRepository.findById(post.getTag().getId())
                            .orElseThrow(() -> new AppException("Unknown tag", HttpStatus.NOT_FOUND));
                    PostDetailsEntity postDetails = postDetailsRepository.findByPostId(post.getId())
                            .orElseThrow(() -> new AppException("Unknown post", HttpStatus.NOT_FOUND));

                    List<PostSkillEntity> postSkills = postSkillRepository.findByPost(post);

                    List<UserSkillEntity> userSkills = userSkillRepository.findByUser(userEntity);

                    boolean hasCommonSkills = postSkills.stream()
                            .map(PostSkillEntity::getSkill)
                            .allMatch(skill -> userSkills.stream().anyMatch(userSkill -> userSkill.getSkill().equals(skill)));

                    if (hasCommonSkills && post.getUser().getId() != userEntity.getId()){
                        if (!tag.getTagName().equalsIgnoreCase("Q&A")){
                            rewardedPostList.add(new PostListDto(post.getId(), user.getId(),userDetails.getFullName(), userDetails.getProfileURL(),post.getTitle(), post.getDescription(),
                                    post.getDateOfPost().format(formatter), getCategoriesOfPost(getRelatedCategories(post.getCategory().getId())), getTagOfPost(tag), post.getCoverURL() ,post.isRewarded(), post.getSlug()));
                        }
                    }
                }
            }
        }
        return rewardedPostList;
    }

    // give reward
    public void giveReward(int postId, UserEntity user){
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(()-> new AppException("Unknown post", HttpStatus.NOT_FOUND));

        if (!isApprove(post.getId()) && !isPending(post.getId())) {
            throw new AppException("Invalid postId", HttpStatus.NOT_FOUND);
        }

        if (post.isRewarded()){
            PostRewardEntity postReward = new PostRewardEntity();
            postReward.setPost(post);
            postReward.setUser(user);
            postReward.setStatus("Accepted");
            postRewardRepository.save(postReward);
        }else {
            PostRewardEntity postReward = new PostRewardEntity();
            postReward.setPost(post);
            postReward.setUser(user);
            postReward.setStatus("Pending");
            postRewardRepository.save(postReward);

            int countNumOfReward = postRewardRepository.countNumOfReward(postId);
            if (countNumOfReward >= 2){
                List<PostRewardEntity> postRewardList = postRewardRepository.findByPostAndStatus(post, "Pending");
                for (PostRewardEntity postRewardEntity : postRewardList) {
                    postRewardEntity.setStatus("Accepted");
                    postRewardRepository.save(postRewardEntity);
                }
                post.setRewarded(true);
                postRepository.save(post);
            }
        }
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

    public void dismissReward(int postId, UserEntity user){
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(()-> new AppException("Unknown post", HttpStatus.NOT_FOUND));

        if (!isApprove(post.getId()) && !isPending(post.getId())) {
            throw new AppException("Invalid postId", HttpStatus.NOT_FOUND);
        }

        PostRewardEntity postReward = new PostRewardEntity();
        postReward.setPost(post);
        postReward.setUser(user);
        postReward.setStatus("Dismiss");
        postRewardRepository.save(postReward);
    }

    // view Q&A pending list
    public List<QuestionAnswerDto> viewQAPendingPost(UserEntity userEntity){
        List<PostEntity> postList = postRepository.getAllPendingPost();
        List<QuestionAnswerDto> QApendingPostList = new ArrayList<>();

        for (PostEntity post: postList) {

                UserEntity user = userRepository.findById(post.getUser().getId())
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                TagEntity tag = tagRepository.findById(post.getTag().getId())
                        .orElseThrow(() -> new AppException("Unknown tag", HttpStatus.NOT_FOUND));

                   if (post.getUser().getId() != userEntity.getId()){
                       if (tag.getTagName().equalsIgnoreCase("Q&A")) {
                           QuestionAnswerDto questionAnswerDto = new QuestionAnswerDto(post.getId(), user.getId() ,userDetails.getFullName(),userDetails.getProfileURL(),post.getTitle(), post.getDescription() ,post.getContent(),
                                   post.getDateOfPost().format(formatter), post.getNumOfUpvote(), post.getNumOfDownvote() ,getCategoriesOfPost(getRelatedCategories(post.getCategory().getId())), getTagOfPost(tag), post.getCoverURL(), post.isRewarded(), post.getSlug(), commentRepository.countNumOfCommentForPost(post.getId()));
                           QApendingPostList.add(questionAnswerDto);
                       }
                   }
        }
        return QApendingPostList;
    }

    public List<QuestionAnswerDto> viewQAApprovedPost(){
        List<PostEntity> postList = postRepository.getAllApprovedPost();
        List<QuestionAnswerDto> QAapprovedPostList = new ArrayList<>();

        postList.sort(Comparator
                .comparingInt((PostEntity post) -> post.getNumOfUpvote() - post.getNumOfDownvote())
                .thenComparing(PostEntity::getDateOfPost).reversed());

        for (PostEntity post: postList) {

                UserEntity user = userRepository.findById(post.getUser().getId())
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                TagEntity tag = tagRepository.findById(post.getTag().getId())
                        .orElseThrow(() -> new AppException("Unknown tag", HttpStatus.NOT_FOUND));

                if (tag.getTagName().equalsIgnoreCase("Q&A")) {
                    QuestionAnswerDto questionAnswerDto = new QuestionAnswerDto(post.getId(), user.getId() ,userDetails.getFullName(),userDetails.getProfileURL(),post.getTitle(), post.getDescription() ,post.getContent(),
                            post.getDateOfPost().format(formatter), post.getNumOfUpvote(), post.getNumOfDownvote() ,getCategoriesOfPost(getRelatedCategories(post.getCategory().getId())), getTagOfPost(tag), post.getCoverURL(), post.isRewarded(), post.getSlug(), commentRepository.countNumOfCommentForPost(post.getId()));
                    QAapprovedPostList.add(questionAnswerDto);
                }
        }
        return QAapprovedPostList;
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

            //sent mail notify
//            MailStructureDto mail = new MailStructureDto();
//            mail.setTriggerId(user.getId());
//            mail.setReceiverId(postDetails.getUser().getId());
//            mail.setMailType("Approve-Q&A");
//            mail.setPostLink("https://fblog.site/view/"+postDetails.getPost().getSlug());
//            notifyByMailServices.sendMail(mail);

        }else  {
            throw new AppException("This postId does not belong to Q&A tag", HttpStatus.NOT_FOUND);
        }
    }

    public void declineQAPost(int postId, String reasonOfDecline, UserEntity user){

        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException("Unknown post", HttpStatus.NOT_FOUND));

        PostDetailsEntity postDetails = postDetailsRepository.findByPostId(postId)
                .orElseThrow(() -> new AppException("Unknown post", HttpStatus.NOT_FOUND));

        if (post.getTag().getTagName().equalsIgnoreCase("Q&A")) {
        postDetails.setType("Decline");
        postDetails.setReasonOfDeclination(reasonOfDecline);
            postDetails.setDateOfAction(LocalDateTime.of(java.time.LocalDate.now(), java.time.LocalTime.now()));
        postDetails.setUser(user);
        postDetailsRepository.save(postDetails);
            //send mail
//            MailStructureDto mail = new MailStructureDto();
//            mail.setTriggerId(user.getId());
//            mail.setReceiverId(postDetails.getUser().getId());
//            mail.setMailType("Decline-Q&A");
//            mail.setPostLink(postDetails.getReasonOfDeclination());
//            notifyByMailServices.sendMail(mail);
        }else  {
            throw new AppException("This postId does not belong to Q&A tag", HttpStatus.NOT_FOUND);
        }
    }

    public SearchMultipleResultDto searchMultiple(SearchMultipleDto searchMultipleDto){

        List<String> listOfTagsAndSkillsAndCategories = searchMultipleDto.getListTagsAndCategories();
        List<Integer> tagList = new ArrayList<>();
        List<Integer> categoryList = new ArrayList<>();
        List<Integer> skillList = new ArrayList<>();
        List<PostEntity> postsRaw = new ArrayList<>();
        //filter the tag in the list
        List<TagEntity> tags = tagRepository.findAll();
        for (TagEntity tag: tags) {
            if(listOfTagsAndSkillsAndCategories.contains(tag.getTagName())){
                listOfTagsAndSkillsAndCategories.remove(tag.getTagName());
                tagList.add(tag.getId());
            }
        }
        List<CategoryEntity> categoryEntities = categoryRepository.findAll();
        for(CategoryEntity category: categoryEntities){
            if(listOfTagsAndSkillsAndCategories.contains(category.getCategoryName())){
                listOfTagsAndSkillsAndCategories.remove(category.getCategoryName());
                categoryList.add(category.getId());
            }
        }

        for (String skillName: listOfTagsAndSkillsAndCategories) {
            SkillEntity skill = skillRepository.findBySkillName(skillName).orElseThrow(()-> new AppException("unknown skill",HttpStatus.NOT_FOUND));
            skillList.add(skill.getId());
        }

        if(categoryList.isEmpty() && tagList.isEmpty() ){
            postsRaw = postRepository.findByTitle(searchMultipleDto.getTitle());
        }
        else if(categoryList.isEmpty()){
            postsRaw = postRepository.findByTagsAndTitle(tagList, searchMultipleDto.getTitle());

        }else if(tagList.isEmpty()){
            postsRaw = postRepository.findByCategoriesAndTitle(categoryList,searchMultipleDto.getTitle());

        }else{
            postsRaw = postRepository.findByCategoriesAndTagsAndTitle(categoryList,tagList, searchMultipleDto.getTitle());
        }

        if(!skillList.isEmpty()){
            Iterator<PostEntity> postIterator = postsRaw.iterator();
            while(postIterator.hasNext()){
                PostEntity post = postIterator.next();
                List<PostSkillEntity> postSkillEntities = postSkillRepository.findByPost(post);
                boolean hasSkill = postSkillEntities.stream().anyMatch(postSkill -> skillList.contains(postSkill.getSkill().getId()));
                if(!hasSkill){
                    postIterator.remove();
                }
            }
        }

        postsRaw.sort(Comparator
                .comparingInt((PostEntity post) -> post.getNumOfUpvote() - post.getNumOfDownvote())
                .thenComparing(PostEntity::getDateOfPost).reversed());

        List<PostListDto> postList = new ArrayList<>();
        List<QuestionAnswerDto> qaList = new ArrayList<>();

        for(PostEntity post : postsRaw){
            UserEntity user = userRepository.findById(post.getUser().getId())
                    .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
            UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                    .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
            TagEntity tag = tagRepository.findById(post.getTag().getId())
                    .orElseThrow(() -> new AppException("Unknown tag", HttpStatus.NOT_FOUND));

            if (tag.getTagName().equalsIgnoreCase("Q&A")){
                QuestionAnswerDto questionAnswerDto = new QuestionAnswerDto(post.getId(), user.getId(),userDetails.getFullName(), userDetails.getProfileURL(),post.getTitle(), post.getDescription(), post.getContent(),
                        post.getDateOfPost().format(formatter), post.getNumOfUpvote(), post.getNumOfDownvote()
                        ,getCategoriesOfPost(getRelatedCategories(post.getCategory().getId())), getTagOfPost(tag), post.getCoverURL() ,post.isRewarded(), post.getSlug(), commentRepository.countNumOfCommentForPost(post.getId()));
                qaList.add(questionAnswerDto);
            }else{
                PostListDto postListDto = new PostListDto(post.getId(), user.getId(),userDetails.getFullName(), userDetails.getProfileURL(),post.getTitle(), post.getDescription(),
                        post.getDateOfPost().format(formatter), getCategoriesOfPost(getRelatedCategories(post.getCategory().getId())), getTagOfPost(tag), post.getCoverURL() ,post.isRewarded(), post.getSlug());
                postList.add(postListDto);
            }
        }

        return new SearchMultipleResultDto(postList,qaList);
    }
}
