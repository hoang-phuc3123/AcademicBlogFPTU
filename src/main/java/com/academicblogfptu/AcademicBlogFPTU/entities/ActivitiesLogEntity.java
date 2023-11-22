package com.academicblogfptu.AcademicBlogFPTU.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity
@Table(name = "activities_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivitiesLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "action_time" , nullable = false)
    private Timestamp actionTime;

    @Lob
    @Column(name = "action" , nullable = false)
    private String action;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private UserEntity user;
}
