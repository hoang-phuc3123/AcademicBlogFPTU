package com.academicblogfptu.AcademicBlogFPTU.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import com.academicblogfptu.AcademicBlogFPTU.services.TokenServices;

@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final UserAuthProvider userAuthProvider;
    private final TokenServices tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if(header != null){
            String[] elements = header.split(" ");
            if(elements.length == 2 && "Bearer".equals(elements[0])){
                if (!tokenService.isTokenExist(elements[1])) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("The token is not exist.");
                    response.getWriter().flush();
                    response.getWriter().close();
                    return;
                }
                try{
                    SecurityContextHolder.getContext().setAuthentication(userAuthProvider.validateToken(elements[1]));
                    String username = userAuthProvider.getUser(elements[1]);
                    if (userAuthProvider.isBanned(username)) {
                        response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                        response.getWriter().write("The user has been banned.");
                        response.getWriter().flush();
                        response.getWriter().close();
                        return;
                    }
                } catch (RuntimeException e){
                    try {
                        SecurityContextHolder.getContext().setAuthentication(userAuthProvider.validateTokenEmail(elements[1]));
                    } catch (RuntimeException ex) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.getWriter().write("The token has expired.");
                        response.getWriter().flush();
                        response.getWriter().close();
                        return;
                    }
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
