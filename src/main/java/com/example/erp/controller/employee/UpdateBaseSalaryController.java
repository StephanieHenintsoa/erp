package com.example.erp.controller.employee;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.erp.service.salary.SalaryComponentService;
import com.example.erp.service.salary.UpdateBaseSalaryService;

@Controller
public class UpdateBaseSalaryController {

    @Autowired
    private UpdateBaseSalaryService updateBaseSalaryService;

    @Autowired
    private SalaryComponentService salaryComponentService;

    @GetMapping("/update-base-salary")
    public String showUpdateBaseSalaryForm(Model model) {
        model.addAttribute("salaryComponents", salaryComponentService.getAllSalaryComponents());
        model.addAttribute("activePage", "updateBaseSalary");
        return "/emp/update-base-salary";
    }

    @PostMapping("/update-base-salary")
    public String updateBaseSalary(
        @RequestParam double newBaseSalary,
        @RequestParam String salaryComponent,
        @RequestParam String comparisonOperator,
        @RequestParam double threshold,
        RedirectAttributes redirectAttributes
    ) {
        try {
            int updatedCount = updateBaseSalaryService.updateBaseSalaries(
                newBaseSalary, salaryComponent, comparisonOperator, threshold
            );
            redirectAttributes.addFlashAttribute("successMessage", "Mis a jour ");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur MAJ");
        }
        return "redirect:/update-base-salary";
    }
}