package com.example.stduents_management.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping
    public String adminRoot() {
        // Trang quản lý chính, có thể là overview hoặc redirect sang /admin/roles
        return "admin/index";
    }
}

