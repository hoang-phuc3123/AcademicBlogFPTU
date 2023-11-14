package com.academicblogfptu.AcademicBlogFPTU.controllers;

import com.academicblogfptu.AcademicBlogFPTU.config.UserAuthProvider;
import com.academicblogfptu.AcademicBlogFPTU.dtos.MailStructureDto;
import com.academicblogfptu.AcademicBlogFPTU.services.NotifyByMailServices;
import com.academicblogfptu.AcademicBlogFPTU.services.UserServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mail")
public class SendMailController {

    @Autowired
    private UserServices userService;
    @Autowired
    private UserAuthProvider userAuthProvider;

    @Autowired
    private NotifyByMailServices notifyByMailServices;

    @PostMapping("/send-notify")
    public ResponseEntity<String> sendNotifyByMail(@RequestHeader("Authorization") String headerValue, @RequestBody MailStructureDto mailStructureDto){
        mailStructureDto.setTriggerId(userService.findByUsername(
                userAuthProvider.getUser(
                        headerValue.replace("Bearer ", "")
                )).getId());
        notifyByMailServices.sendMail(mailStructureDto);
        return ResponseEntity.ok("Success");
    }
}
