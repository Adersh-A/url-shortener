package com.myprojects.urlshortener.service;

import com.myprojects.urlshortener.config.ApplicationProperties;
import com.myprojects.urlshortener.entity.ShortUrl;
import com.myprojects.urlshortener.web.form.CreateShortUrlForm;
import com.myprojects.urlshortener.web.mapper.ViewMapper;
import com.myprojects.urlshortener.web.view.PagedResultView;
import com.myprojects.urlshortener.web.view.ShortUrlView;
import com.myprojects.urlshortener.repository.ShortUrlRepository;
import com.myprojects.urlshortener.repository.UserRepository;
import com.myprojects.urlshortener.util.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.time.temporal.ChronoUnit.DAYS;

@Service
@Transactional(readOnly = true)
public class ShortUrlService {

    private static final Logger log = LoggerFactory.getLogger(ShortUrlService.class);
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int SHORT_KEY_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();
    private final ShortUrlRepository shortUrlRepository;
    private final ViewMapper viewMapper;
    private final ApplicationProperties applicationProperties;
    private final UserRepository userRepository;

    public ShortUrlService(ShortUrlRepository shortUrlRepository, ViewMapper viewMapper, ApplicationProperties applicationProperties, UserRepository userRepository) {
        this.shortUrlRepository = shortUrlRepository;
        this.viewMapper = viewMapper;
        this.applicationProperties = applicationProperties;
        this.userRepository = userRepository;
    }

    public static String generateRandomShortKey() {
        StringBuilder sb = new StringBuilder(SHORT_KEY_LENGTH);
        for (int i = 0; i < SHORT_KEY_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    public PagedResultView<ShortUrlView> findAllPublicShortUrls(int pageNo, int pageSize) {
        Pageable pageable = getPageable(pageNo, pageSize);
        Page<ShortUrlView> shortUrlDtoPage = shortUrlRepository.findPublicShortUrls(pageable).map(viewMapper::toShortUrlDto);
        return PagedResultView.from(shortUrlDtoPage);
    }

    public PagedResultView<ShortUrlView> findAllShortUrls(int page, int pageSize) {
        Pageable pageable = getPageable(page, pageSize);
        var shortUrlsPage =  shortUrlRepository.findAllShortUrls(pageable).map(viewMapper::toShortUrlDto);
        return PagedResultView.from(shortUrlsPage);
    }

    public PagedResultView<ShortUrlView> getUserShortUrls(Long userId, int page, int pageSize) {
        Pageable pageable = getPageable(page, pageSize);
        var shortUrlsPage = shortUrlRepository.findByCreatedById(userId, pageable)
                .map(viewMapper::toShortUrlDto);
        return PagedResultView.from(shortUrlsPage);
    }

    @Transactional
    public ShortUrlView createShortUrl(CreateShortUrlForm urlForm, Long userId) {
        if (applicationProperties.validateOriginalUrl()) {
            boolean isValid = UrlValidator.isValidUrl(urlForm.originalUrl());
            if (!isValid) {
                log.error("Invalid URL: {}", urlForm.originalUrl());
                throw new IllegalArgumentException("Invalid URL " + urlForm.originalUrl());
            }
        }
        var shortKey = generateUniqueShortKey();
        var shortUrl = new ShortUrl();
        shortUrl.setOriginalUrl(urlForm.originalUrl());
        shortUrl.setShortKey(shortKey);
        if (userId == null) {
            shortUrl.setCreatedBy(null);
            shortUrl.setIsPrivate(false);
            shortUrl.setExpiresAt(Instant.now().plus(applicationProperties.defaultExpiryInDays(), DAYS));
        } else {
            shortUrl.setCreatedBy(userRepository.findById(userId).orElseThrow());
            shortUrl.setIsPrivate(urlForm.isPrivate() != null && urlForm.isPrivate());
            shortUrl.setExpiresAt(urlForm.expirationInDays() != null ? Instant.now().plus(urlForm.expirationInDays(), DAYS) : null);
        }
        shortUrl.setClickCount(0L);
        shortUrl.setCreatedAt(Instant.now());
        shortUrlRepository.save(shortUrl);
        return viewMapper.toShortUrlDto(shortUrl);
    }

    @Transactional
    public Optional<ShortUrlView> accessShortUrl(String shortKey, Long userId) {
        Optional<ShortUrl> shortUrlOptional = shortUrlRepository.findByShortKey(shortKey);
        if (shortUrlOptional.isEmpty()) {
            return Optional.empty();
        }
        ShortUrl shortUrl = shortUrlOptional.get();
        if (shortUrl.getExpiresAt() != null && shortUrl.getExpiresAt().isBefore(Instant.now())) {
            return Optional.empty();
        }
        if (shortUrl.getIsPrivate() != null && shortUrl.getCreatedBy() != null && !Objects.equals(shortUrl.getCreatedBy().getId(), userId)) {
            return Optional.empty();
        }
        shortUrl.setClickCount(shortUrl.getClickCount() + 1);
        shortUrlRepository.save(shortUrl);
        return shortUrlOptional.map(viewMapper::toShortUrlDto);
    }

    @Transactional
    public void deleteUserShortUrls(List<Long> ids, Long userId) {
        if (ids != null && !ids.isEmpty() && userId != null) {
            shortUrlRepository.deleteByIdInAndCreatedById(ids, userId);
        }
    }

    private String generateUniqueShortKey() {
        String shortKey;
        do {
            shortKey = generateRandomShortKey();
        } while (shortUrlRepository.existsByShortKey(shortKey));
        return shortKey;
    }

    private Pageable getPageable(int page, int size) {
        page = page > 1 ? page - 1: 0;
        return PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
    }
}