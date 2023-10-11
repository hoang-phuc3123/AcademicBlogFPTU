package com.academicblogfptu.AcademicBlogFPTU.entities;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="Users")
@Data
@NoArgsConstructor
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "Username", nullable = false)
    private String username;
    @Column(name = "Password", nullable = false)
    private String password;
    @Column(name = "Email", nullable = false ,columnDefinition = "VARCHAR(255)")
    private String email;

    @ManyToOne
    @JoinColumn(name = "RoleId", referencedColumnName = "id")
    private RoleEntity role;



}
