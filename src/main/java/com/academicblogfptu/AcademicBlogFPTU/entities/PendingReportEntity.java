package com.academicblogfptu.AcademicBlogFPTU.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;

import java.sql.Date;

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

    private Date dateOfReport;

    private String reportType;

    @ManyToOne
    @JoinColumn(name = "comment_id")
    private CommentEntity comment;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private UserEntity user;

}
