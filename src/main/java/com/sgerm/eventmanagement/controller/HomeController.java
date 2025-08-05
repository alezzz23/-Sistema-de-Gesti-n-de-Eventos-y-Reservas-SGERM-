package com.sgerm.eventmanagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador para páginas web
 */
@Controller
public class HomeController {
    
    /**
     * Página de inicio
     */
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("appName", "Sistema de Gestión de Eventos y Reservas (SGERM)");
        model.addAttribute("version", "1.0.0");
        return "index";
    }
    
    /**
     * Página de información de la API
     */
    @GetMapping("/api")
    public String apiInfo(Model model) {
        model.addAttribute("appName", "SGERM API");
        return "api-info";
    }
}