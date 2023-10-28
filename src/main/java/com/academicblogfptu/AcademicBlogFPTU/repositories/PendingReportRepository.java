package com.academicblogfptu.AcademicBlogFPTU.repositories;

import com.academicblogfptu.AcademicBlogFPTU.entities.PendingReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PendingReportRepository extends JpaRepository<PendingReportEntity, Integer> {

    Optional<PendingReportEntity> findByContentIdAndReportType(Integer contentId,String reportType);
}
