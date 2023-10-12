package com.academicblogfptu.AcademicBlogFPTU.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "major")
@Data
@NoArgsConstructor
public class MajorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "major_name")
    private String majorName;

}