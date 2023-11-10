package com.academicblogfptu.AcademicBlogFPTU.services;

import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos.*;
import com.academicblogfptu.AcademicBlogFPTU.entities.UserDetailsEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.UserEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.RoleEntity;
import com.academicblogfptu.AcademicBlogFPTU.exceptions.AppException;
import com.academicblogfptu.AcademicBlogFPTU.repositories.MajorRepository;
import com.academicblogfptu.AcademicBlogFPTU.repositories.RoleRepository;
import com.academicblogfptu.AcademicBlogFPTU.repositories.UserDetailsRepository;
import com.academicblogfptu.AcademicBlogFPTU.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.nio.CharBuffer;

@RequiredArgsConstructor
@Service
public class UserServices {

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

    public UserDto findByUsername(String Username) {
        UserEntity user = userRepository.findByUsername(Username)
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.UNAUTHORIZED));
        UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.UNAUTHORIZED));
        return new UserDto(user.getId(),user.getUsername(),userDetails.getFullName(),userDetails.isBanned(),userDetails.isMuted(),userDetails.getMutetime(),user.getRole().getRoleName(),userDetails.getProfileURL(), userDetails.getCoverURL() ,"" , "");
    }

    public UserDetailsDto findByEmail(String email) {
        UserDetailsEntity user = userDetailsRepository.findByEmail(email)
                .orElseThrow(() -> new AppException("Unknown email", HttpStatus.UNAUTHORIZED));
        return new UserDetailsDto(user.getEmail(), user.getFullName(), user.getCoverURL());
    }

    public UserDto login(LoginRequestDto loginDto) {
        UserEntity user = userRepository.findByUsername(loginDto.getUsername())
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.UNAUTHORIZED));
        UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.UNAUTHORIZED));
        if (passwordEncoder.matches(CharBuffer.wrap(loginDto.getPassword()), user.getPassword())) {
            return new UserDto(user.getId(),user.getUsername(), userDetails.getFullName(), userDetails.isBanned(), userDetails.isMuted(),userDetails.getMutetime(),user.getRole().getRoleName(), userDetails.getProfileURL(), userDetails.getCoverURL(),"" , "");
        }
        throw new AppException("Invalid password", HttpStatus.UNAUTHORIZED);
    }

    public String isEmailExist(String email) {
        UserDetailsEntity user = userDetailsRepository.findByEmail(email)
                .orElseThrow(() -> new AppException("Unknown email", HttpStatus.UNAUTHORIZED));
        return "true";
    }

    public UserDto register(LoginRequestDto registerDto) {
        Optional<UserEntity> optionalUser = userRepository.findByUsername(registerDto.getUsername());
        if (optionalUser.isPresent()) {
            // Nếu tìm thấy người dùng, trả về thông tin người dùng hiện tại
            UserEntity user = optionalUser.get();
            UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                    .orElseThrow(() -> new AppException("Unknown user", HttpStatus.UNAUTHORIZED));
            return new UserDto(user.getId(), user.getUsername(), userDetails.getFullName(),userDetails.isBanned(), userDetails.isMuted(), userDetails.getMutetime(), user.getRole().getRoleName(), userDetails.getProfileURL(),userDetails.getCoverURL(), "" ,"");
        } else {
            // Nếu không tìm thấy, tạo một tài khoản mới và trả về thông tin của tài khoản mới
            UserEntity newUser = new UserEntity();
            newUser.setUsername(registerDto.getUsername());
            newUser.setPassword(passwordEncoder.encode(CharBuffer.wrap(registerDto.getPassword())));
            RoleEntity roleEntity = roleRepository.findByRoleName("Student").orElse(null);
            newUser.setRole(roleEntity);
            userRepository.save(newUser);
            // Tạo UserDto từ tài khoản mới và trả về
            return new UserDto(newUser.getId(), newUser.getUsername(), "" ,false,false, null ,newUser.getRole().getRoleName(), "", "", "", "");
        }
    }

    public void RegisterUserDetail(UserDetailsDto userDetailsDto){
        Optional<UserEntity> optionalUser = userRepository.findByUsername(userDetailsDto.getEmail());
        UserEntity user = optionalUser.get();
        Optional<UserDetailsEntity> optionalUserDetailsEntity = userDetailsRepository.findByUserAccount(user);
        if (optionalUserDetailsEntity.isPresent()) {
            optionalUserDetailsEntity.get().setFullName(userDetailsDto.getGivenName());
        }
        else {
            UserDetailsEntity newUserDetails = new UserDetailsEntity();
            newUserDetails.setEmail(userDetailsDto.getEmail());
            newUserDetails.setFullName(userDetailsDto.getGivenName());
            newUserDetails.setPhone(null);
            newUserDetails.setBanned(false);
            newUserDetails.setWeightOfReport(0);
            newUserDetails.setProfileURL(userDetailsDto.getProfileUrl());
            newUserDetails.setUser(user);
            //MajorEntity majorEntity = majorRepository.findByMajorName("Kỹ Thuật Phần Mềm").orElse(null) ;
            newUserDetails.setMajor(null);
            userDetailsRepository.save(newUserDetails);
        }
    }

    public UserDto resetPass(ResetPasswordDto resetPasswordDto) {
        UserDetailsEntity user = userDetailsRepository.findByEmail(resetPasswordDto.getEmail())
                .orElseThrow(() -> new AppException("Unknown email", HttpStatus.UNAUTHORIZED));
        String newPasswordHash = passwordEncoder.encode(CharBuffer.wrap(resetPasswordDto.getPassword()));
        // Cập nhật mật khẩu của người dùng
        Optional<UserEntity> newUser = userRepository.findById(user.getUser().getId()); // Tìm UserEntity có ID = 1
        if (newUser.isPresent()) {
            UserEntity user_ = newUser.get();
            user_.setPassword(newPasswordHash);
            userRepository.save(user_);
            // Trả về thông tin người dùng sau khi cập nhật
            return new UserDto(user_.getId(),user_.getUsername(),"",user.isBanned(), user.isMuted(), user.getMutetime(),user_.getRole().getRoleName(), user.getProfileURL(),user.getCoverURL(),"", "");
        }
        else {
            return new UserDto();
        }
    }

    public void changePassword(ChangePasswordDto changePasswordDto){
        UserEntity user = userRepository.findById(changePasswordDto.getUserId())
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.UNAUTHORIZED));
        if(!passwordEncoder.matches(CharBuffer.wrap(changePasswordDto.getOldPassword()), user.getPassword())){
            throw new AppException("Password not match",HttpStatus.UNAUTHORIZED);
        }else{
            user.setPassword(passwordEncoder.encode(CharBuffer.wrap(changePasswordDto.getNewPassword())));
            userRepository.save(user);
        }
    }



}