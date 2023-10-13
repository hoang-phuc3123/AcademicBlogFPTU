package com.academicblogfptu.AcademicBlogFPTU.entities;

import lombok.Data;

import jakarta.persistence.*;
import java.sql.Date;

@Data
@Entity
@Table(name = "favorite_post")
public class FavoritePostEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "save_at", nullable = false)
    private Date saveAt;

    @Column(name = "post_id")
    private int postId;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private UserEntity user;

}
