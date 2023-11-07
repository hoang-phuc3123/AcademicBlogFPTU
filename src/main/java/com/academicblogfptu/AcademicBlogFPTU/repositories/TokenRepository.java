package com.academicblogfptu.AcademicBlogFPTU.repositories;

import com.academicblogfptu.AcademicBlogFPTU.entities.TokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<TokenEntity, String> {

    Optional<TokenEntity> findByToken(String token);

    Optional<TokenEntity> findByRefreshToken(String refreshToken);


}
