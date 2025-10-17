package imbuy.backend.config;

import imbuy.backend.repository.UserRepository;
import imbuy.backend.service.TokenBlacklistService;
import imbuy.backend.utils.JwtTokenProvider;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements Filter {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;
    private final UserRepository userRepository;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpResp = (HttpServletResponse) response;

        String header = httpReq.getHeader("Authorization");

        try {
            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);

                if (!jwtTokenProvider.validateToken(token)) {
                    throw new JwtException("Invalid or expired token");
                }
                if (tokenBlacklistService.isBlacklisted(token)) {
                    throw new JwtException("Token is blacklisted");
                }

                String email = jwtTokenProvider.getEmailFromToken(token);
                userRepository.findByEmail(email)
                        .orElseThrow(() -> new JwtException("User not found"));
            }

            chain.doFilter(request, response);

        } catch (JwtException e) {
            httpResp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResp.setContentType("application/json");
            httpResp.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}
