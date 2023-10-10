package com.academicblogfptu.AcademicBlogFPTU.services;

import com.academicblogfptu.AcademicBlogFPTU.dtos.LoginRequestDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDto;
import com.academicblogfptu.AcademicBlogFPTU.entities.UserEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.RoleEntity;
import com.academicblogfptu.AcademicBlogFPTU.exceptions.AppException;
import com.academicblogfptu.AcademicBlogFPTU.repositories.RoleRepository;
import com.academicblogfptu.AcademicBlogFPTU.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;


import java.nio.CharBuffer;

@RequiredArgsConstructor
@Service
public class UserServices {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

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

    public UserDto login(LoginRequestDto loginDto) {
        UserEntity user = userRepository.findByUsername(loginDto.getUsername())
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));

        if (passwordEncoder.matches(CharBuffer.wrap(loginDto.getPassword()), user.getPassword())) {
            UserDto userDto = new UserDto(user.getId(),user.getUsername(),user.getRole().getRoleName(),"");
            return userDto;
        }
        throw new AppException("Invalid password", HttpStatus.NOT_FOUND);
    }

    public UserDto resetPass(LoginRequestDto loginDto) {
        UserEntity user = userRepository.findByUsername(loginDto.getUsername())
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));

        String newPasswordHash = passwordEncoder.encode(CharBuffer.wrap(loginDto.getPassword()));

        // Cập nhật mật khẩu của người dùng
        user.setPassword(newPasswordHash);
        userRepository.save(user);

        // Trả về thông tin người dùng sau khi cập nhật
        UserDto userDto = new UserDto(user.getId(), user.getUsername(), user.getRole().getRoleName(), "");
        return userDto;
    }

}
