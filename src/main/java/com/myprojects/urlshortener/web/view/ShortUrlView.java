package com.myprojects.urlshortener.web.view;

import java.io.Serializable;
import java.time.Instant;

public record ShortUrlView(Long id, String shortKey, String originalUrl,
                           Boolean isPrivate, Instant expiresAt,
                           UserView createdBy, Long clickCount,
                           Instant createdAt) implements Serializable {
}