package com.myprojects.urlshortener.controller;

import com.myprojects.urlshortener.config.ApplicationProperties;
import com.myprojects.urlshortener.web.view.PagedResultView;
import com.myprojects.urlshortener.web.view.ShortUrlView;
import com.myprojects.urlshortener.service.ShortUrlService;
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
        PagedResultView<ShortUrlView> allUrls = shortUrlService.findAllShortUrls(page, applicationProperties.pageSize());
        model.addAttribute("shortUrls", allUrls);
        model.addAttribute("baseUrl", applicationProperties.baseUrl());
        model.addAttribute("paginationUrl", "/admin/dashboard");
        return "admin-dashboard";
    }
}