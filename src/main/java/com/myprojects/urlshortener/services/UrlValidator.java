package com.myprojects.urlshortener.services;

import java.net.URI;

public class UrlValidator {

    private UrlValidator(){
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean isValidUrl(String urlString) {
        if (urlString == null || urlString.isBlank()) {
            return false;
        }

        try {
            URI uri = URI.create(urlString);
            String scheme = uri.getScheme();

            return ( "http".equalsIgnoreCase(scheme)
                    || "https".equalsIgnoreCase(scheme) )
                    && uri.getHost() != null;

        } catch (Exception e) {
            return false;
        }
    }
}