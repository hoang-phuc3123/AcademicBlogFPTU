package com.academicblogfptu.AcademicBlogFPTU.services;

import com.academicblogfptu.AcademicBlogFPTU.dtos.CommentDtos.CommentDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.CommentDtos.CreateCommentDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.CommentDtos.ReplyCommentDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.CommentDtos.ReportCommentDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.MailStructureDto;
import com.academicblogfptu.AcademicBlogFPTU.entities.*;
import com.academicblogfptu.AcademicBlogFPTU.exceptions.AppException;
import com.academicblogfptu.AcademicBlogFPTU.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

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
    private NotifyByMailServices notifyByMailServices;

    @Autowired
    private final PendingReportRepository pendingReportRepository;

    @Autowired
    private final PendingReportReasonRepository pendingReportReasonRepository;

    @Autowired
    private final VoteRepository voteRepository;

    @Autowired
    private final AdminServices adminServices;

    @Autowired
    private final BadgeServices badgeServices;

    @Autowired
    private final NotificationServices notificationServices;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    ZoneId vietnamZone = ZoneId.of("Asia/Ho_Chi_Minh");

    // Get the current date and time in the specified time zone
    LocalDateTime localDateTime = LocalDateTime.now(vietnamZone);

    public CommentDto createComment(CreateCommentDto createCommentDto, UserEntity user){
        CommentEntity comment = new CommentEntity();

        PostEntity post = postRepository.findById(createCommentDto.getPostId())
                        .orElseThrow(()-> new AppException("Unknown post", HttpStatus.NOT_FOUND));

        UserDetailsEntity userDetails = userDetailsRepository.findByUserId(user.getId());

        List<BadgeEntity> userBadges = badgeServices.findBadgesByUserId(user.getId());

        comment.setContent(createCommentDto.getContent());
        comment.setDateOfComment(LocalDateTime.of(java.time.LocalDate.now(), java.time.LocalTime.now()));
        comment.setNumOfUpvote(0);
        comment.setNumOfDownvote(0);
        comment.setEdited(false);
        comment.setParentComment(null);
        comment.setPost(post);
        comment.setUser(user);
        commentRepository.save(comment);

        /*
        //send mail
        MailStructureDto mail = new MailStructureDto();
        mail.setTriggerId(user.getId());
        mail.setReceiverId(comment.getPost().getUser().getId());
        mail.setMailType("Comment");
        mail.setPostLink("https://fblog.site/view/" +comment.getPost().getSlug());
        notifyByMailServices.sendMail(mail);
        */

        return new CommentDto(comment.getId(), user.getId(), userDetails.getFullName(), userDetails.getProfileURL(), comment.getContent(),
                comment.isEdited(), comment.getNumOfUpvote(), comment.getNumOfDownvote(),
                comment.getDateOfComment().format(formatter), comment.getPost().getId(), null, userBadges);
    }

    public CommentDto editComment(CommentDto commentDto, UserEntity user){
        CommentEntity comment = commentRepository.findById(commentDto.getCommentId())
                .orElseThrow(()-> new AppException("Unknown comment",  HttpStatus.NOT_FOUND));

        UserDetailsEntity userDetails = userDetailsRepository.findByUserId(user.getId());

        List<BadgeEntity> userBadges = badgeServices.findBadgesByUserId(user.getId());

        comment.setContent(commentDto.getContent());
        comment.setEdited(true);
        comment.setDateOfComment(LocalDateTime.of(java.time.LocalDate.now(), java.time.LocalTime.now()));
        commentRepository.save(comment);

        Integer parentCommentId = (comment.getParentComment() !=  null) ? comment.getParentComment().getId() : null;

        return new CommentDto(comment.getId(), user.getId(),userDetails.getFullName(), userDetails.getProfileURL(), comment.getContent(),
                comment.isEdited(), comment.getNumOfUpvote(), comment.getNumOfDownvote(),
                comment.getDateOfComment().format(formatter), comment.getPost().getId(), parentCommentId, userBadges);
    }

    public void deleteComment(int commentId) {

        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException("Unknown comment", HttpStatus.NOT_FOUND));

        List<PendingReportEntity> pendingReportEntity = pendingReportRepository.findByContentIdAndReportType(comment.getId(),"Comment");

        List<VoteEntity> votes = voteRepository.findByCommentId(commentId);
        if (votes != null){
            for ( VoteEntity vote : votes) {
                voteRepository.delete(vote);
            }
        }

        if (!pendingReportEntity.isEmpty()){
            adminServices.deleteReportComment(comment.getId());
        }
        deleteReplyComments(comment);
        notificationServices.deleteDeletedCommentNotification(commentId);
        commentRepository.delete(comment);
    }
    private void deleteReplyComments(CommentEntity parentComment) {
        List<CommentEntity> replyComments = commentRepository.findByParentComment(parentComment);
        for (CommentEntity reply : replyComments) {
            List<PendingReportEntity> pendingReportEntity = pendingReportRepository.findByContentIdAndReportType(reply.getId(),"Comment");

            List<VoteEntity> votes = voteRepository.findByCommentId(reply.getId());
            if (votes != null){
                for ( VoteEntity vote : votes) {
                    voteRepository.delete(vote);
                }
            }

            if (!pendingReportEntity.isEmpty()){
                adminServices.deleteReportComment(reply.getId());
            }
            deleteReplyComments(reply);
            notificationServices.deleteDeletedCommentNotification(reply.getId());
            commentRepository.delete(reply);
        }
    }

    public CommentDto replyComment(ReplyCommentDto replyCommentDto, UserEntity user){
        CommentEntity comment = new CommentEntity();

        PostEntity post = postRepository.findById(replyCommentDto.getPostId())
                .orElseThrow(()-> new AppException("Unknown post", HttpStatus.NOT_FOUND));

        CommentEntity parentComment = commentRepository.findById(replyCommentDto.getParentCommentId())
                .orElseThrow(()-> new AppException("Unknown parent comment", HttpStatus.NOT_FOUND));

        UserDetailsEntity userDetails = userDetailsRepository.findByUserId(user.getId());

        List<BadgeEntity> userBadges = badgeServices.findBadgesByUserId(user.getId());

        comment.setContent(replyCommentDto.getContent());
        comment.setDateOfComment(LocalDateTime.of(java.time.LocalDate.now(), java.time.LocalTime.now()));
        comment.setNumOfUpvote(0);
        comment.setNumOfDownvote(0);
        comment.setEdited(false);
        comment.setParentComment(parentComment);
        comment.setPost(post);
        comment.setUser(user);
        commentRepository.save(comment);
        /*
        //send mail
        MailStructureDto mail = new MailStructureDto();
        mail.setTriggerId(user.getId());
        mail.setReceiverId(parentComment.getUser().getId());
        mail.setMailType("Reply-comment");
        mail.setPostLink("https://fblog.site/view/" +comment.getPost().getSlug());
        notifyByMailServices.sendMail(mail);

         */
        return new CommentDto(comment.getId(), user.getId(), userDetails.getFullName(), userDetails.getProfileURL(), comment.getContent(),
                comment.isEdited(), comment.getNumOfUpvote(), comment.getNumOfDownvote(),
                comment.getDateOfComment().format(formatter), comment.getPost().getId(), parentComment.getId(), userBadges);
    }

    public List<ReportReasonEntity> viewReportReason(){
        return reportReasonRepository.findAll();
    }

    public PendingReportEntity reportComment(ReportCommentDto reportCommentDto, UserEntity reporter){
        PendingReportEntity reportComment = new PendingReportEntity();

        CommentEntity comment = commentRepository.findById(reportCommentDto.getCommentId())
                .orElseThrow(()-> new AppException("Unknown comment", HttpStatus.NOT_FOUND));


        reportComment.setContent(comment.getContent());
        reportComment.setDateOfReport(LocalDateTime.of(java.time.LocalDate.now(), java.time.LocalTime.now()));
        reportComment.setReportType("Comment");
        reportComment.setContentId(comment.getId());

        reportComment.setUser(reporter);

        pendingReportRepository.save(reportComment);
        return reportComment;

    }

    public void pendingReportReason(PendingReportEntity report, int reasonOfReportId) {
        ReportReasonEntity reason = reportReasonRepository.findById(reasonOfReportId)
                .orElseThrow(()-> new AppException("Unknown reason", HttpStatus.NOT_FOUND));

        PendingReportReasonEntity pendingReportReason = new PendingReportReasonEntity();

        pendingReportReason.setReport(report);
        pendingReportReason.setReason(reason);

        pendingReportReasonRepository.save(pendingReportReason);
    }
}
