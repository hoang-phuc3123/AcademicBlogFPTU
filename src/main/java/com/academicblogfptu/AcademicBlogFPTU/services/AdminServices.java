package com.academicblogfptu.AcademicBlogFPTU.services;

import com.academicblogfptu.AcademicBlogFPTU.dtos.*;
import com.academicblogfptu.AcademicBlogFPTU.entities.*;
import com.academicblogfptu.AcademicBlogFPTU.exceptions.AppException;
import com.academicblogfptu.AcademicBlogFPTU.repositories.*;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
//import java.util.Date;
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

    public List<UserEntity> getAllUsers(){
        return userRepository.findAll();
    }

    public UserDto findById(int id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.UNAUTHORIZED));
        UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.UNAUTHORIZED));
        return new UserDto(user.getId(),user.getUsername(),userDetails.isBanned(),userDetails.isMuted(),userDetails.getMutetime(),user.getRole().getRoleName(), "" , "");
    }

    public UserDto register(RegisterDto registerDto) {
        Optional<UserEntity> optionalUser = userRepository.findByUsername(registerDto.getUsername());
        if (optionalUser.isPresent()) {
            // Nếu tìm thấy người dùng, trả về thông tin người dùng hiện tại
            UserEntity user = optionalUser.get();
            UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                    .orElseThrow(() -> new AppException("Unknown user", HttpStatus.UNAUTHORIZED));
            return new UserDto(user.getId(), user.getUsername(), userDetails.isBanned(), userDetails.isMuted(),userDetails.getMutetime(), user.getRole().getRoleName(), "" , "");
        } else {
            // Nếu không tìm thấy, tạo một tài khoản mới và trả về thông tin của tài khoản mới
            UserEntity newUser = new UserEntity();
            newUser.setUsername(registerDto.getUsername());
            newUser.setPassword(passwordEncoder.encode(CharBuffer.wrap(registerDto.getPassword())));
            RoleEntity roleEntity = roleRepository.findByRoleName(registerDto.getRole()).orElse(null);
            newUser.setRole(roleEntity);
            userRepository.save(newUser);
            // Tạo UserDto từ tài khoản mới và trả về
            return new UserDto(newUser.getId(), newUser.getUsername(),false, false, null, newUser.getRole().getRoleName(), "", "");
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
        newUserDetails.setUserid(user);
        MajorEntity majorEntity = majorRepository.findByMajorName("CÔNG NGHỆ THÔNG TIN").orElse(null) ;
        newUserDetails.setMajor(majorEntity);
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
        LocalDate currentDate = LocalDate.now();
        roleUpdateHistory.setChangeDate(Date.valueOf(currentDate)); // Sử dụng ngày hiện tại
        UserEntity userEntity = userRepository.findById(id).orElse(null) ;
        roleUpdateHistory.setSetBy(userEntity); // Điền thông tin của người thực hiện cập nhật
        roleUpdateHistory.setUser(user);
        // Lưu thông tin cập nhật vào bảng role_update_history
        roleUpdateHistoryRepository.save(roleUpdateHistory);
        userRepository.save(user);

    }

    public void banUser(UserDto userDto){
        Optional<UserEntity> optionalUser = userRepository.findByUsername(userDto.getUsername());
        UserEntity user = optionalUser.get();
        UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.UNAUTHORIZED));
        userDetails.setBanned(true);
        userDetailsRepository.save(userDetails);
    }

    public void unbanUser(UserDto userDto){
        Optional<UserEntity> optionalUser = userRepository.findByUsername(userDto.getUsername());
        UserEntity user = optionalUser.get();
        UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.UNAUTHORIZED));
        userDetails.setBanned(false);
        userDetailsRepository.save(userDetails);
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
    }



    public void unmuteUser(UserDto userDto) {
        Optional<UserEntity> optionalUser = userRepository.findByUsername(userDto.getUsername());
        UserEntity user = optionalUser.get();
        UserDetailsEntity userDetails = userDetailsRepository.findByUserAccount(user)
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.UNAUTHORIZED));
        userDetails.setMuted(false);
        userDetails.setMutetime(null);
        userDetailsRepository.save(userDetails);
    }

}
