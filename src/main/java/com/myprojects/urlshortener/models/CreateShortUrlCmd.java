package com.myprojects.urlshortener.models;

public record CreateShortUrlCmd(
        String originalUrl,
        Boolean isPrivate,
        Integer expirationInDays,
        Long userId
) {}