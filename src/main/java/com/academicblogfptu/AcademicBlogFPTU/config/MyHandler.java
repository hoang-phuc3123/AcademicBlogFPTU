package com.academicblogfptu.AcademicBlogFPTU.config;

import com.academicblogfptu.AcademicBlogFPTU.dtos.NotificationDtos.NotificationDto;
import com.academicblogfptu.AcademicBlogFPTU.exceptions.AppException;
import com.academicblogfptu.AcademicBlogFPTU.services.UserServices;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
public class MyHandler extends TextWebSocketHandler {

    @Autowired
    private UserAuthProvider userAuthProvider;
    @Autowired
    private UserServices userService;

    private final Map<Integer, WebSocketSession> userSessions = new ConcurrentHashMap<>();


    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // Extract the JWT token from the session attributes
        String query = session.getUri().getQuery();
        String userIdString = Arrays.stream(query.split("&"))
                .filter(s -> s.startsWith("userId="))
                .findFirst()
                .map(s -> s.substring(7))
                .orElse(null);

        // Convert the user id to an integer
        Integer userId = Integer.parseInt(userIdString);

        // Store the session
        userSessions.put(userId, session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        // Remove the session
        userSessions.values().remove(session);
    }

    public void sendNotification(Integer userId, NotificationDto notificationDto) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                    // Create a new ObjectMapper instance
                ObjectMapper mapper = new ObjectMapper();
                    // Convert the NotificationEntity object to a JSON string
                String jsonMessage = mapper.writeValueAsString(notificationDto);
                    // Send the JSON string
                session.sendMessage(new TextMessage(jsonMessage));
            } catch (IOException e) {
                throw new AppException("Send realtime notify error", HttpStatus.NOT_FOUND);
            }
        }
    }

}
