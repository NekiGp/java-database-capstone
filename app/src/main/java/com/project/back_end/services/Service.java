package com.project.back_end.services;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

@org.springframework.stereotype.Service
public class Service {

    private final TokenService tokenService;

    public Service(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    public ResponseEntity<Map<String, String>> validateToken(
            String token,
            String role
    ) {
        Map<String, String> response = new HashMap<>();

        boolean validToken = tokenService.validateToken(token, role);

        if (validToken) {
            return ResponseEntity.ok(response);
        }

        response.put("message", "Invalid or expired token");

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }
}