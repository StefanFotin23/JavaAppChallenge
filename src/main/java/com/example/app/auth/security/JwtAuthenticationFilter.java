package com.example.app.auth.security;

import com.example.app.auth.dto.AuthResponse;
import com.example.app.auth.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final AuthContext authContext;
    private final AuthService authService;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                String username = authService.extractUsername(token);
                String refreshToken = req.getHeader("Refresh-Token");
                if (refreshToken == null) {
                    refreshToken = req.getHeader("X-Refresh-Token");
                }
                authContext.setTokens(token, refreshToken);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    var auth = new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                String refreshToken = req.getHeader("Refresh-Token");
                if (refreshToken == null) {
                    refreshToken = req.getHeader("X-Refresh-Token");
                }

                if (refreshToken != null) {
                    try {
                        AuthResponse refreshed = authService.refresh(refreshToken);
                        String newAccessToken = refreshed.accessToken();
                        String username = authService.extractUsername(newAccessToken);
                        
                        authContext.setTokens(newAccessToken, refreshToken);

                        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                            var auth = new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
                            SecurityContextHolder.getContext().setAuthentication(auth);
                        }

                        res.setHeader("New-Access-Token", newAccessToken);
                        res.setHeader("Access-Control-Expose-Headers", "New-Access-Token");
                    } catch (Exception ex) {
                        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        res.setContentType("application/json");
                        res.getWriter().write("{\"error\": \"JWT expired and refresh failed\"}");
                        return;
                    }
                } else {
                    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    res.setContentType("application/json");
                    res.getWriter().write("{\"error\": \"JWT expired and no refresh token provided\"}");
                    return;
                }
            } catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                res.setContentType("application/json");
                res.getWriter().write("{\"error\": \"Invalid JWT token\"}");
                return;
            }
        }
        chain.doFilter(req, res);
    }
}
