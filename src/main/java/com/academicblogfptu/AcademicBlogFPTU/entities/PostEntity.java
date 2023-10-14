package com.academicblogfptu.AcademicBlogFPTU.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;

import java.sql.Date;

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

    private String content;

    private Date dateOfPost;

    private int numOfUpvote;

    private int numOfDownvote;

    private boolean isRewarded;

    private boolean isEdited;

    private boolean allowComment;

    private int length;

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
