package com.academicblogfptu.AcademicBlogFPTU.services;

import com.academicblogfptu.AcademicBlogFPTU.dtos.PostListDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.ProfileDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.QuestionAnswerDto;
import com.academicblogfptu.AcademicBlogFPTU.entities.*;
import com.academicblogfptu.AcademicBlogFPTU.exceptions.AppException;
import com.academicblogfptu.AcademicBlogFPTU.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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


    public ProfileDto viewProfile(int id) {

        ProfileDto profile = new ProfileDto();

        UserDetailsEntity userDetails = userDetailsRepository.findByUserId(id);

        List<BadgeEntity> badges = badgeServices.findBadgesByUserId(id);
        List<PostListDto> postList = getAllPost(id);
        List<QuestionAnswerDto> QAList = getAllQuestionAndAnswerPost(id);
        Long numOfFollower = followerRepository.countByUserId(id);
        Long numOfPost = postDetailsRepository.countByUserIdAndType(id, "Approve");

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

        return profile;
    }

    public void editProfile(ProfileDto profileDto){

        UserDetailsEntity userDetails = userDetailsRepository.findByUserId(profileDto.getUserId());

        if(profileDto.getProfileUrl()!=null){
            userDetails.setProfileURL(profileDto.getProfileUrl());
        }
        if(profileDto.getCoverUrl()!=null){
            userDetails.setCoverURL(profileDto.getCoverUrl());
        }
        if(profileDto.getFullname()!=null){
            userDetails.setFullName(profileDto.getFullname());
        }
        if(profileDto.getUserStory()!=null){
            userDetails.setUserStory(profileDto.getUserStory());
        }
        userDetailsRepository.save(userDetails);
    }


    public List<PostListDto> getAllPost(int id) {
        List<PostEntity> list = postRepository.findAll();
        List<PostListDto> postList = new ArrayList<>();
        for (PostEntity post : list) {

            if (postServices.isApprove(post.getId()) && post.getUser().getId() == id) {
                UserEntity user = userRepository.findById(post.getUser().getId())
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                TagEntity tag = tagRepository.findById(post.getTag().getId())
                        .orElseThrow(() -> new AppException("Unknown tag", HttpStatus.NOT_FOUND));

                if (!tag.getTagName().equalsIgnoreCase("Q&A")) {
                    PostListDto postListDto = new PostListDto(post.getId(), user.getId() ,userDetails.getFullName(), userDetails.getProfileURL(), post.getTitle(), post.getDescription(),
                            post.getDateOfPost().format(formatter), postServices.getRelatedCategories(post.getCategory().getId()), tag.getTagName(), post.getCoverURL(), post.isRewarded(), post.getSlug());
                    postList.add(postListDto);
                }
            }
        }
        return postList;
    }

    public List<QuestionAnswerDto> getAllQuestionAndAnswerPost(int id) {
        List<PostEntity> list = postRepository.findAll();
        List<QuestionAnswerDto> QAPostList = new ArrayList<>();
        for (PostEntity post : list) {

            if (postServices.isApprove(post.getId()) && post.getUser().getId() == id) {
                UserEntity user = userRepository.findById(post.getUser().getId())
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                TagEntity tag = tagRepository.findById(post.getTag().getId())
                        .orElseThrow(() -> new AppException("Unknown tag", HttpStatus.NOT_FOUND));
                if (tag.getTagName().equalsIgnoreCase("Q&A")) {

                    int numOfUpvote = (post.getNumOfUpvote() != null) ? post.getNumOfUpvote() : 0;
                    int numOfDownvote = (post.getNumOfDownvote() != null) ? post.getNumOfDownvote() : 0;

                    QuestionAnswerDto questionAnswerDto = new QuestionAnswerDto(post.getId(), user.getId() ,userDetails.getFullName(), post.getTitle(), post.getContent(),
                            post.getDateOfPost().format(formatter), numOfUpvote, numOfDownvote, postServices.getRelatedCategories(post.getCategory().getId()), tag.getTagName(), post.getCoverURL(), post.isRewarded());
                    QAPostList.add(questionAnswerDto);
                }
            }
        }
        return QAPostList;
    }

    public List<BadgeEntity> getAllBadge(int id) {
        return badgeServices.findBadgesByUserId(id);
    }


}
