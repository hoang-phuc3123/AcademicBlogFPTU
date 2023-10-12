package com.academicblogfptu.AcademicBlogFPTU.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_details")
@Data
@NoArgsConstructor
public class UserDetailsEntity {

    @Id
    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "fullname")
    private String fullName;

    @Column(name = "phone")
    private String phone;

    @Column(name = "is_banned")
    private boolean isBanned;

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
    private UserEntity userid;


    @ManyToOne
    @JoinColumn(name = "major_id", referencedColumnName = "id" )
    private MajorEntity major;

}