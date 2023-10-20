package com.academicblogfptu.AcademicBlogFPTU.repositories;

import com.academicblogfptu.AcademicBlogFPTU.entities.RoleUpdateHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleUpdateRepository extends JpaRepository<RoleUpdateHistoryEntity,Integer> {

}
