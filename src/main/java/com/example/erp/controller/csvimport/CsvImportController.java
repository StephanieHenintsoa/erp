package com.example.erp.controller.csvimport;

import com.example.erp.service.csvimport.CsvImportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;

@Controller
public class CsvImportController {

    private final CsvImportService csvImportService;

    public CsvImportController(CsvImportService csvImportService) {
        this.csvImportService = csvImportService;
    }

    @GetMapping("/import-csv")
    public String showImportPage() {
        return "import/import-csv";
    }

    @PostMapping("/import-csv")
    public String importCsv(@RequestParam("csvFile") MultipartFile file, Model model) {
        try {
            if (file.isEmpty()) {
                model.addAttribute("result", Map.of(
                    "success", false,
                    "message", "Please select a CSV file",
                    "errors", List.of("No file uploaded")
                ));
                return "import/import-csv"; // Corrigé pour correspondre au chemin
            }

            Map<String, Object> result = csvImportService.importCsv(file);
            model.addAttribute("result", result);
            return "import/import-csv"; // Corrigé pour correspondre au chemin

        } catch (Exception e) {
            model.addAttribute("result", Map.of(
                "success", false,
                "message", "Error processing CSV",
                "errors", List.of(e.getMessage())
            ));
            return "import/import-csv"; // Corrigé pour correspondre au chemin
        }
    }
}