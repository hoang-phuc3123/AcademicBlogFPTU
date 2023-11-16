package com.academicblogfptu.AcademicBlogFPTU.services;

import com.academicblogfptu.AcademicBlogFPTU.dtos.CategoryAndTagDtos.TagDto;
import com.academicblogfptu.AcademicBlogFPTU.entities.TagEntity;
import com.academicblogfptu.AcademicBlogFPTU.exceptions.AppException;
import com.academicblogfptu.AcademicBlogFPTU.repositories.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TagServices {

    @Autowired
    private TagRepository tagRepository;

    public List<TagEntity> getAllTags(){
        return tagRepository.findAll();
    }

    public Optional<TagEntity> getTagById(int Id){
        return tagRepository.findById(Id);
    }

    public TagEntity createTag(TagDto tagDto) {
        TagEntity tagEntity = new TagEntity();
        if(tagRepository.existsByTagName(tagDto.getTagName())){
            throw new AppException("Tag existed!",HttpStatus.IM_USED);
        }
        tagEntity.setTagName(tagDto.getTagName());
        return tagRepository.save(tagEntity);
    }

    public TagEntity updateTag(TagDto updatedTag) {
        TagEntity existingTag = tagRepository.findById(updatedTag.getTagId())
                .orElseThrow(() -> new AppException("Unknown tag", HttpStatus.UNAUTHORIZED));
        existingTag.setTagName(updatedTag.getTagName());
        return tagRepository.save(existingTag);
    }
    public void deleteTag(int id) {

        TagEntity existingTag = tagRepository.findById(id)
                .orElseThrow(() -> new AppException("Unknown tag", HttpStatus.UNAUTHORIZED));
        tagRepository.deleteById(id);
    }
}

