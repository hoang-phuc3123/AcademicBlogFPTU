package com.academicblogfptu.AcademicBlogFPTU.entities;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

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

    @Lob
    @Column(name = "refresh_token" , nullable = false)
    private String refreshToken;

    @Column(name = "expired_time" , nullable = false)
    private Timestamp expiredTime;
}
