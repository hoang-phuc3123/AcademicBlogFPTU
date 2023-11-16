package com.academicblogfptu.AcademicBlogFPTU.entities;

import jakarta.persistence.*;
import jdk.jfr.Timespan;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.sql.Timestamp;

@Entity
@Table(name = "user_details")
@Data
@NoArgsConstructor
public class UserDetailsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "email")
    private String email;

    @Column(name = "fullname")
    private String fullName;

    @Column(name = "phone")
    private String phone;

    @Column(name = "is_banned")
    private boolean isBanned;

    @Column(name = "is_muted")
    private boolean isMuted;

    @Column(name = "mute_time")
    private Timestamp mutetime;

    @Column(name = "weight_of_report")
    private int weightOfReport;

    @Column(name = "profile_URL")
    private String profileURL;

    @Column(name = "cover_URL")
    private String coverURL;

    @Column(name = "user_story")
    private String userStory;

    @OneToOne
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "major_id", referencedColumnName = "id", nullable = true)
    private MajorEntity major;
}