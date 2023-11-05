package com.academicblogfptu.AcademicBlogFPTU.entities;
import jakarta.persistence.*;
import lombok.*;
import java.sql.Date;
import java.time.LocalDateTime;

@Entity
@Table(name = "news")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String title;

    private String content;

    private LocalDateTime newsAt;

    @ManyToOne
    @JoinColumn(name = "sent_by")
    private UserEntity user;


}
