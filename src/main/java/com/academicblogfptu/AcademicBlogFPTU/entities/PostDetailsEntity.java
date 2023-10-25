package com.academicblogfptu.AcademicBlogFPTU.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "post_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostDetailsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private LocalDateTime dateOfAction;

    private String type;

    private String reasonOfDeclination;

    @ManyToOne
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "post_id", referencedColumnName = "id")
    private PostEntity post;

}
