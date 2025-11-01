package com.carmaintenance.security;

import com.carmaintenance.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

/**
 * JWT认证过滤器
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && !jwtUtils.isTokenExpired(jwt)) {
                String username = jwtUtils.getUsernameFromToken(jwt);
                Long userId = jwtUtils.getUserIdFromToken(jwt);
                Integer userType = jwtUtils.getUserTypeFromToken(jwt);

                // 根据用户类型设置角色
                String role = getRoleByUserType(userType);

                UserPrincipal userPrincipal = new UserPrincipal(userId, username, userType,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role)));

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            logger.error("无法设置用户认证", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private String getRoleByUserType(Integer userType) {
        switch (userType) {
            case 1:
                return "CUSTOMER";
            case 2:
                return "SHOP";
            case 3:
                return "ADMIN";
            default:
                return "CUSTOMER";
        }
    }
}