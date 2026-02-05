package com.myprojects.urlshortener.web.mapper;

import com.myprojects.urlshortener.entity.ShortUrl;
import com.myprojects.urlshortener.entity.User;
import com.myprojects.urlshortener.web.view.ShortUrlView;
import com.myprojects.urlshortener.web.view.UserView;
import org.springframework.stereotype.Component;

@Component
public class ViewMapper {

    public ShortUrlView toShortUrlDto(ShortUrl shortUrl) {
        UserView userView = null;
        if(shortUrl.getCreatedBy() != null) {
            userView = toUserDto(shortUrl.getCreatedBy());
        }

        return new ShortUrlView(
                shortUrl.getId(),
                shortUrl.getShortKey(),
                shortUrl.getOriginalUrl(),
                shortUrl.getIsPrivate(),
                shortUrl.getExpiresAt(),
                userView,
                shortUrl.getClickCount(),
                shortUrl.getCreatedAt()
        );
    }

    public UserView toUserDto(User user) {
        return new UserView(user.getId(), user.getName());
    }
}