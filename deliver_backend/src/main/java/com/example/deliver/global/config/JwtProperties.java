package com.example.deliver.global.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        @NotBlank
        @Size(min = 32, message = "JWT secret must be at least 32 characters.")
        String secret,

        @Min(value = 60000, message = "JWT access token expiration must be at least 60,000ms.")
        long accessTokenExpirationMs
) {
}