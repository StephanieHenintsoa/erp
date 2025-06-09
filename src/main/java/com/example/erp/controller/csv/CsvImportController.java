package com.example.erp.controller.csv;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.erp.dto.CsvImportRequest;
import com.example.erp.dto.CsvImportResponse;
import com.example.erp.service.auth.LoginService;
import com.example.erp.service.csv.CsvImportService;

import jakarta.servlet.http.HttpSession;

@Controller
public class CsvImportController {

    private final CsvImportService csvImportService;

    @Autowired
    public CsvImportController(CsvImportService csvImportService) {
        this.csvImportService = csvImportService;
    }

    @GetMapping("/csv-import")
    public String showImportForm(Model model, HttpSession session) {
        if (!LoginService.isAuthenticated(session)) {
            return "redirect:/"; 
        }
        model.addAttribute("csvImportRequest", new CsvImportRequest());
        return "/import/csv-import";
    }

    @PostMapping("/csv-import")
    public String handleCsvImport(@ModelAttribute CsvImportRequest request, Model model, HttpSession session) {
        if (!LoginService.isAuthenticated(session)) {
            return "redirect:/"; 
        }
        CsvImportResponse response = csvImportService.importCsvFiles(request);
        
        model.addAttribute("response", response);
        model.addAttribute("csvImportRequest", new CsvImportRequest());
        return "/import/csv-import";
    }
}