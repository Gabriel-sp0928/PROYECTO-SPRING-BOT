package com.industriagafra.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SettingsController {

    @GetMapping("/settings")
    public String settings(Model model) {
        model.addAttribute("title", "Configuración - Industria Gafra");
        return "settings";
    }
}
