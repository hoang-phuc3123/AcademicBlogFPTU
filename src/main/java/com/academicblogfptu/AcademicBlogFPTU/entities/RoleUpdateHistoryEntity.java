package com.academicblogfptu.AcademicBlogFPTU.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.sql.Date;
import jakarta.persistence.*;

@Entity
@Table(name = "role_update_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleUpdateHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String roleBefore;

    private String roleAfter;

    private Date changeDate;

    @ManyToOne
    @JoinColumn(name = "set_by")
    private UserEntity setBy;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private UserEntity user;

}
