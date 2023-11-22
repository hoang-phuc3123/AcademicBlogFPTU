package com.academicblogfptu.AcademicBlogFPTU.services;

import com.academicblogfptu.AcademicBlogFPTU.dtos.MailStructureDto;
import com.academicblogfptu.AcademicBlogFPTU.entities.UserDetailsEntity;
import com.academicblogfptu.AcademicBlogFPTU.exceptions.AppException;
import com.academicblogfptu.AcademicBlogFPTU.repositories.UserDetailsRepository;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class NotifyByMailServices {

    @Autowired
    private final UserDetailsRepository userDetailsRepository;
    @Autowired
    private JavaMailSender mailSender;

    @Value("$(FBlog)")
    private String fromMail;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");



    public MailStructureDto setAllFieldsToSendMail(MailStructureDto mailStructureDto){

        UserDetailsEntity triggerDetails = userDetailsRepository.findByUserId(mailStructureDto.getTriggerId());
        UserDetailsEntity receiverDetails = userDetailsRepository.findByUserId(mailStructureDto.getReceiverId());
        if( triggerDetails == null || receiverDetails == null ){
            throw new AppException("Unknown user", HttpStatus.NOT_FOUND);
        }
        else if (receiverDetails.getId() == triggerDetails.getId()) {
            return null;
        }
        else {
            mailStructureDto.setReceiverName(receiverDetails.getFullName());
            mailStructureDto.setReceiverMail(receiverDetails.getEmail());
            mailStructureDto.setTriggerName(triggerDetails.getFullName());
            switch (mailStructureDto.getMailType()){
                case "Reply-comment":
                    mailStructureDto.setSubject("Bình luận của bạn đã được phản hồi");
                    mailStructureDto.setMessage(
                            "Bình luận của bạn tại bài viết dưới đây đã được "
                                    +mailStructureDto.getTriggerName()+" phản hồi!\n\n"
                                    + "Bài viết: \n\n"
                                    + mailStructureDto.getPostLink()
                    );
                    break;
                case "Comment":
                    mailStructureDto.setSubject("Câu hỏi/bài viết của bạn đã được phản hồi");
                    mailStructureDto.setMessage(
                            "Câu hỏi/bài viết của bạn dưới đây đã được "
                                    +mailStructureDto.getTriggerName()+" phản hồi!\n\n "
                                    + "Câu hỏi/bài viết:\n\n"
                                    + mailStructureDto.getPostLink()
                    );
                    break;
                case "Approve-post":
                    mailStructureDto.setSubject("Bài viết của bạn đã được duyệt");
                    mailStructureDto.setMessage(
                            "Bài viết của bạn dưới đây đã được "
                                    +mailStructureDto.getTriggerName()+" duyệt!\n\n"
                                    + "Bài viết: \n\n"
                                    + mailStructureDto.getPostLink()
                    );
                    break;
                case "Approve-Q&A":
                    mailStructureDto.setSubject("Câu hỏi của bạn đã được duyệt");
                    mailStructureDto.setMessage(
                            "Câu hỏi của bạn dưới đây đã được "
                                    +mailStructureDto.getTriggerName()+" duyệt!\n\n "
                                    + "Bài viết:\n\n"
                                    + mailStructureDto.getPostLink()
                    );
                    break;
                case "Was-Banned":
                    mailStructureDto.setSubject("Tài khoản của bạn đã bị khóa!!");
                    mailStructureDto.setMessage(
                            "Tài khoản của bạn hiện đang bị khóa vì vi phạm quy tắc và bị report với số lượng lớn. \n\n"
                                    + "Dưới đây là những lý do bạn bị báo cáo: \n\n "
                                    + mailStructureDto.getPostLink()
                                    + "Vì thế chúng tôi sẽ tạm khóa tài khoản của bạn để điều tra! \n\n"
                                    +"Bạn sẽ không thể tham gia fblog.site bằng tài khoản này nữa! \n\n"
                                    + "Sẽ có thông báo nếu việc khóa tài khoản của bạn là hiểu lầm!\n\n"
                    );
                    break;
                case "unban":
                    mailStructureDto.setSubject("Tài khoản của bạn đã được mở khóa!!");
                    mailStructureDto.setMessage(
                            "Tài khoản của bạn đã được mở khóa sau khi chúng tôi điều tra!\n\n"
                                    + "Xin lỗi bạn vì sự bất tiện này!!\n\n"
                            + mailStructureDto.getPostLink()

                    );
                    break;
                case "Was-Muted":
                    mailStructureDto.setSubject("Tài khoản của bạn đã bị hạn chế");
                    mailStructureDto.setMessage(
                            "Tài khoản của bạn hiện đang bị hạn chế vì vi phạm quy tắc và bị report với số lượng lớn.\n\n"
                                    + "Vì thế chúng tôi sẽ hạn chế tài khoản của bạn trong khoảng thời gian nhất định!\n\n"
                                    + "Cụ thể là " + receiverDetails.getMutetime().toString()
                                    + "Dưới đây là những lý do bạn bị báo cáo: \n\n "
                                    + mailStructureDto.getPostLink()
                                    +"\n\nBạn sẽ không thể tương tác bằng tài khoảng này!\n\n"
                                    + "Sẽ có thông báo nếu việc khóa tài khoản của bạn là hiểu lầm!\n\n"
                    );
                    break;
                case "unmute":
                    mailStructureDto.setSubject("Tài khoản của bạn không còn bị hạn chế");
                    mailStructureDto.setMessage(
                            "Tài khoản của bạn hiện không còn bị hạn chế!!!\n\n"

                                    +"\n\nBạn sẽ có thể tương tác bằng tài khoảng này tại\n\n"
                            +mailStructureDto.getPostLink()

                    );
                    break;
                case "Post-decline":
                    mailStructureDto.setSubject("Bài viết của bạn đã bị từ chối!");
                    mailStructureDto.setMessage(
                            "Bài viết của bạn đã bị từ chối vì lý do sau:\n"
                                    + mailStructureDto.getPostLink()
                    );
                    break;
                case "Q&A-decline":
                    mailStructureDto.setSubject("Câu hỏi của bạn đã bị từ chối");
                    mailStructureDto.setMessage(
                            "Câu hỏi của bạn đã bị từ chối vì lý do sau:\n"
                                    + mailStructureDto.getPostLink()
                    );
                    break;
                default:
                    mailStructureDto.setSubject("Xin lỗi vì bất tiện!!!");
                    mailStructureDto.setMessage("Nếu bạn thấy mail này nghĩa là chúng tôi có một số sự cố ở việc gửi mail!\n\n"+
                            "Xin vui lòng liên hệ\nacademicblogfptu@gmail.com để thông báo cho chúng tôi biết điều này! \n\n"+
                            "Xin cảm ơn!!!");
                    break;
            }
        }
        return mailStructureDto;
    }


    public void sendRegisterMail(String email, String username, String password) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(fromMail);
        mail.setSubject("Đăng kí tài khoản thành công");
        mail.setText("Tài khoản của bạn đã được đăng kí thành công tại FBlog. Đây là thông tin đăng nhập:\nTên tài khoản: " + username + "\nMật khẩu: " + password + "\nVui lòng không chia sẻ thông tin tài khoản cho bất kì ai.");
        mail.setTo(email);
        mailSender.send(mail);
    }

    public void sendMail(MailStructureDto mailStructureDto){
        MailStructureDto baseMail = setAllFieldsToSendMail(mailStructureDto);
        if (baseMail == null) {
            return;
        }
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(fromMail);
        mail.setSubject(baseMail.getSubject());
        mail.setText(baseMail.getMessage());
        mail.setTo(baseMail.getReceiverMail());
        mailSender.send(mail);
    }

}
