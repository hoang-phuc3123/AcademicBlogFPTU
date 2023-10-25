package com.academicblogfptu.AcademicBlogFPTU.services;

import com.academicblogfptu.AcademicBlogFPTU.dtos.CommentDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.CreateCommentDto;
import com.academicblogfptu.AcademicBlogFPTU.entities.CommentEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.PostEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.UserDetailsEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.UserEntity;
import com.academicblogfptu.AcademicBlogFPTU.exceptions.AppException;
import com.academicblogfptu.AcademicBlogFPTU.repositories.CommentRepository;
import com.academicblogfptu.AcademicBlogFPTU.repositories.PostRepository;
import com.academicblogfptu.AcademicBlogFPTU.repositories.UserDetailsRepository;
import com.academicblogfptu.AcademicBlogFPTU.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
        return new CommentDto(comment.getId(), comment.getContent(), userDetails.getFullName(),
                comment.isEdited(), comment.getNumOfUpvote(), comment.getNumOfDownvote(),
                comment.getDateOfComment().format(formatter), comment.getPost().getId(), userDetails.getProfileURL());
    }

    public CommentDto editComment(CommentDto commentDto, UserEntity user){
        CommentEntity comment = commentRepository.findById(commentDto.getCommentId())
                .orElseThrow(()-> new AppException("Unknown comment",  HttpStatus.UNAUTHORIZED));

        UserDetailsEntity userDetails = userDetailsRepository.findByUserId(user.getId());

        comment.setContent(commentDto.getContent());
        comment.setEdited(true);
        commentRepository.save(comment);

         return new CommentDto(comment.getId(), comment.getContent(), userDetails.getFullName(),
                comment.isEdited(), comment.getNumOfUpvote(), comment.getNumOfDownvote(),
                comment.getDateOfComment().format(formatter), comment.getPost().getId(), userDetails.getProfileURL());
    }

}
