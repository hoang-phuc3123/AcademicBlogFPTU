package com.academicblogfptu.AcademicBlogFPTU.repositories;

import com.academicblogfptu.AcademicBlogFPTU.entities.ActivitiesLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ActivitiesLogRepository extends JpaRepository<ActivitiesLogEntity, Integer> {

    @Query("SELECT a.id, a.actionTime, a.action, u.fullName FROM ActivitiesLogEntity a JOIN UserDetailsEntity u ON a.user.id = u.user.id")
    List<Object[]> findAllActionsAndFullNames();
}
