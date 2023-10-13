package com.academicblogfptu.AcademicBlogFPTU.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="bagde")
@Data
@NoArgsConstructor
public class BadgeEntity {
    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "bagde_name")
    private String badgeName;

}
