package com.academicblogfptu.AcademicBlogFPTU.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;

import java.sql.Date;

@Entity
@Table(name = "comment")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String content;

    private Date dateOfComment;

    private int numOfUpvote;

    private int numOfDownvote;

    private boolean isEdited;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private CommentEntity parentComment;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private PostEntity post;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private UserEntity user;

}