package com.academicblogfptu.AcademicBlogFPTU.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;

@Entity
@Table(name = "pending_report_reason")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PendingReportReasonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "report_id")
    private PendingReportEntity report;

    @ManyToOne
    @JoinColumn(name = "reason_id")
    private ReportReasonEntity reason;

}
