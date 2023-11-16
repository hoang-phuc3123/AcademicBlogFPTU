package com.academicblogfptu.AcademicBlogFPTU.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_skill")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostSkillEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private PostEntity post;

    @ManyToOne
    @JoinColumn(name = "skill_id")
    private SkillEntity skill;
}
