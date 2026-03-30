package com.example.stduents_management.web;

import com.example.stduents_management.employee.entity.DecisionType;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(String.class, DecisionType.class, source -> {
            if (source == null || source.isBlank()) {
                return null;
            }
            return DecisionType.valueOf(source.trim());
        });
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Map URL /uploads/** tới thư mục uploads/ trong project
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}

