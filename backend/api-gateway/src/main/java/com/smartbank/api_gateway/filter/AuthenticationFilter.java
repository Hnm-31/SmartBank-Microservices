package com.smartbank.api_gateway.filter;

import com.smartbank.api_gateway.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    // Public endpoints that don't need a token
    private static final List<String> OPEN_ENDPOINTS = List.of(
            "/api/v1/auth/register",
            "/api/v1/auth/login",
            "/eureka"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 1. Check if the route is inherently public
        if (OPEN_ENDPOINTS.stream().anyMatch(path::contains)) {
            return chain.filter(exchange);
        }
        
        // 1.5 Allow anonymous POST to support tickets, but block GET (viewing tickets)
        if (path.contains("/api/v1/users/tickets") && request.getMethod().name().equalsIgnoreCase("POST")) {
            return chain.filter(exchange);
        }

        // 2. Check for Authorization header
        if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        try {
            // 3. Cryptographically validate the token in-memory!
            jwtUtil.validateToken(token);

            // 4. Extract Username & Role
            String username = jwtUtil.extractUsername(token);
            String role = jwtUtil.extractRole(token);

            // 5. Mutate the request to inject headers for downstream services
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header("X-User-Name", username)
                    .header("X-User-Role", role)
                    .build();

            // 6. Forward to the downstream microservice
            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (Exception e) {
            System.out.println("Invalid JWT Token: " + e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -1; // Execute this filter extremely early in the chain
    }
}
