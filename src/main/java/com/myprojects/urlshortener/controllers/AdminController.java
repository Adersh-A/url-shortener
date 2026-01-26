package com.myprojects.urlshortener.controllers;

import com.myprojects.urlshortener.ApplicationProperties;
import com.myprojects.urlshortener.models.PagedResult;
import com.myprojects.urlshortener.models.ShortUrlDto;
import com.myprojects.urlshortener.services.ShortUrlService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final ShortUrlService shortUrlService;
    private final ApplicationProperties applicationProperties;

    public AdminController(ShortUrlService shortUrlService, ApplicationProperties applicationProperties) {
        this.shortUrlService = shortUrlService;
        this.applicationProperties = applicationProperties;
    }

    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(defaultValue = "1") int page,
            Model model) {
        PagedResult<ShortUrlDto> allUrls = shortUrlService.findAllShortUrls(page, applicationProperties.pageSize());
        model.addAttribute("shortUrls", allUrls);
        model.addAttribute("baseUrl", applicationProperties.baseUrl());
        model.addAttribute("paginationUrl", "/admin/dashboard");
        return "admin-dashboard";
    }
}