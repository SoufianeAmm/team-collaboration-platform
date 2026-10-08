package com.teamcollab.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Simple endpoint to verify the backend toolchain (JDK + Maven wrapper + Spring Boot) works.
 * Will be replaced by real feature controllers in later phases.
 */
@RestController
public class HealthController {

    @GetMapping("/api/health")
    public Map<String, String> health() {
        return Map.of("status", "ok", "service", "team-collaboration-platform-backend");
    }
}
