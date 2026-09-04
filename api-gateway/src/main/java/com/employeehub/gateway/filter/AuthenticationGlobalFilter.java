package com.employeehub.gateway.filter;

import com.employeehub.gateway.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * The single, centralized authentication point for EmployeeHub.
 *
 * - Public routes (login/register/health) pass straight through.
 * - Every other route requires a valid "jwt" cookie. The gateway verifies it
 *   ONCE, then forwards the caller's identity to the upstream service in a
 *   trusted "X-Auth-User" header. Any client-supplied X-Auth-User is stripped
 *   so it cannot be spoofed.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationGlobalFilter implements GlobalFilter, Ordered {

    public static final String USER_HEADER = "X-Auth-User";

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/actuator"
    );

    private final JwtService jwtService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // Let CORS preflight (OPTIONS) requests through untouched - the browser
        // sends them without the auth cookie, so they must not be rejected.
        if (request.getMethod() == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }

        // Never trust an incoming identity header from the client.
        ServerHttpRequest cleaned = request.mutate()
                .headers(h -> h.remove(USER_HEADER))
                .build();

        if (isPublic(path)) {
            return chain.filter(exchange.mutate().request(cleaned).build());
        }

        HttpCookie jwtCookie = request.getCookies().getFirst("jwt");

        if (jwtCookie == null) {
            return unauthorized(exchange, "Missing authentication token");
        }

        String email = jwtService.validateAndGetSubject(jwtCookie.getValue());

        if (email == null) {
            return unauthorized(exchange, "Invalid or expired token");
        }

        ServerHttpRequest authorized = cleaned.mutate()
                .header(USER_HEADER, email)
                .build();

        return chain.filter(exchange.mutate().request(authorized).build());
    }

    private boolean isPublic(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String reason) {
        log.warn("Rejected request to {} : {}",
                exchange.getRequest().getURI().getPath(), reason);
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        // Run before the routing filter.
        return -1;
    }
}



