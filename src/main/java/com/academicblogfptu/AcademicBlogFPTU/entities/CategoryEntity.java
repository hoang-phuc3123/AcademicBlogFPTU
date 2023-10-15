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

    @Column(name = "category_name")
    private String categoryName;

    @Column(name = "category_type")
    private String categoryType;

    @Column(name = "parent_id")
    private Integer parentID;

    @ManyToOne
    @JoinColumn(name = "major_id")
    private MajorEntity major;
}

