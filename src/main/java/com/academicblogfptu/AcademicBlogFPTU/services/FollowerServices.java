package com.academicblogfptu.AcademicBlogFPTU.services;

import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos.FollowDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos.FollowerDto;
import com.academicblogfptu.AcademicBlogFPTU.entities.FollowerEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.UserDetailsEntity;
import com.academicblogfptu.AcademicBlogFPTU.exceptions.AppException;
import com.academicblogfptu.AcademicBlogFPTU.repositories.FollowerRepository;
import com.academicblogfptu.AcademicBlogFPTU.repositories.UserDetailsRepository;
import com.academicblogfptu.AcademicBlogFPTU.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowerServices {

    @Autowired
    private final FollowerRepository followerRepository;
    @Autowired
    private final UserDetailsRepository userDetailsRepository;
    @Autowired
    private final UserRepository userRepository;

    public List<FollowerDto> getFollower(Integer id){
        List<FollowerEntity> followers = followerRepository.findByUserId(id);
        return followers.stream().map(this::mapToFollowerDto).collect(Collectors.toList());
    }

    public List<FollowerDto> getFollowed(Integer id){
        List<FollowerEntity> followedUsers = followerRepository.findByFollowedBy(id);

        return followedUsers.stream().map(this::mapToFollowingDto).collect(Collectors.toList());
    }

    private FollowerDto mapToFollowingDto(FollowerEntity followerEntity) {

        //get user details of follower
        UserDetailsEntity userDetails = new UserDetailsEntity();
        userDetails = userDetailsRepository.findByUserId(followerEntity.getUser().getId());

        // convert entity to dto
        FollowerDto dto = new FollowerDto();
        dto.setId(userDetails.getUser().getId());
        dto.setFullName(userDetails.getFullName());
        dto.setProfileUrl(userDetails.getProfileURL());
        return dto;
    }
    private FollowerDto mapToFollowerDto(FollowerEntity followerEntity) {

        //get user details of follower
        UserDetailsEntity userDetails = new UserDetailsEntity();
        userDetails = userDetailsRepository.findByUserId(followerEntity.getFollowedBy());

        // convert entity to dto
        FollowerDto dto = new FollowerDto();
        dto.setId(userDetails.getUser().getId());
        dto.setFullName(userDetails.getFullName());
        dto.setProfileUrl(userDetails.getProfileURL());
        return dto;
    }

    public void follow(FollowDto followDto){
        Optional<FollowerEntity> followerEntity = followerRepository.findByUserIdAndFollowedBy(followDto.getUserId(),followDto.getFollowedBy());
        if(followerEntity.isEmpty()){
            FollowerEntity follower = new FollowerEntity();
            follower.setFollowedBy(followDto.getFollowedBy());
            follower.setUser(userRepository.findById(followDto.getUserId()).orElseThrow(() -> new AppException("Unknown user", HttpStatus.UNAUTHORIZED)));
            followerRepository.save(follower);
        }else{
            AppException ex = new AppException("already followed",HttpStatus.UNAUTHORIZED);
            throw ex;
        }


    }

    public void unfollow(FollowDto followDto){
        FollowerEntity followerEntity = followerRepository.findByUserIdAndFollowedBy(followDto.getUserId(),followDto.getFollowedBy()).orElseThrow(() -> new AppException("Not follow yet", HttpStatus.UNAUTHORIZED));
        followerRepository.delete(followerEntity);
    }




}
