package com.academicblogfptu.AcademicBlogFPTU.entities;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "account_badge")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBadgeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "badge_id")
    private BadgeEntity badge;
}
