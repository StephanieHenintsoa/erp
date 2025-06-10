package com.example.erp.controller.employee;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.erp.service.employee.EmployeeSalaryService;

@Controller
public class EmployeeSalaryController {

    @Autowired
    private EmployeeSalaryService employeeSalaryService;

    // list of months for dropdowns
    private static final List<String> MONTHS = Arrays.asList(
        "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
        "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"
    );

    // display salary generation form
    @GetMapping("/generate-salary")
    public String showGenerateSalaryForm(Model model) {
        model.addAttribute("months", MONTHS);
        model.addAttribute("employees", employeeSalaryService.getAllEmployees());
        model.addAttribute("activePage", "generateSalary");
        return "/emp/generate-salary";
    }

    // handle salary generation form submission
    @PostMapping("/generate-salary")
    public String generateSalary(
        @RequestParam String employee,
        @RequestParam String moisDebut,
        @RequestParam int anneeDebut,
        @RequestParam String moisFin,
        @RequestParam int anneeFin,
        RedirectAttributes redirectAttributes
    ) {
        try {
            employeeSalaryService.generateSalaries(employee, moisDebut, anneeDebut, moisFin, anneeFin);
            redirectAttributes.addFlashAttribute("successMessage", "Salaires générés avec succès.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de la génération des salaires.");
        }
        return "redirect:/generate-salary";
    }
}