package com.academicblogfptu.AcademicBlogFPTU.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "token_list")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Lob // Sử dụng @Lob cho kiểu dữ liệu VARCHAR(MAX)
    @Column(name = "token", nullable = false)
    private String token;
}
