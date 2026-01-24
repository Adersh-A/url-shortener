package com.myprojects.urlshortener.services;

import com.myprojects.urlshortener.ApplicationProperties;
import com.myprojects.urlshortener.entities.ShortUrl;
import com.myprojects.urlshortener.entities.User;
import com.myprojects.urlshortener.models.CreateShortUrlCmd;
import com.myprojects.urlshortener.models.ShortUrlDto;
import com.myprojects.urlshortener.repositories.ShortUrlRepository;
import com.myprojects.urlshortener.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import static java.time.temporal.ChronoUnit.*;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ShortUrlService {

    private final ShortUrlRepository shortUrlRepository;
    private final EntityMapper entityMapper;
    private final ApplicationProperties applicationProperties;
    private final UserRepository userRepository;

    private static final Logger log =
            LoggerFactory.getLogger(ShortUrlService.class);

    public ShortUrlService(ShortUrlRepository shortUrlRepository,
                           EntityMapper entityMapper,
                           ApplicationProperties applicationProperties,
                           UserRepository userRepository) {
        this.shortUrlRepository = shortUrlRepository;
        this.entityMapper = entityMapper;
        this.applicationProperties = applicationProperties;
        this.userRepository = userRepository;
    }

    public List<ShortUrlDto> findAllPublicShortUrls() {
        return shortUrlRepository.findPublicShortUrls()
                .stream().map(entityMapper::toShortUrlDto).toList();
    }

    @Transactional
    public ShortUrlDto createShortUrl(CreateShortUrlCmd cmd) {
        if(applicationProperties.validateOriginalUrl()) {
            boolean isValid = UrlValidator.isValidUrl(cmd.originalUrl());
            if(!isValid) {
                log.error("Invalid URL: {}", cmd.originalUrl());
                throw new IllegalArgumentException("Invalid URL "+cmd.originalUrl());
            }
        }
        var shortKey = generateUniqueShortKey();
        var shortUrl = new ShortUrl();
        shortUrl.setOriginalUrl(cmd.originalUrl());
        shortUrl.setShortKey(shortKey);
        if(cmd.userId() == null) {
            shortUrl.setCreatedBy(null);
            shortUrl.setIsPrivate(false);
            shortUrl.setExpiresAt(Instant.now().plus(applicationProperties.defaultExpiryInDays(), DAYS));
        } else {
            shortUrl.setCreatedBy(userRepository.findById(cmd.userId()).orElseThrow());
            shortUrl.setIsPrivate(cmd.isPrivate() != null && cmd.isPrivate());
            shortUrl.setExpiresAt(cmd.expirationInDays() != null ? Instant.now().plus(cmd.expirationInDays(), DAYS) : null);
        }
        shortUrl.setClickCount(0L);
        shortUrl.setCreatedAt(Instant.now());
        shortUrlRepository.save(shortUrl);
        return entityMapper.toShortUrlDto(shortUrl);
    }

    @Transactional
    public Optional<ShortUrlDto> accessShortUrl(String shortKey, Long userId) {
        Optional<ShortUrl> shortUrlOptional = shortUrlRepository.findByShortKey(shortKey);
        if(shortUrlOptional.isEmpty()) {
            return Optional.empty();
        }
        ShortUrl shortUrl = shortUrlOptional.get();
        if(shortUrl.getExpiresAt() != null && shortUrl.getExpiresAt().isBefore(Instant.now())) {
            return Optional.empty();
        }
        if(shortUrl.getIsPrivate() != null && shortUrl.getCreatedBy() != null
                && !Objects.equals(shortUrl.getCreatedBy().getId(), userId)) {
            return Optional.empty();
        }
        shortUrl.setClickCount(shortUrl.getClickCount()+1);
        shortUrlRepository.save(shortUrl);
        return shortUrlOptional.map(entityMapper::toShortUrlDto);
    }

    private String generateUniqueShortKey() {
        String shortKey;
        do {
            shortKey = generateRandomShortKey();
        } while (shortUrlRepository.existsByShortKey(shortKey));
        return shortKey;
    }

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int SHORT_KEY_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generateRandomShortKey() {
        StringBuilder sb = new StringBuilder(SHORT_KEY_LENGTH);
        for (int i = 0; i < SHORT_KEY_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }
}