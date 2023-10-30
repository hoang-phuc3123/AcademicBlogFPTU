package com.academicblogfptu.AcademicBlogFPTU.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="badge")
@Data
@NoArgsConstructor
public class BadgeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "badge_name")
    private String badgeName;

}