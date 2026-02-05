package com.myprojects.urlshortener.web.view;

import java.io.Serializable;

public record UserView(Long id, String name) implements Serializable {
}