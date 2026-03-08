package com.example.stduents_management.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final OverviewStatsService overviewStatsService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/login")
    public String adminLoginPage() {
        return "admin/login";
    }

    @GetMapping
    public String adminRoot(Model model) throws JsonProcessingException {
        Map<String, Object> stats = overviewStatsService.getOverviewStats();
        model.addAttribute("barChartLabelsJson", objectMapper.writeValueAsString(stats.get("barChartLabels")));
        model.addAttribute("barChartDataJson", objectMapper.writeValueAsString(stats.get("barChartData")));
        model.addAttribute("lineChartLabelsJson", objectMapper.writeValueAsString(stats.get("lineChartLabels")));
        model.addAttribute("lineChartDataJson", objectMapper.writeValueAsString(stats.get("lineChartData")));
        model.addAttribute("pieChartLabelsJson", objectMapper.writeValueAsString(stats.get("pieChartLabels")));
        model.addAttribute("pieChartDataJson", objectMapper.writeValueAsString(stats.get("pieChartData")));
        return "admin/index";
    }
}

