package com.worker.ai_productivity.controller;

import com.worker.ai_productivity.seed.DataInitializer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class InternalController {

    private final DataInitializer initializer;

    public InternalController(DataInitializer initializer) {
        this.initializer = initializer;
    }

    @PostMapping("/internal/seed")
    public ResponseEntity<?> reseed() {
        try {
            initializer.seedAll(); // safe idempotent seeding
            return ResponseEntity.ok(Map.of("status", "ok", "message", "seed completed"));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", ex.getMessage()
            ));
        }
    }
}
