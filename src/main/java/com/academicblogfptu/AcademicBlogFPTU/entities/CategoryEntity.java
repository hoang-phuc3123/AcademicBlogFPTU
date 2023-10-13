package com.academicblogfptu.AcademicBlogFPTU.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;

@Entity
@Table(name = "category")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String categoryName;

    private String categoryType;

    private int parentID;

    @ManyToOne
    @JoinColumn(name = "major_id")
    private MajorEntity major;

}

