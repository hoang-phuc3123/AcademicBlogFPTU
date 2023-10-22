package com.academicblogfptu.AcademicBlogFPTU.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "follower")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FollowerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "followed_by")
    private Integer followedBy;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private UserEntity user;



}
