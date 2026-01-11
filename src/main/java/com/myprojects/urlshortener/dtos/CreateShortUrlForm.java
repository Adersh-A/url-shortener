package com.myprojects.urlshortener.dtos;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public record CreateShortUrlForm(
        @NotBlank(message = "Original URL is required")
        String originalUrl) {
}