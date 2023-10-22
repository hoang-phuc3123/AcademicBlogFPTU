package com.academicblogfptu.AcademicBlogFPTU.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;

import java.sql.Date;
import java.sql.Timestamp;

@Entity
@Table(name = "notification")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String content;

    private int relatedId;

    private boolean isRead;

    private Timestamp notifyAt;

    private String type;

    private int triggerUser;

    @Column(name = "related_URL")
    private String relatedURL;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private UserEntity user;

}
