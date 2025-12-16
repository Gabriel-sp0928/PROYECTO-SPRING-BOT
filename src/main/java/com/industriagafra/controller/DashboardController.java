package com.industriagafra.controller;

import com.industriagafra.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping
    public org.springframework.http.ResponseEntity<?> getDashboardData(org.springframework.security.core.Authentication authentication) {
        if (authentication == null || authentication.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_LOGISTICS"))) {
            return org.springframework.http.ResponseEntity.status(403).build();
        }
        return org.springframework.http.ResponseEntity.ok(dashboardService.getDashboardData());
    }
}