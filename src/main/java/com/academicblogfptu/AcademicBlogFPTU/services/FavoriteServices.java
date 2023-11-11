package com.academicblogfptu.AcademicBlogFPTU.services;

import com.academicblogfptu.AcademicBlogFPTU.dtos.FavoriteDtos.FavoriteDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.FavoriteDtos.FavoritePostResponseDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.FavoriteDtos.FavoriteQAResponseDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.PostDtos.PostListDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.PostDtos.QuestionAnswerDto;
import com.academicblogfptu.AcademicBlogFPTU.entities.*;
import com.academicblogfptu.AcademicBlogFPTU.exceptions.AppException;
import com.academicblogfptu.AcademicBlogFPTU.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteServices {

    @Autowired
    private FavoriteRepository favoriteRepository;
    @Autowired
    private PostServices postServices;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private PostDetailsRepository postDetailsRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserDetailsRepository userDetailsRepository;
    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private CommentRepository commentRepository;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<FavoritePostResponseDto> getFavoritePostList(int id){
        List<FavoritePostEntity> favoritePostEntities = favoriteRepository.findByUserId(id);
        return getPostsFavorite(favoritePostEntities);
    }

    public List<FavoriteQAResponseDto> getFavoriteQAList(int id){
        List<FavoritePostEntity> favoritePostEntities = favoriteRepository.findByUserId(id);
        return getAllQuestionAndAnswerPost(favoritePostEntities);
    }

    public void addFavorite(FavoriteDto favoriteDto){
        PostEntity post = postRepository.findById(favoriteDto.getPostId())
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
        UserEntity user = userRepository.findById(favoriteDto.getUserId())
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
        try{
            FavoritePostEntity favoritePost = new FavoritePostEntity();
            favoritePost.setUser(user);
            favoritePost.setPostId(post.getId());
            favoritePost.setSaveAt(LocalDateTime.now());
            favoriteRepository.save(favoritePost);
        }catch (Exception e){
            throw new AppException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    public void removeFavorite(FavoriteDto favoriteDto){
        FavoritePostEntity entity = favoriteRepository.findByPostIdAndUserId(favoriteDto.getPostId(),favoriteDto.getUserId());
        if(entity == null){
            throw new AppException("Unknown favorite",HttpStatus.NOT_FOUND);
        }else {
            try{
                favoriteRepository.delete(entity);
            }catch (Exception e){
                throw new AppException(e.getMessage(), HttpStatus.UNAUTHORIZED);
            }
        }
    }

    public Boolean checkFavorite(FavoriteDto favoriteDto){
        FavoritePostEntity entity = favoriteRepository.findByPostIdAndUserId(favoriteDto.getPostId(),favoriteDto.getUserId());
        if(entity == null){
            return false;
        }return true;
    }


    public List<FavoritePostResponseDto> getPostsFavorite(List<FavoritePostEntity> favoritePostEntities){
        List<FavoritePostResponseDto> responseDtos = new ArrayList<>();
        for (FavoritePostEntity favoritePost:
                favoritePostEntities) {

            FavoritePostResponseDto dto = new FavoritePostResponseDto();
            //add id and time
            dto.setFavoriteId(favoritePost.getId());
            dto.setSaveAt(favoritePost.getSaveAt().format(formatter));
            //get post to add
            PostEntity post = postRepository.findById(favoritePost.getPostId())
                    .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
            if (postServices.isApprove(post.getId()) && post.getId() == favoritePost.getPostId()) {
                UserEntity user = userRepository.findById(post.getUser().getId())
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                TagEntity tag = tagRepository.findById(post.getTag().getId())
                        .orElseThrow(() -> new AppException("Unknown tag", HttpStatus.NOT_FOUND));

                if (!tag.getTagName().equalsIgnoreCase("Q&A")) {
                    PostListDto postListDto = new PostListDto(post.getId(), user.getId() ,userDetails.getFullName(), userDetails.getProfileURL(), post.getTitle(), post.getDescription(),
                            post.getDateOfPost().format(formatter), postServices.getCategoriesOfPost(postServices.getRelatedCategories(post.getCategory().getId())), postServices.getTagOfPost(tag), post.getCoverURL(), post.isRewarded(), post.getSlug());

                    dto.setPostListDto(postListDto);
                    responseDtos.add(dto);
                }
            }
        }
        return responseDtos;
    }





    public List<FavoriteQAResponseDto> getAllQuestionAndAnswerPost(List<FavoritePostEntity> favoritePostEntities) {
        List<FavoriteQAResponseDto> responseDtos = new ArrayList<>();
        for (FavoritePostEntity favoritePost:
                favoritePostEntities) {

            FavoriteQAResponseDto dto = new FavoriteQAResponseDto();
            //add id and time
            dto.setFavoriteId(favoritePost.getId());
            dto.setSaveAt(favoritePost.getSaveAt().format(formatter));
            //get post to add
            PostEntity post = postRepository.findById(favoritePost.getPostId())
                    .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
            if (postServices.isApprove(post.getId()) && post.getId() == favoritePost.getPostId()) {
                UserEntity user = userRepository.findById(post.getUser().getId())
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                TagEntity tag = tagRepository.findById(post.getTag().getId())
                        .orElseThrow(() -> new AppException("Unknown tag", HttpStatus.NOT_FOUND));
                if (tag.getTagName().equalsIgnoreCase("Q&A")) {

                    int numOfUpvote = (post.getNumOfUpvote() != null) ? post.getNumOfUpvote() : 0;
                    int numOfDownvote = (post.getNumOfDownvote() != null) ? post.getNumOfDownvote() : 0;

                    QuestionAnswerDto questionAnswerDto = new QuestionAnswerDto(post.getId(), user.getId() , userDetails.getFullName(), userDetails.getProfileURL(),post.getTitle(), post.getDescription(),post.getContent(),
                            post.getDateOfPost().format(formatter),numOfUpvote,numOfDownvote ,postServices.getCategoriesOfPost(postServices.getRelatedCategories(post.getCategory().getId())),postServices.getTagOfPost(tag), post.getCoverURL(), post.isRewarded(), post.getSlug(), commentRepository.countNumOfCommentForPost(post.getId()));

                    dto.setQuestionAnswerDto(questionAnswerDto);
                    responseDtos.add(dto);
                }
            }
        }
        return responseDtos;
    }

}