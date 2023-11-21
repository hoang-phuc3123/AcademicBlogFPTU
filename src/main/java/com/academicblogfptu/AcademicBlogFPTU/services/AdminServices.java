package com.academicblogfptu.AcademicBlogFPTU.services;

import com.academicblogfptu.AcademicBlogFPTU.dtos.AdminDtos.ActivitiesLogDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.CommentDtos.ReportedCommentDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.MailStructureDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos.RegisterDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos.ReportedProfileDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos.UserDto;
import com.academicblogfptu.AcademicBlogFPTU.entities.*;
import com.academicblogfptu.AcademicBlogFPTU.exceptions.AppException;
import com.academicblogfptu.AcademicBlogFPTU.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
//import java.util.Date;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.nio.CharBuffer;
import java.time.LocalDate;
import java.sql.Date;


@RequiredArgsConstructor
@Service
public class AdminServices {

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final RoleRepository roleRepository;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private final MajorRepository majorRepository;

    @Autowired
    private final UserDetailsRepository userDetailsRepository;

    @Autowired
    private final RoleUpdateRepository roleUpdateHistoryRepository;

    @Autowired
    private final PendingReportRepository pendingReportRepository;

    @Autowired
    private final PendingReportReasonRepository pendingReportReasonRepository;

    @Autowired
    private final ReportReasonRepository reportReasonRepository;

    @Autowired
    private final FollowerRepository followerRepository;

    @Autowired
    private final BadgeServices badgeServices;

    @Autowired
    private final CommentRepository commentRepository;
    @Autowired
    private NotifyByMailServices notifyByMailServices;

    @Autowired
    private final ActivitiesLogRepository activitiesLogRepository;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public UserDto findById(int id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.UNAUTHORIZED));
        UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.UNAUTHORIZED));
        return new UserDto(user.getId(),user.getUsername(),userDetails.getFullName(),userDetails.isBanned(),userDetails.isMuted(),userDetails.getMutetime(),user.getRole().getRoleName(), userDetails.getProfileURL(),userDetails.getCoverURL(), "" , "");
    }

    public boolean isEmailExist(String email) {
        Optional<UserDetailsEntity> userDetails = userDetailsRepository.findByEmail(email);
        return userDetails.isPresent();
    }

    public void saveActivity(ActivitiesLogDto activitiesLogDto) {
        ActivitiesLogEntity activitiesLogEntity = new ActivitiesLogEntity();
        activitiesLogEntity.setActionTime(activitiesLogDto.getActionTime());
        activitiesLogEntity.setAction(activitiesLogDto.getAction());
        Optional<UserEntity> userEntity = userRepository.findById(activitiesLogDto.getUserID());
        activitiesLogEntity.setUser(userEntity.get());
        activitiesLogRepository.save(activitiesLogEntity);
    }

    public UserDto register(RegisterDto registerDto) {
        Optional<UserEntity> optionalUser = userRepository.findByUsername(registerDto.getUsername());
        if (optionalUser.isPresent()) {
            // Nếu tìm thấy người dùng, trả về thông tin người dùng hiện tại
            UserEntity user = optionalUser.get();
            UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                    .orElseThrow(() -> new AppException("Unknown user", HttpStatus.UNAUTHORIZED));
            return new UserDto(user.getId(), user.getUsername(), userDetails.getFullName(), userDetails.isBanned(), userDetails.isMuted(),userDetails.getMutetime(), user.getRole().getRoleName(), userDetails.getProfileURL(), userDetails.getCoverURL(), "" , "");
        } else {
            // Nếu không tìm thấy, tạo một tài khoản mới và trả về thông tin của tài khoản mới
            UserEntity newUser = new UserEntity();
            newUser.setUsername(registerDto.getUsername());
            newUser.setPassword(passwordEncoder.encode(CharBuffer.wrap(registerDto.getPassword())));
            RoleEntity roleEntity = roleRepository.findByRoleName(registerDto.getRole()).orElse(null);
            newUser.setRole(roleEntity);
            UserEntity user = userRepository.save(newUser);
            //Set badge for user
            badgeServices.setRoleBadge(user);
            // Tạo UserDto từ tài khoản mới và trả về
            return new UserDto(newUser.getId(), newUser.getUsername(), "" ,false, false, null, newUser.getRole().getRoleName(), "","", "", "");
        }
    }

    public void RegisterUserDetail(RegisterDto userDetailsDto){
        Optional<UserEntity> optionalUser = userRepository.findByUsername(userDetailsDto.getUsername());
        UserEntity user = optionalUser.get();
        UserDetailsEntity newUserDetails = new UserDetailsEntity();
        newUserDetails.setEmail(userDetailsDto.getEmail());
        newUserDetails.setFullName(userDetailsDto.getFullname());
        newUserDetails.setPhone(userDetailsDto.getPhone());
        newUserDetails.setBanned(false);
        newUserDetails.setMuted(false);
        newUserDetails.setWeightOfReport(0);
        newUserDetails.setProfileURL(null);
        newUserDetails.setCoverURL(null);
        newUserDetails.setUserStory(null);
        newUserDetails.setUser(user);
        MajorEntity majorEntity = majorRepository.findById(userDetailsDto.getMajorID()).orElseThrow(()-> new AppException("unknown major",HttpStatus.NOT_FOUND));
        newUserDetails.setMajor(majorEntity);
        badgeServices.setMajorBadge(newUserDetails);
        userDetailsRepository.save(newUserDetails);
    }

    public void setRoleUser(UserDto setRoleDto, int id){
        Optional<UserEntity> optionalUser = userRepository.findById(setRoleDto.getId());
        UserEntity user = optionalUser.get();
        String roleBefore = optionalUser.get().getRole().getRoleName();
        RoleEntity roleEntity = roleRepository.findByRoleName(setRoleDto.getRoleName()).orElse(null);
        user.setRole(roleEntity);
        RoleUpdateHistoryEntity roleUpdateHistory = new RoleUpdateHistoryEntity();
        roleUpdateHistory.setRoleBefore(roleBefore);
        roleUpdateHistory.setRoleAfter(user.getRole().getRoleName());

        //Adjust role badge
        badgeServices.adjustUserRoleBadge(roleBefore,user);


        LocalDate currentDate = LocalDate.now();
        roleUpdateHistory.setChangeDate(Date.valueOf(currentDate)); // Sử dụng ngày hiện tại
        UserEntity userEntity = userRepository.findById(id).orElse(null) ;
        roleUpdateHistory.setSetBy(userEntity); // Điền thông tin của người thực hiện cập nhật
        roleUpdateHistory.setUser(user);
        // Lưu thông tin cập nhật vào bảng role_update_history
        roleUpdateHistoryRepository.save(roleUpdateHistory);
        userRepository.save(user);

    }

    public void setMajorUser(int userID, int majorID) {
        Optional<UserEntity> optionalUser = userRepository.findById(userID);
        UserEntity user = optionalUser.get();
        UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.UNAUTHORIZED));
        MajorEntity majorBefore = userDetails.getMajor();
        MajorEntity majorEntity = majorRepository.findById(majorID).orElse(null) ;
        userDetails.setMajor(majorEntity);
        badgeServices.changeMajorBadge(userDetails,majorBefore);
        userDetailsRepository.save(userDetails);
    }

    public void banUser(UserDto userDto){
        Optional<UserEntity> optionalUser = userRepository.findByUsername(userDto.getUsername());
        UserEntity user = optionalUser.get();
        UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.UNAUTHORIZED));

        List<PendingReportEntity> pendingReportProfile = pendingReportRepository.findByContentIdAndReportType(user.getId(),"Profile");

        String reasonList = null;
        List<String> listCheck = new ArrayList<>();
        for (PendingReportEntity report : pendingReportProfile) {
            if (listCheck == null) {
                listCheck.add(report.getContent());
            }
            if(listCheck.contains(report.getContent())){
                listCheck.add(report.getContent());
                reasonList = report.getContent()+"\n";
            }
        }

        if (!pendingReportProfile.isEmpty()){
            deletePendingReportedProfile(user.getId());
        }

        userDetails.setBanned(true);

        userDetailsRepository.save(userDetails);
/*
        //send mail
        MailStructureDto mail = new MailStructureDto();
        mail.setTriggerId(user.getId());
        mail.setReceiverId(user.getId());
        mail.setMailType("Was-banned");
        mail.setPostLink(reasonList);
        notifyByMailServices.sendMail(mail);

 */
    }

    public void unbanUser(UserDto userDto){
        Optional<UserEntity> optionalUser = userRepository.findByUsername(userDto.getUsername());
        UserEntity user = optionalUser.get();
        UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.UNAUTHORIZED));
        userDetails.setBanned(false);
        userDetailsRepository.save(userDetails);
/*
        MailStructureDto mail = new MailStructureDto();
        mail.setTriggerId(user.getId());
        mail.setReceiverId(user.getId());
        mail.setMailType("unban");
        mail.setPostLink("https://fblog.site");
        notifyByMailServices.sendMail(mail);

 */
    }

    public void muteUser(UserDto userDto, Timestamp muteDuration) {
        Optional<UserEntity> optionalUser = userRepository.findByUsername(userDto.getUsername());
        UserEntity user = optionalUser.get();
        UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.UNAUTHORIZED));

        userDetails.setMuted(true);
        Timestamp timespan = new Timestamp(muteDuration.getTime());
        userDetails.setMutetime(timespan);
        userDetailsRepository.save(userDetails);
        List<PendingReportEntity> pendingReportProfile = pendingReportRepository.findByContentIdAndReportType(user.getId(),"Profile");

        String reasonList = null;
        List<String> listCheck = new ArrayList<>();
        for (PendingReportEntity report : pendingReportProfile) {
            if (listCheck == null) {
                listCheck.add(report.getContent());
            }
            if(listCheck.contains(report.getContent())){
                listCheck.add(report.getContent());
                reasonList = report.getContent()+"\n";
            }
        }

        if (!pendingReportProfile.isEmpty()){
            deletePendingReportedProfile(user.getId());
        }
        /*
        MailStructureDto mail = new MailStructureDto();
        mail.setTriggerId(user.getId());
        mail.setReceiverId(user.getId());
        mail.setMailType("Was-muted");
        mail.setPostLink(reasonList);
        notifyByMailServices.sendMail(mail);

         */
    }

    public void unmuteUser(UserDto userDto) {
        Optional<UserEntity> optionalUser = userRepository.findByUsername(userDto.getUsername());
        UserEntity user = optionalUser.get();
        UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.UNAUTHORIZED));
        userDetails.setMuted(false);
        userDetails.setMutetime(null);
        userDetailsRepository.save(userDetails);
        /*
        MailStructureDto mail = new MailStructureDto();
        mail.setTriggerId(user.getId());
        mail.setReceiverId(user.getId());
        mail.setMailType("unmute");
        mail.setPostLink("https://fblog.site");
        notifyByMailServices.sendMail(mail);

         */
    }

    public List<ReportedProfileDto> viewReportProfile(){
        List<UserEntity> userList = userRepository.findAll();

        List<ReportedProfileDto> reportProfiles = new ArrayList<>();

        for (UserEntity user : userList) {
            List<PendingReportEntity> pendingReport = pendingReportRepository.findByContentIdAndReportType(user.getId(), "Profile");
            if (!pendingReport.isEmpty()){
                UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                ReportedProfileDto reportedProfileDto = new ReportedProfileDto(user.getId(), userDetails.getFullName(), userDetails.getProfileURL(), listOfReportReason(user.getId()));
                reportProfiles.add(reportedProfileDto);
            }
        }
        return reportProfiles;
    }

    public List<String> listOfReportReason (int contentId) {
        List<PendingReportEntity> reports = pendingReportRepository.findByContentIdAndReportType(contentId, "Profile");

        List<String> reportReasonOfProfile = new ArrayList<>();

        for (PendingReportEntity pendingReport: reports) {
            PendingReportReasonEntity pendingReportReason = pendingReportReasonRepository.findByReportId(pendingReport.getId())
                    .orElseThrow(() -> new AppException("Unknown report", HttpStatus.NOT_FOUND));

            ReportReasonEntity reason = reportReasonRepository.findById(pendingReportReason.getReason().getId())
                    .orElseThrow(() -> new AppException("Unknown reason", HttpStatus.NOT_FOUND));

            if (!reportReasonOfProfile.contains(reason.getReasonName())) {
                reportReasonOfProfile.add(reason.getReasonName());
            }
        }
        return reportReasonOfProfile;
    }

    public List<ReportedCommentDto> viewPendingReportComment(){

        List<PendingReportEntity> reports = pendingReportRepository.findAll();

        List<ReportedCommentDto> reportComments = new ArrayList<>();

        for (PendingReportEntity pendingReport: reports) {
            if (pendingReport.getReportType().equalsIgnoreCase("Comment")){
                UserEntity user = userRepository.findById(pendingReport.getUser().getId())
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                        .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));

                PendingReportReasonEntity pendingReportReason = pendingReportReasonRepository.findByReportId(pendingReport.getId())
                        .orElseThrow(() -> new AppException("Unknown report", HttpStatus.NOT_FOUND));

                ReportReasonEntity reason = reportReasonRepository.findById(pendingReportReason.getReason().getId())
                        .orElseThrow(() -> new AppException("Unknown reason", HttpStatus.NOT_FOUND));

                ReportedCommentDto reportedCommentDto = new ReportedCommentDto(pendingReport.getId(), pendingReport.getDateOfReport().format(formatter), pendingReport.getReportType(),
                        pendingReport.getContentId(), pendingReport.getContent() , userDetails.getFullName(), reason.getReasonName());
                reportComments.add(reportedCommentDto);
            }
        }
        return reportComments;
    }

    public void deleteReportComment(int commentId){
        List<PendingReportEntity> pendingReports = pendingReportRepository.findByContentIdAndReportType(commentId,"Comment");

        if (pendingReports.isEmpty()){
           throw new AppException("Unknown reported comment", HttpStatus.NOT_FOUND);
        }

        if (pendingReports != null) {
            for (PendingReportEntity pendingReport: pendingReports) {
                PendingReportReasonEntity pendingReportReason = pendingReportReasonRepository.findByReportId(pendingReport.getId())
                        .orElseThrow(() -> new AppException("Unknown pending report reason", HttpStatus.NOT_FOUND));
                pendingReportReasonRepository.delete(pendingReportReason);
            }

            for (PendingReportEntity pendingReport: pendingReports) {
                pendingReportRepository.delete(pendingReport);
            }
        }
    }

    public void deletePendingReportedProfile(int userId){
        List<PendingReportEntity> pendingReports = pendingReportRepository.findByContentIdAndReportType(userId,"Profile");

        if (pendingReports.isEmpty()){
           throw new AppException("Unknown reported profile", HttpStatus.NOT_FOUND);
        }

        if (pendingReports != null) {
            for (PendingReportEntity pendingReport: pendingReports) {
                PendingReportReasonEntity pendingReportReason = pendingReportReasonRepository.findByReportId(pendingReport.getId())
                        .orElseThrow(() -> new AppException("Unknown pending report reason", HttpStatus.NOT_FOUND));
                pendingReportReasonRepository.delete(pendingReportReason);
            }

            for (PendingReportEntity pendingReport: pendingReports) {
                pendingReportRepository.delete(pendingReport);
            }
        }
    }
}
