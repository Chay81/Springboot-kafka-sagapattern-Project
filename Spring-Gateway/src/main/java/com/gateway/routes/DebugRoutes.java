package com.gateway.routes;

import org.springframework.boot.ApplicationRunner;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DebugRoutes {

    @Bean
    public ApplicationRunner runner(RouteLocator locator) {
        return args -> locator.getRoutes().subscribe(route ->
                System.out.println("Loaded Route: " + route.getId() + " -> " + route.getUri())
        );
    }
}
