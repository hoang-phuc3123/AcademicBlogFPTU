package com.academicblogfptu.AcademicBlogFPTU.controllers;


import com.academicblogfptu.AcademicBlogFPTU.config.UserAuthProvider;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos.UserDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos.UserSkillsDto;
import com.academicblogfptu.AcademicBlogFPTU.entities.SkillEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.UserEntity;
import com.academicblogfptu.AcademicBlogFPTU.repositories.SkillRepository;
import com.academicblogfptu.AcademicBlogFPTU.repositories.UserRepository;
import com.academicblogfptu.AcademicBlogFPTU.repositories.UserSkillRepository;
import com.academicblogfptu.AcademicBlogFPTU.services.SkillServices;
import com.academicblogfptu.AcademicBlogFPTU.services.UserServices;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RequiredArgsConstructor
@RestController
public class SkillController {

    @Autowired
    private final SkillServices skillServices;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSkillRepository skillRepository;

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

    @PostMapping("/admin/set-skills")
    public ResponseEntity<String> setUserSkill(@RequestHeader("Authorization") String headerValue,@RequestBody UserSkillsDto userSkillsDto){
        if (isAdmin(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            skillServices.setUserSkills(userSkillsDto);
            return ResponseEntity.ok("Success");
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/admin/remove-user-skill")
    public ResponseEntity<String> removeUserSkill(@RequestHeader("Authorization") String headerValue,@RequestBody UserSkillsDto userSkillsDto){
        if (isAdmin(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            skillServices.removeUserSkill(userSkillsDto);
            return ResponseEntity.ok("Success");
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/users/skills")
    public ResponseEntity<List<String>> getUserskill(@RequestHeader("Authorization") String headerValue) {
        String username = userAuthProvider.getUser(headerValue.replace("Bearer ", ""));
        Optional<UserEntity> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isPresent()) {
            UserEntity user = optionalUser.get();
            int userId = user.getId();
            List<Object[]> userSkills = skillRepository.getUserSkills(userId);

            List<String> skillList = new ArrayList<>();
            for (Object[] userSkill : userSkills) {
                String skillName = userSkill[1].toString();
                skillList.add(skillName);
            }
            return ResponseEntity.ok(skillList);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }


}
