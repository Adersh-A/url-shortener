package com.myprojects.urlshortener.models;

public record CreateUserCmd(
        String email,
        String password,
        String name,
        Role role) {
}