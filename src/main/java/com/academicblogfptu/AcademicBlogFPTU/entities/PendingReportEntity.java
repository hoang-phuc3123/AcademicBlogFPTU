package com.academicblogfptu.AcademicBlogFPTU.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "pending_report")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PendingReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String content;

    private LocalDateTime dateOfReport;

    private String reportType;


    private int contentId;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private UserEntity user;

}
