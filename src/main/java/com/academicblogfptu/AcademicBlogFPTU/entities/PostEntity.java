package com.academicblogfptu.AcademicBlogFPTU.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "post")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String title;
    @Column(name = "description")
    private String description;
    private String content;
    private LocalDateTime dateOfPost;
    private Integer numOfUpvote;
    private Integer numOfDownvote;
    private boolean isRewarded;
    private boolean isEdited;
    private boolean allowComment;
    private int length;

    @Column(name = "slug")
    private String slug;

    @Column(name = "cover_URL")
    private String coverURL;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private CategoryEntity category;

    @ManyToOne
    @JoinColumn(name = "tag_id")
    private TagEntity tag;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private PostEntity parentPost;

}

