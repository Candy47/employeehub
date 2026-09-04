package com.employeehub.employee.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Trusts the identity forwarded by the API Gateway.
 *
 * The gateway is the single place that validates the JWT; it then injects the
 * authenticated user's email into the "X-Auth-User" header. This service holds
 * NO JWT secret and does no token parsing - it simply trusts the gateway.
 *
 * In production only the gateway is publicly reachable, so this header can be
 * trusted. (For local dev the services are also reachable directly, which is
 * why you should call everything through the gateway on port 8090.)
 */
@Component
public class GatewayAuthenticationFilter extends OncePerRequestFilter {

    public static final String USER_HEADER = "X-Auth-User";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String email = request.getHeader(USER_HEADER);

        if (email != null && !email.isBlank()
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_USER"))
                    );

            authenticationToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext()
                    .setAuthentication(authenticationToken);
        }

        filterChain.doFilter(request, response);
    }
}

