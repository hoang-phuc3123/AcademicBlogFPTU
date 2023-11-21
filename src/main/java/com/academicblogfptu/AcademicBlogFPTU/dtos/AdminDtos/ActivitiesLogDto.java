package com.academicblogfptu.AcademicBlogFPTU.dtos.AdminDtos;

import com.academicblogfptu.AcademicBlogFPTU.entities.UserEntity;
import jdk.jfr.Timespan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ActivitiesLogDto {

    private Timestamp actionTime;
    private String action;
    private int userID;

    public ActivitiesLogDto(Timestamp actionTime, String action, UserEntity userID) {

    }
}
