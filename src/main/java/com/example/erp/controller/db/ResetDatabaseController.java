package com.example.erp.controller.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.erp.service.db.ResetDatabaseService;

@Controller
@RequestMapping("/reset-database")
public class ResetDatabaseController {

    @Autowired
    private ResetDatabaseService resetDatabaseService;

    @GetMapping
    public String showResetPage(Model model) {
        model.addAttribute("activePage", "reset-database");
        return "/db/reset-database";
    }

    @PostMapping("/confirm")
    public String confirmReset(Model model) {
        try {
            resetDatabaseService.resetDatabase();
            model.addAttribute("message", "Database reset successfully.");
            model.addAttribute("success", true);
        } catch (Exception e) {
            model.addAttribute("message", "Failed to reset database: " + e.getMessage());
            model.addAttribute("success", false);
        }
        model.addAttribute("activePage", "reset-database");
        return "/db/reset-database";
    }
}
