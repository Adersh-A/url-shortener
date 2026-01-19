INSERT INTO users (email, password, name, role)
VALUES ('admin@gmail.com', 'admin', 'Administrator', 'ROLE_ADMIN'),
       ('macro@gmail.com', 'pass', 'Macro', 'ROLE_USER');

INSERT INTO short_urls (short_key, original_url, created_by, created_at, expires_at, is_private, click_count)
VALUES ('sa1Aed', 'https://en.wikipedia.org/wiki/URL_shortening', 1, TIMESTAMP '2024-07-15', NULL, FALSE,
        0);