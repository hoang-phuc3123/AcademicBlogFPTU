package com.academicblogfptu.AcademicBlogFPTU.controllers;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    @MessageMapping("/message")
    @SendTo("/specific/messages")
    public String processMessage(String message) {
        return "Processed: " + message;
    }
}
