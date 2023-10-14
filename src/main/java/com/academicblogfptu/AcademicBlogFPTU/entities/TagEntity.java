package com.academicblogfptu.AcademicBlogFPTU.entities;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "tag")
public class TagEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "tag_name", nullable = false, length = 10)
    private String tagName;
}