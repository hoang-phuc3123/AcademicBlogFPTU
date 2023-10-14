package com.academicblogfptu.AcademicBlogFPTU.services;

import com.academicblogfptu.AcademicBlogFPTU.dtos.*;
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

    public UserDto register(RegisterDto registerDto) {
        Optional<UserEntity> optionalUser = userRepository.findByUsername(registerDto.getUsername());
        if (optionalUser.isPresent()) {
            // Nếu tìm thấy người dùng, trả về thông tin người dùng hiện tại
            UserEntity user = optionalUser.get();
            UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                    .orElseThrow(() -> new AppException("Unknown user", HttpStatus.UNAUTHORIZED));
            return new UserDto(user.getId(), user.getUsername(), userDetails.isBanned(), userDetails.isMuted(), user.getRole().getRoleName(), "");
        } else {
            // Nếu không tìm thấy, tạo một tài khoản mới và trả về thông tin của tài khoản mới
            UserEntity newUser = new UserEntity();
            newUser.setUsername(registerDto.getUsername());
            newUser.setPassword(passwordEncoder.encode(CharBuffer.wrap(registerDto.getPassword())));
            RoleEntity roleEntity = roleRepository.findByRoleName(registerDto.getRole()).orElse(null);
            newUser.setRole(roleEntity);
            userRepository.save(newUser);
            // Tạo UserDto từ tài khoản mới và trả về
            return new UserDto(newUser.getId(), newUser.getUsername(), false,false, newUser.getRole().getRoleName(), "");
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
        newUserDetails.setWeightOfReport(0);
        newUserDetails.setProfileURL(null);
        newUserDetails.setCoverURL(null);
        newUserDetails.setUserStory(null);
        newUserDetails.setUserid(user);
        MajorEntity majorEntity = majorRepository.findByMajorName("CÔNG NGHỆ THÔNG TIN").orElse(null) ;
        newUserDetails.setMajor(majorEntity);
        userDetailsRepository.save(newUserDetails);
    }

}
