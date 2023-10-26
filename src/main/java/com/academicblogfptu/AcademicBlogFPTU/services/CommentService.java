package com.academicblogfptu.AcademicBlogFPTU.services;

import com.academicblogfptu.AcademicBlogFPTU.dtos.CommentDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.CreateCommentDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.ReplyCommentDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.ReportCommentDto;
import com.academicblogfptu.AcademicBlogFPTU.entities.*;
import com.academicblogfptu.AcademicBlogFPTU.exceptions.AppException;
import com.academicblogfptu.AcademicBlogFPTU.repositories.*;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.parser.HttpParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.time.ZoneId;
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

    @Autowired
    private final ReportReasonRepository reportReasonRepository;

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final PendingReportRepository pendingReportRepository;

    @Autowired
    private final PendingReportReasonRepository pendingReportReasonRepository;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    ZoneId vietnamZone = ZoneId.of("Asia/Ho_Chi_Minh");

    // Get the current date and time in the specified time zone
    LocalDateTime localDateTime = LocalDateTime.now(vietnamZone);

    public CommentDto createComment(CreateCommentDto createCommentDto, UserEntity user){
        CommentEntity comment = new CommentEntity();

        PostEntity post = postRepository.findById(createCommentDto.getPostId())
                        .orElseThrow(()-> new AppException("Unknown post", HttpStatus.UNAUTHORIZED));

        UserDetailsEntity userDetails = userDetailsRepository.findByUserId(user.getId());

        comment.setContent(createCommentDto.getContent());
        comment.setDateOfComment(localDateTime);
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
        comment.setDateOfComment(localDateTime);
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

    public List<ReportReasonEntity> viewReportReason(){
        return reportReasonRepository.findAll();
    }

    public PendingReportEntity reportComment(ReportCommentDto reportCommentDto){
        PendingReportEntity reportComment = new PendingReportEntity();

        CommentEntity comment = commentRepository.findById(reportCommentDto.getCommentId())
                .orElseThrow(()-> new AppException("Unknown comment", HttpStatus.UNAUTHORIZED));

        UserEntity user = userRepository.findById(comment.getUser().getId())
                .orElseThrow(()-> new AppException("Unknown user", HttpStatus.UNAUTHORIZED));

        reportComment.setContent(comment.getContent());
        reportComment.setDateOfReport(localDateTime);
        reportComment.setReportType("Comment");
        reportComment.setComment(comment);
        reportComment.setUser(user);

        pendingReportRepository.save(reportComment);
        return  reportComment;
    }

    public void pendingReportReason(PendingReportEntity report, int reasonOfReportId) {
        ReportReasonEntity reason = reportReasonRepository.findById(reasonOfReportId)
                .orElseThrow(()-> new AppException("Unknown reason", HttpStatus.UNAUTHORIZED));

        PendingReportReasonEntity pendingReportReason = new PendingReportReasonEntity();

        pendingReportReason.setReport(report);
        pendingReportReason.setReason(reason);

        pendingReportReasonRepository.save(pendingReportReason);
    }
}
