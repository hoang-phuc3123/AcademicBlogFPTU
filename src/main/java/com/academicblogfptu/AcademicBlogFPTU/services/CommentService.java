package com.academicblogfptu.AcademicBlogFPTU.services;

import com.academicblogfptu.AcademicBlogFPTU.dtos.CommentDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.CreateCommentDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.ReplyCommentDto;
import com.academicblogfptu.AcademicBlogFPTU.entities.*;
import com.academicblogfptu.AcademicBlogFPTU.exceptions.AppException;
import com.academicblogfptu.AcademicBlogFPTU.repositories.CommentRepository;
import com.academicblogfptu.AcademicBlogFPTU.repositories.PostRepository;
import com.academicblogfptu.AcademicBlogFPTU.repositories.UserDetailsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    @Autowired
    private final CommentRepository commentRepository;

    @Autowired
    private final PostRepository postRepository;

    @Autowired
    private final UserDetailsRepository userDetailsRepository;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public CommentDto createComment(CreateCommentDto createCommentDto, UserEntity user){
        CommentEntity comment = new CommentEntity();

        PostEntity post = postRepository.findById(createCommentDto.getPostId())
                        .orElseThrow(()-> new AppException("Unknown post", HttpStatus.UNAUTHORIZED));

        UserDetailsEntity userDetails = userDetailsRepository.findByUserId(user.getId());

        comment.setContent(createCommentDto.getContent());
        comment.setDateOfComment(LocalDateTime.of(java.time.LocalDate.now(), java.time.LocalTime.now()));
        comment.setNumOfUpvote(0);
        comment.setNumOfDownvote(0);
        comment.setEdited(false);
        comment.setParentComment(null);
        comment.setPost(post);
        comment.setUser(user);
        commentRepository.save(comment);
        return new CommentDto(comment.getId(), userDetails.getFullName(), userDetails.getProfileURL(), comment.getContent(),
                comment.isEdited(), comment.getNumOfUpvote(), comment.getNumOfDownvote(),
                comment.getDateOfComment().format(formatter), comment.getPost().getId());
    }

    public CommentDto editComment(CommentDto commentDto, UserEntity user){
        CommentEntity comment = commentRepository.findById(commentDto.getCommentId())
                .orElseThrow(()-> new AppException("Unknown comment",  HttpStatus.UNAUTHORIZED));

        UserDetailsEntity userDetails = userDetailsRepository.findByUserId(user.getId());

        comment.setContent(commentDto.getContent());
        comment.setEdited(true);
        commentRepository.save(comment);

        return new CommentDto(comment.getId(), userDetails.getFullName(), userDetails.getProfileURL(), comment.getContent(),
                comment.isEdited(), comment.getNumOfUpvote(), comment.getNumOfDownvote(),
                comment.getDateOfComment().format(formatter), comment.getPost().getId());
    }

    public void deleteComment(int commentId) {

        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException("Unknown comment", HttpStatus.UNAUTHORIZED));
        deleteReplyComments(comment);
        commentRepository.delete(comment);
    }
    private void deleteReplyComments(CommentEntity parentComment) {
        List<CommentEntity> replyComments = commentRepository.findByParentComment(parentComment);
        for (CommentEntity reply : replyComments) {
            deleteReplyComments(reply);
            commentRepository.delete(reply);
        }
    }

    public CommentDto replyComment(ReplyCommentDto replyCommentDto, UserEntity user){
        CommentEntity comment = new CommentEntity();

        PostEntity post = postRepository.findById(replyCommentDto.getPostId())
                .orElseThrow(()-> new AppException("Unknown post", HttpStatus.UNAUTHORIZED));

        CommentEntity parentComment = commentRepository.findById(replyCommentDto.getParentCommentId())
                .orElseThrow(()-> new AppException("Unknown post", HttpStatus.UNAUTHORIZED));

        UserDetailsEntity userDetails = userDetailsRepository.findByUserId(user.getId());

        comment.setContent(replyCommentDto.getContent());
        comment.setDateOfComment(LocalDateTime.of(java.time.LocalDate.now(), java.time.LocalTime.now()));
        comment.setNumOfUpvote(0);
        comment.setNumOfDownvote(0);
        comment.setEdited(false);
        comment.setParentComment(parentComment);
        comment.setPost(post);
        comment.setUser(user);
        commentRepository.save(comment);
        return new CommentDto(comment.getId(), userDetails.getFullName(), userDetails.getProfileURL(), comment.getContent(),
                comment.isEdited(), comment.getNumOfUpvote(), comment.getNumOfDownvote(),
                comment.getDateOfComment().format(formatter), comment.getPost().getId());
    }

}
