package com.academicblogfptu.AcademicBlogFPTU.services;

import com.academicblogfptu.AcademicBlogFPTU.entities.SkillEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.TagEntity;
import com.academicblogfptu.AcademicBlogFPTU.exceptions.AppException;
import com.academicblogfptu.AcademicBlogFPTU.repositories.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SkillServices {
    @Autowired
    private SkillRepository skillRepository;

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
}
