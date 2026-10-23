package com.example.ecom_project.dto;

import java.time.Instant;
import java.util.Map;

import org.springframework.http.HttpStatus;

public record ApiError(Instant timestamp, int status, String error, String message, Map<String, String> errors) {

    public static ApiError of(HttpStatus status, String message) {
        return new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), message, Map.of());
    }
}
