package com.academicblogfptu.AcademicBlogFPTU.entities;

import lombok.Data;

import jakarta.persistence.*;

@Data
@Entity
@Table(name = "report_reason")
public class ReportReasonEntity {
    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "reason_name", nullable = false, length = 50)
    private String reasonName;
}

