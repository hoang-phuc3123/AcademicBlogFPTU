package com.academicblogfptu.AcademicBlogFPTU.services;

import com.academicblogfptu.AcademicBlogFPTU.dtos.CommentDtos.ReportCommentDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.PostDtos.PostListDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos.ProfileDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.PostDtos.QuestionAnswerDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos.ReportProfileDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos.SearchRequestDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos.SearchUserDto;
import com.academicblogfptu.AcademicBlogFPTU.entities.*;
import com.academicblogfptu.AcademicBlogFPTU.exceptions.AppException;
import com.academicblogfptu.AcademicBlogFPTU.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProfileServices {

    @Autowired
    private PostRepository postRepository;
    @Autowired
    private PostDetailsRepository postDetailsRepository;
    @Autowired
    private FollowerRepository followerRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BadgeServices badgeServices;
    @Autowired
    private UserDetailsRepository userDetailsRepository;
    @Autowired
    private TagRepository tagRepository;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    @Autowired
    private PostServices postServices;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PendingReportRepository pendingReportRepository;

    @Autowired
    private PendingReportReasonRepository pendingReportReasonRepository;

    @Autowired
    private UserSkillRepository userSkillRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private SkillServices skillServices;

    public ProfileDto viewProfile(int id,int currentUserId) {

        ProfileDto profile = new ProfileDto();

        UserEntity user = userRepository.findById(id)
                .orElseThrow(()-> new AppException("Unknown user", HttpStatus.NOT_FOUND));

        UserDetailsEntity userDetails = userDetailsRepository.findByUserId(id);

        List<SkillEntity> userSkillList = skillServices.findSkillByUser(user);

        List<BadgeEntity> badges = badgeServices.findBadgesByUserId(id);
        Map<String, List<PostListDto>> postList;
        Map<String, List<QuestionAnswerDto>> QAList;
        if(id == currentUserId){
            postList = getAllCurrentUserPost(id);
            QAList = getAllCurrentUserQuestionAndAnswerPost(id);
        }else{
            postList = getAllPost(id);
            QAList = getAllQuestionAndAnswerPost(id);
        }
        Long numOfFollower = followerRepository.countByUserId(id);
        Long numOfPost = postRepository.countByUserIdAndType(id, "Approve");
        profile.setUserId(id);
        profile.setFullname(userDetails.getFullName());
        profile.setProfileUrl(userDetails.getProfileURL());
        profile.setCoverUrl(userDetails.getCoverURL());
        profile.setUserStory(userDetails.getUserStory());
        profile.setBadges(badges);
        profile.setPostList(postList);
        profile.setQaList(QAList);
        profile.setNumOfFollower(numOfFollower);
        profile.setNumOfPost(numOfPost);
        profile.setUserSkillsList(userSkillList);

        return profile;
    }

    public void editProfile(ProfileDto profileDto){

        UserDetailsEntity userDetails = userDetailsRepository.findByUserId(profileDto.getUserId());

        if(profileDto.getFullname()!=null){
            userDetails.setFullName(profileDto.getFullname());
        }
        if(profileDto.getUserStory()!=null){
            userDetails.setUserStory(profileDto.getUserStory());
        }
        userDetailsRepository.save(userDetails);
    }

    public List<SearchUserDto> getAllUser(Integer id){
        List<SearchUserDto> listUser = new ArrayList<>();
        List<UserDetailsEntity> userDetailsEntities = userDetailsRepository.findAll();
        for (UserDetailsEntity user : userDetailsEntities) {
            if( !(user.getUser().getRole().getRoleName().equals("admin"))){
                if(!user.isBanned()){
                    Long numOfFollower = followerRepository.countByUserId(user.getUser().getId());
                    if(followerRepository.findByUserIdAndFollowedBy(user.getUser().getId(), id).isPresent()){
                        SearchUserDto dto = new SearchUserDto(user.getUser().getId(),user.getFullName(),user.getProfileURL(),numOfFollower,true);
                        listUser.add(dto);
                    }else{
                        SearchUserDto dto = new SearchUserDto(user.getUser().getId(),user.getFullName(),user.getProfileURL(),numOfFollower,false);
                        listUser.add(dto);
                    }
                }
            }
        }
        return listUser;
    }

    public List<SearchUserDto> getSearchResult(SearchRequestDto searchRequestDto){

        //Pageable page = PageRequest.of(searchRequestDto.getPage()-1,searchRequestDto.getUsersOfPage());
        //Page<UserDetailsEntity> pageResult = userDetailsRepository.findUsersPage(searchRequestDto.getSearch(),page);

        List<UserDetailsEntity> userDetailsEntities = userDetailsRepository.findUserByFullName(searchRequestDto.getSearch());


        List<SearchUserDto> searchResults = new ArrayList<>();
        //pageResult.getContent().forEach(userDetailsEntity -> {
        for (UserDetailsEntity userDetailsEntity: userDetailsEntities) {
            if( !(userDetailsEntity.getUser().getRole().getRoleName().equals("admin"))){
                if(!userDetailsEntity.isBanned()){
                    Long numOfFollower = followerRepository.countByUserId(userDetailsEntity.getUser().getId());
                    if(followerRepository.findByUserIdAndFollowedBy(userDetailsEntity.getUser().getId(), searchRequestDto.getUserId()).isPresent()){
                        SearchUserDto dto = new SearchUserDto(userDetailsEntity.getUser().getId(),userDetailsEntity.getFullName(),userDetailsEntity.getProfileURL(),numOfFollower,true);
                        searchResults.add(dto);
                    }else{
                        SearchUserDto dto = new SearchUserDto(userDetailsEntity.getUser().getId(),userDetailsEntity.getFullName(),userDetailsEntity.getProfileURL(),numOfFollower,false);
                        searchResults.add(dto);
                    }
                }
            }
        }
        //});
        return searchResults;
    }


    public Map<String, List<PostListDto>> getAllCurrentUserPost(int id) {
        List<PostEntity> list = postRepository.findByUserId(id);
        List<PostListDto> approvePostList = new ArrayList<>();
        List<PostListDto> pendingPostList = new ArrayList<>();
        Map<String, List<PostListDto>> result = new HashMap<>();
        for (PostEntity post : list) {

            if (postServices.isApprove(post.getId()) || postServices.isPending(post.getId())) {
                UserEntity user = userRepository.findById(post.getUser().getId())
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                TagEntity tag = tagRepository.findById(post.getTag().getId())
                        .orElseThrow(() -> new AppException("Unknown tag", HttpStatus.NOT_FOUND));

                if (!tag.getTagName().equalsIgnoreCase("Q&A")) {

                    PostListDto postListDto = new PostListDto(post.getId(), user.getId() ,userDetails.getFullName(), userDetails.getProfileURL(), post.getTitle(), post.getDescription(),
                            post.getDateOfPost().format(formatter), postServices.getCategoriesOfPost(postServices.getRelatedCategories(post.getCategory().getId())), postServices.getTagOfPost(tag), post.getCoverURL(), post.isRewarded(), post.getSlug());

                    if (postServices.isApprove(post.getId())){
                        approvePostList.add(postListDto);
                    }else if (postServices.isPending(post.getId())){
                        pendingPostList.add(postListDto);
                    }
                }
            }
        }
        result.put("ApprovedPost", approvePostList);
        result.put("PendingPost", pendingPostList);

        return result;
    }


    public Map<String, List<PostListDto>> getAllPost(int id) {
        List<PostEntity> list = postRepository.findByUserId(id);
        List<PostListDto> approvePostList = new ArrayList<>();
        Map<String, List<PostListDto>> result = new HashMap<>();
        for (PostEntity post : list) {

            if (postServices.isApprove(post.getId())) {
                UserEntity user = userRepository.findById(post.getUser().getId())
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                TagEntity tag = tagRepository.findById(post.getTag().getId())
                        .orElseThrow(() -> new AppException("Unknown tag", HttpStatus.NOT_FOUND));

                if (!tag.getTagName().equalsIgnoreCase("Q&A")) {
                    PostListDto postListDto = new PostListDto(post.getId(), user.getId() ,userDetails.getFullName(), userDetails.getProfileURL(), post.getTitle(), post.getDescription(),
                            post.getDateOfPost().format(formatter), postServices.getCategoriesOfPost(postServices.getRelatedCategories(post.getCategory().getId())), postServices.getTagOfPost(tag), post.getCoverURL(), post.isRewarded(), post.getSlug());
                    approvePostList.add(postListDto);
                }
            }
        }
        result.put("ApprovedPost", approvePostList);
        return result;
    }

    public Map<String, List<QuestionAnswerDto>> getAllQuestionAndAnswerPost(int id) {
        List<PostEntity> list = postRepository.findByUserId(id);
        List<QuestionAnswerDto> approveQAPostList = new ArrayList<>();
        Map<String, List<QuestionAnswerDto>> result = new HashMap<>();

        for (PostEntity post : list) {
            if (postServices.isApprove(post.getId())) {
                UserEntity user = userRepository.findById(post.getUser().getId())
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                TagEntity tag = tagRepository.findById(post.getTag().getId())
                        .orElseThrow(() -> new AppException("Unknown tag", HttpStatus.NOT_FOUND));
                if (tag.getTagName().equalsIgnoreCase("Q&A")) {

                    int numOfUpvote = (post.getNumOfUpvote() != null) ? post.getNumOfUpvote() : 0;
                    int numOfDownvote = (post.getNumOfDownvote() != null) ? post.getNumOfDownvote() : 0;

                    QuestionAnswerDto questionAnswerDto = new QuestionAnswerDto(post.getId(), user.getId() ,userDetails.getFullName(), userDetails.getProfileURL() ,post.getTitle(), post.getDescription() ,post.getContent(),
                            post.getDateOfPost().format(formatter), numOfUpvote, numOfDownvote, postServices.getCategoriesOfPost(postServices.getRelatedCategories(post.getCategory().getId())), postServices.getTagOfPost(tag), post.getCoverURL(), post.isRewarded(), post.getSlug(), commentRepository.countNumOfCommentForPost(post.getId()));
                    approveQAPostList.add(questionAnswerDto);
                }
            }
        }
        result.put("ApprovedQA", approveQAPostList);
        return result;
    }
    public Map<String, List<QuestionAnswerDto>> getAllCurrentUserQuestionAndAnswerPost(int id) {
        List<PostEntity> list = postRepository.findByUserId(id);
        List<QuestionAnswerDto> approveQAPostList = new ArrayList<>();
        List<QuestionAnswerDto> pendingQAPostList = new ArrayList<>();
        Map<String, List<QuestionAnswerDto>> result = new HashMap<>();

        for (PostEntity post : list) {
            if (postServices.isApprove(post.getId())|| postServices.isPending(post.getId())) {
                UserEntity user = userRepository.findById(post.getUser().getId())
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                TagEntity tag = tagRepository.findById(post.getTag().getId())
                        .orElseThrow(() -> new AppException("Unknown tag", HttpStatus.NOT_FOUND));
                if (tag.getTagName().equalsIgnoreCase("Q&A")) {

                    int numOfUpvote = (post.getNumOfUpvote() != null) ? post.getNumOfUpvote() : 0;
                    int numOfDownvote = (post.getNumOfDownvote() != null) ? post.getNumOfDownvote() : 0;

                    QuestionAnswerDto questionAnswerDto = new QuestionAnswerDto(post.getId(), user.getId() ,userDetails.getFullName(), userDetails.getProfileURL() ,post.getTitle(), post.getDescription() ,post.getContent(),
                            post.getDateOfPost().format(formatter), numOfUpvote, numOfDownvote, postServices.getCategoriesOfPost(postServices.getRelatedCategories(post.getCategory().getId())), postServices.getTagOfPost(tag), post.getCoverURL(), post.isRewarded(), post.getSlug(), commentRepository.countNumOfCommentForPost(post.getId()));

                    if (postServices.isApprove(post.getId())){
                        approveQAPostList.add(questionAnswerDto);
                    }else if (postServices.isPending(post.getId())){
                        pendingQAPostList.add(questionAnswerDto);
                    }
                }
            }
        }
        result.put("ApprovedQA", approveQAPostList);
        result.put("PendingQA", pendingQAPostList);
        return result;
    }

    public List<BadgeEntity> getAllBadge(int id) {
        return badgeServices.findBadgesByUserId(id);
    }

    public PendingReportEntity reportProfile(ReportProfileDto reportProfileDto, UserEntity reporter){
        PendingReportEntity reportProfile = new PendingReportEntity();

        UserEntity user = userRepository.findById(reportProfileDto.getUserId())
                .orElseThrow(()-> new AppException("Unknown user", HttpStatus.NOT_FOUND));

        UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                .orElseThrow(()-> new AppException("Unknown user detail", HttpStatus.NOT_FOUND));


        reportProfile.setContent(userDetails.getFullName());
        reportProfile.setDateOfReport(LocalDateTime.of(java.time.LocalDate.now(), java.time.LocalTime.now()));
        reportProfile.setReportType("Profile");
        reportProfile.setContentId(user.getId());
        reportProfile.setUser(reporter);

        pendingReportRepository.save(reportProfile);
        return reportProfile;
    }
}
