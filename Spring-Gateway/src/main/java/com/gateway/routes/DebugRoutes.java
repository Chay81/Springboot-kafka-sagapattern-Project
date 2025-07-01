package com.gateway.routes;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class DebugRoutes {

    @Bean
    public ApplicationRunner runner(RouteLocator locator) {
        return args -> locator.getRoutes().subscribe(route ->
                log.info("Loaded Route: " + route.getId() + " -> " + route.getUri())
        );
    }
}
