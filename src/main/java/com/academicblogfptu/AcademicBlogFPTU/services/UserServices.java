package com.academicblogfptu.AcademicBlogFPTU.services;

import com.academicblogfptu.AcademicBlogFPTU.dtos.LoginRequestDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDetailsDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDto;
import com.academicblogfptu.AcademicBlogFPTU.entities.MajorEntity;
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
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
        UserDto userDto = new UserDto(user.getId(),user.getUsername(),user.getRole(),"");
        return userDto;
    }

    public UserDto register(LoginRequestDto registerDto) {
        Optional<UserEntity> optionalUser = userRepository.findByUsername(registerDto.getUsername());

        if (optionalUser.isPresent()) {
            // Nếu tìm thấy người dùng, trả về thông tin người dùng hiện tại
            UserEntity user = optionalUser.get();
            UserDto userDto = new UserDto(user.getId(), user.getUsername(), user.getRole().getRoleName(), "");
            return userDto;
        } else {
            // Nếu không tìm thấy, tạo một tài khoản mới và trả về thông tin của tài khoản mới
            UserEntity newUser = new UserEntity();
            newUser.setUsername(registerDto.getUsername());
            newUser.setPassword(passwordEncoder.encode(CharBuffer.wrap(registerDto.getPassword())));
            RoleEntity roleEntity = roleRepository.findByRoleName("Student").orElse(null);
            newUser.setRole(roleEntity);
            // Đặt các giá trị khác của newUser theo đúng logic của bạn
            // Lưu tài khoản mới vào cơ sở dữ liệu
            userRepository.save(newUser);
            // Tạo UserDto từ tài khoản mới và trả về
            UserDto newUserDto = new UserDto(newUser.getId(), newUser.getUsername(), newUser.getRole().getRoleName(), "");
            return newUserDto;
        }
    }

    public void RegisterUserDetail(UserDetailsDto userDetailsDto){
        Optional<UserEntity> optionalUser = userRepository.findByUsername(userDetailsDto.getEmail());
        UserEntity user = optionalUser.get();

        UserDetailsEntity newUserDetails = new UserDetailsEntity();
        newUserDetails.setEmail(userDetailsDto.getEmail());
        newUserDetails.setFullName(userDetailsDto.getGivenName());
        newUserDetails.setPhone(null);


        newUserDetails.setBanned(false);
        newUserDetails.setWeightOfReport(0);
        newUserDetails.setProfileURL(userDetailsDto.getProfileUrl());
        newUserDetails.setCoverURL(null);
        newUserDetails.setUserStory(null);
        newUserDetails.setUserid(user);
        MajorEntity majorEntity = majorRepository.findByMajorName("IT").orElse(null) ;
        newUserDetails.setMajor(majorEntity);

        userDetailsRepository.save(newUserDetails);

    }



    public UserDto login(LoginRequestDto loginDto) {
        UserEntity user = userRepository.findByUsername(loginDto.getUsername())
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));

        if (passwordEncoder.matches(CharBuffer.wrap(loginDto.getPassword()), user.getPassword())) {
            UserDto userDto = new UserDto(user.getId(),user.getUsername(),user.getRole().getRoleName(),"");
            return userDto;
        }
        throw new AppException("Invalid password", HttpStatus.BAD_REQUEST);
    }

}
