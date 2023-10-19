package com.academicblogfptu.AcademicBlogFPTU.services;

import com.academicblogfptu.AcademicBlogFPTU.dtos.FollowerDto;
import com.academicblogfptu.AcademicBlogFPTU.entities.FollowerEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.UserDetailsEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.UserEntity;
import com.academicblogfptu.AcademicBlogFPTU.repositories.FollowerRepository;
import com.academicblogfptu.AcademicBlogFPTU.repositories.UserDetailsRepository;
import com.academicblogfptu.AcademicBlogFPTU.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
        dto.setId(followerEntity.getId());
        dto.setFullName(userDetails.getFullName());
        dto.setProfileUrl(userDetails.getProfileURL());
        return dto;
    }




}
