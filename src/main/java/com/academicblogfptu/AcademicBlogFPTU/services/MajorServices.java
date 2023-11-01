package com.academicblogfptu.AcademicBlogFPTU.services;

import com.academicblogfptu.AcademicBlogFPTU.dtos.MajorDtos.MajorDto;
import com.academicblogfptu.AcademicBlogFPTU.entities.MajorEntity;
import com.academicblogfptu.AcademicBlogFPTU.exceptions.AppException;
import com.academicblogfptu.AcademicBlogFPTU.repositories.MajorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MajorServices {
    @Autowired
    private MajorRepository majorRepository;

    public List<MajorEntity> getAllMajors(){
        return majorRepository.findAll();
    }

    public MajorEntity createMajor(MajorDto MajorDto) {
        MajorEntity MajorEntity = new MajorEntity();
        MajorEntity.setMajorName(MajorDto.getMajorName());
        return majorRepository.save(MajorEntity);
    }

    public MajorEntity updateMajor(MajorDto updatedMajor) {
        MajorEntity existingMajor = majorRepository.findById(updatedMajor.getId())
                .orElseThrow(() -> new AppException("Unknown Major", HttpStatus.UNAUTHORIZED));
        existingMajor.setMajorName(updatedMajor.getMajorName());
        return majorRepository.save(existingMajor);
    }
    public void deleteMajor(int id) {

        MajorEntity existingMajor = majorRepository.findById(id)
                .orElseThrow(() -> new AppException("Unknown Major", HttpStatus.UNAUTHORIZED));
        majorRepository.deleteById(id);
    }

}
