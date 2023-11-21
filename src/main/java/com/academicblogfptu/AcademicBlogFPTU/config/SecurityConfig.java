package com.academicblogfptu.AcademicBlogFPTU.config;

import com.academicblogfptu.AcademicBlogFPTU.services.TokenServices;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig   {

    private final UserAuthenticationEntryPoint userAuthenticationEntryPoint;
    private final UserAuthProvider userAuthProvider;
    private final TokenServices tokenService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)throws Exception{

        http
                .exceptionHandling().authenticationEntryPoint(userAuthenticationEntryPoint)
                .and()
                .addFilterBefore(new JwtAuthFilter(userAuthProvider, tokenService), BasicAuthenticationFilter.class)
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeHttpRequests((requests)-> requests
                        .requestMatchers(HttpMethod.POST,"/users/login" ,
                                "/users/google-login" ,
                                "/users/send-code" ,
                                "/users/verify-code",
                                "/auth/refresh-token",
                                "/auth/remove-token"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET,"/ws/**").permitAll()
                        .anyRequest().authenticated());

        return http.build();
    }
}
