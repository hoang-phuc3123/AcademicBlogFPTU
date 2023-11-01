package com.academicblogfptu.AcademicBlogFPTU.services;

import com.academicblogfptu.AcademicBlogFPTU.dtos.VoteDto;
import com.academicblogfptu.AcademicBlogFPTU.entities.CommentEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.PostEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.VoteEntity;
import com.academicblogfptu.AcademicBlogFPTU.exceptions.AppException;
import com.academicblogfptu.AcademicBlogFPTU.repositories.CommentRepository;
import com.academicblogfptu.AcademicBlogFPTU.repositories.PostRepository;
import com.academicblogfptu.AcademicBlogFPTU.repositories.UserRepository;
import com.academicblogfptu.AcademicBlogFPTU.repositories.VoteRepository;
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
public class VoteServices {

    @Autowired
    private VoteRepository voteRepository;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private UserRepository userRepository;

    public List<VoteDto> getVoteInPost(int postId,int userId){
        try{
            List<VoteEntity> voteList = voteRepository.findByPostIdAndUserId(postId,userId);
            List<VoteDto> voteDtos = new ArrayList<>();
            for (VoteEntity vote : voteList) {
                VoteDto voteDto = mapToDto(vote);
                voteDtos.add(voteDto);
            }
            return voteDtos;
        }catch (Exception e){
            throw new AppException(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    public void giveAVote(VoteDto voteDto){

        PostEntity post = postRepository.findById(voteDto.getPostId()).orElseThrow(() -> new AppException("Unknown post", HttpStatus.NOT_FOUND));

        if(voteRepository.findByPostIdAndUserIdAndCommentId(voteDto.getPostId(),voteDto.getUserId(),voteDto.getCommentId())!=null){
            throw new AppException("Already vote",HttpStatus.IM_USED);
        }


        VoteEntity vote = new VoteEntity();
        vote.setVoteTime(LocalDateTime.now());
        vote.setUser(userRepository.findById(voteDto.getUserId()).orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND)));

        vote.setTypeOfVote(voteDto.getTypeOfVote());

        if(voteDto.getCommentId() != null){
            CommentEntity comment = commentRepository.findById(voteDto.getCommentId()).orElseThrow(() -> new AppException("Unknown comment", HttpStatus.NOT_FOUND));
            if(vote.getTypeOfVote().equalsIgnoreCase("up")){
                comment.setNumOfDownvote(comment.getNumOfUpvote()+1);
            }else{
                comment.setNumOfDownvote(comment.getNumOfDownvote()+1);
            }
            vote.setComment(comment);
            vote.setPost(post);
            commentRepository.save(comment);
            voteRepository.save(vote);
        }else{
            if(voteDto.getTypeOfVote().equalsIgnoreCase("up")){
                post.setNumOfUpvote(post.getNumOfUpvote()+1);
            }else{
                post.setNumOfDownvote(post.getNumOfDownvote()+1);
            }
            vote.setPost(post);
            postRepository.save(post);
            voteRepository.save(vote);
        }

    }

    public void removeAVote(VoteDto voteDto){

        VoteEntity vote = voteRepository.findById(voteDto.getVoteId()).orElseThrow(() -> new AppException("Unknown vote", HttpStatus.NOT_FOUND));

        PostEntity post = postRepository.findById(vote.getPost().getId()).orElseThrow(() -> new AppException("Unknown post", HttpStatus.NOT_FOUND));

        if(vote.getComment()!= null){
            CommentEntity comment = commentRepository.findById(voteDto.getCommentId()).orElseThrow(() -> new AppException("Unknown comment", HttpStatus.NOT_FOUND));
            if(vote.getTypeOfVote().equalsIgnoreCase("up")){
                comment.setNumOfDownvote(comment.getNumOfUpvote() - 1);
            }else{
                comment.setNumOfDownvote(comment.getNumOfDownvote() - 1);
            }
            vote.setComment(comment);
            vote.setPost(post);
            commentRepository.save(comment);
            voteRepository.delete(vote);
        }else{
            if(vote.getTypeOfVote().equalsIgnoreCase("up")){
                post.setNumOfDownvote(post.getNumOfUpvote() - 1);
            }else{
                post.setNumOfDownvote(post.getNumOfDownvote() - 1);
            }
            vote.setPost(post);
            postRepository.save(post);
            voteRepository.delete(vote);
        }
    }



    public VoteDto mapToDto(VoteEntity voteEntity){
        try {
            VoteDto vote = new VoteDto();
            vote.setVoteId(voteEntity.getId());
            vote.setUserId(voteEntity.getUser().getId());
            vote.setTypeOfVote(vote.getTypeOfVote());
            vote.setPostId(voteEntity.getPost().getId());
            vote.setCommentId(voteEntity.getComment().getId());
            vote.setVoteTime(voteEntity.getVoteTime().format(formatter));
            return vote;
        }catch (Exception e){
            throw new AppException(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

}
