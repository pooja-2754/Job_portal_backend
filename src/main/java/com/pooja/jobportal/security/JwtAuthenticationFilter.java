package com.pooja.jobportal.security;

import com.pooja.jobportal.repository.CompanyRepository;
import com.pooja.jobportal.repository.UserRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends GenericFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserRepository userRepository, CompanyRepository companyRepository){
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;

        String authHeader = req.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {

            String token = authHeader.substring(7);

            JwtTokenProvider.TokenValidationResult validationResult = jwtTokenProvider.validateTokenDetailed(token);
            
            if (validationResult.isValid()) {
                String email = validationResult.getEmail();
                String entityType = validationResult.getEntityType();

                if ("COMPANY".equals(entityType)) {
                    // Handle company authentication
                    companyRepository.findByEmail(email).ifPresent(company -> {
                        CompanyPrincipal companyPrincipal = new CompanyPrincipal(company);
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(companyPrincipal, null, companyPrincipal.getAuthorities());

                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));

                        SecurityContextHolder.getContext().setAuthentication(auth);
                    });
                } else {
                    // Handle user authentication (default behavior)
                    userRepository.findByEmail(email).ifPresent(user -> {
                        UserPrincipal userPrincipal = new UserPrincipal(user);
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());

                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));

                        SecurityContextHolder.getContext().setAuthentication(auth);
                    });
                }
            }
        }

        chain.doFilter(request, response);
    }
}
