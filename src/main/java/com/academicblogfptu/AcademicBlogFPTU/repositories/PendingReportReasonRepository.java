package com.academicblogfptu.AcademicBlogFPTU.repositories;

import com.academicblogfptu.AcademicBlogFPTU.entities.PendingReportEntity;
import com.academicblogfptu.AcademicBlogFPTU.entities.PendingReportReasonEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PendingReportReasonRepository extends JpaRepository<PendingReportReasonEntity, Integer> {
    Optional<PendingReportReasonEntity> findByReportId(Integer reportId);
}
