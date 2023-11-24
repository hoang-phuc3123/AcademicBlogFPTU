package com.academicblogfptu.AcademicBlogFPTU.services;

import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos.UserSkillsDto;
import com.academicblogfptu.AcademicBlogFPTU.entities.*;
import com.academicblogfptu.AcademicBlogFPTU.exceptions.AppException;
import com.academicblogfptu.AcademicBlogFPTU.repositories.SkillRepository;
import com.academicblogfptu.AcademicBlogFPTU.repositories.UserRepository;
import com.academicblogfptu.AcademicBlogFPTU.repositories.UserSkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SkillServices {
    @Autowired
    private final SkillRepository skillRepository;
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final UserSkillRepository userSkillRepository;

    public List<SkillEntity> getAll(){
        return skillRepository.findAll();
    }


    public void createSkill(SkillEntity skill) {

        if (skillRepository.existsBySkillName(skill.getSkillName())){
            throw new AppException("Skill existed",HttpStatus.IM_USED);
        }
        skillRepository.save(skill);
    }

    public SkillEntity updateSkill(SkillEntity skill) {
        SkillEntity existingSkill = skillRepository.findById(skill.getId())
                .orElseThrow(() -> new AppException("Unknown Skill", HttpStatus.NOT_FOUND));
        existingSkill.setSkillName(skill.getSkillName());
        return skillRepository.save(existingSkill);
    }

    public void deleteSkill(int id) {
        SkillEntity existingSkill = skillRepository.findById(id)
                .orElseThrow(() -> new AppException("Unknown skill", HttpStatus.NOT_FOUND));
        skillRepository.deleteById(id);
    }

    public void setUserSkills(UserSkillsDto userSkillsDto) {
        UserEntity user = userRepository.findById(userSkillsDto.getUserId()).orElseThrow(()-> new AppException("Unknown user",HttpStatus.NOT_FOUND));
        List<Integer> listSkills = userSkillsDto.getSkillList();
        for (Integer skillId: listSkills) {
            SkillEntity skill = skillRepository.findById(skillId).orElseThrow(()-> new AppException("Unknown skill",HttpStatus.NOT_FOUND));
            if(!userSkillRepository.existsByUserIdAndSkillId(userSkillsDto.getUserId(), skill.getId())){
                UserSkillEntity userSkill = new UserSkillEntity();
                userSkill.setUser(user);
                userSkill.setSkill(skill);
                userSkillRepository.save(userSkill);
            }
        }
    }

    public void removeUserSkill(UserSkillsDto userSkillsDto){
        UserSkillEntity userSkill = userSkillRepository.findByUserIdAndSkillId(userSkillsDto.getUserId(),userSkillsDto.getSkillId()).orElseThrow(()-> new AppException("Unknown user",HttpStatus.NOT_FOUND));
        try{
            userSkillRepository.delete(userSkill);
        }catch (Exception e){
            throw new AppException(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public List<SkillEntity> findSkillByUser(UserEntity user) {
        List<UserSkillEntity> userSkill = userSkillRepository.findByUser(user);
        List<SkillEntity> skills = userSkill.stream()
                .map(UserSkillEntity::getSkill)
                .collect(Collectors.toList());
        return skills;
    }
}
