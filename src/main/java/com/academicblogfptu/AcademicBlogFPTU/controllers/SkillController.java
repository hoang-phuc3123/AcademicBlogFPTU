package com.academicblogfptu.AcademicBlogFPTU.controllers;


import com.academicblogfptu.AcademicBlogFPTU.config.UserAuthProvider;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos.UserDto;
import com.academicblogfptu.AcademicBlogFPTU.entities.SkillEntity;
import com.academicblogfptu.AcademicBlogFPTU.services.SkillServices;
import com.academicblogfptu.AcademicBlogFPTU.services.UserServices;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class SkillController {

    @Autowired
    private final SkillServices skillServices;

    @Autowired
    private final UserServices userService;

    @Autowired
    private final UserAuthProvider userAuthProvider;

    public boolean isAdmin(UserDto userDto) {
        return userDto.getRoleName().equals("admin");
    }

    @GetMapping("/skills")
    public ResponseEntity<List<SkillEntity>> getAllSkill(){
        List<SkillEntity> skillList = skillServices.getAll();
        return ResponseEntity.ok(skillList);
    }

    @PostMapping("/admin/add-skill")
    public ResponseEntity<String> addSkill(@RequestHeader("Authorization") String headerValue, @RequestBody SkillEntity skill){
        if (isAdmin(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            skillServices.createSkill(skill);
            return ResponseEntity.ok("Success");
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/admin/edit-skill")
    public ResponseEntity<SkillEntity> updateSkill(@RequestHeader("Authorization") String headerValue, @RequestBody SkillEntity skill) {
        if (isAdmin(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            SkillEntity updatedSkill = new SkillEntity();
            updatedSkill = skillServices.updateSkill(skill);
            return ResponseEntity.ok(updatedSkill);
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/admin/delete-skill")
    public ResponseEntity<String> deleteSkill(@RequestHeader("Authorization") String headerValue, @RequestBody SkillEntity skill) {
        if (isAdmin(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            try{
                skillServices.deleteSkill(skill.getId());
            }catch (Exception e){
                if(e.getMessage().equals("could not execute statement; SQL [n/a]; constraint [null]")){
                    return new ResponseEntity<>("This skill has at least 1 usage",HttpStatus.CONFLICT);
                }
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            return ResponseEntity.ok("Success");
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

}
