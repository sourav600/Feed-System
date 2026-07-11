package com.newsfeed.gateway.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring Cloud Gateway Server WebMVC has no {@code globalcors} equivalent (unlike the classic
 * reactive Gateway) - its routes are plain {@code RouterFunction}s served through Spring MVC's
 * {@code RouterFunctionMapping}, and Spring's {@code WebMvcConfigurationSupport} wires this
 * registry into every {@code HandlerMapping} including that one, so a standard
 * {@link WebMvcConfigurer} CORS registration applies to gateway-routed requests exactly as it
 * would to any other MVC endpoint.
 */
@Configuration
@RequiredArgsConstructor
public class GatewayConfig implements WebMvcConfigurer {

    private final CorsProperties corsProperties;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(corsProperties.allowedOrigin())
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                // Required for cookie-based auth to survive the gateway hop - see
                // backend-service's SecurityConfig for why allowCredentials is non-negotiable.
                .allowCredentials(true);
        registry.addMapping("/media/**")
                .allowedOrigins(corsProperties.allowedOrigin())
                .allowedMethods("GET", "OPTIONS")
                .allowCredentials(true);
    }
}
