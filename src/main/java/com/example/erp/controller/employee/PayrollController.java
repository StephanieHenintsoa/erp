package com.example.erp.controller.employee;

import com.example.erp.entity.Employee;
import com.example.erp.entity.salary.SalarySlip;
import com.example.erp.service.salary.PayrollService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/payroll")
public class PayrollController {

    private static final Logger logger = LoggerFactory.getLogger(PayrollController.class);

    @Autowired
    private PayrollService payrollService;

    @GetMapping
    public String showPayrollPage(Model model) {
        List<Employee> employees = payrollService.getAllEmployees();
        if (employees == null) {
            employees = new ArrayList<>();
            logger.warn("No employees found, setting empty list");
        }
        logger.info("Loaded {} employees for payroll page", employees.size());
        model.addAttribute("employees", employees);
        model.addAttribute("salarySlips", new ArrayList<SalarySlip>());
        model.addAttribute("selectedMonth", "");
        model.addAttribute("selectedYear", "");
        return "emp/payroll";
    }

    @PostMapping
    public String filterPayroll(
            @RequestParam(value = "month", required = false) String month,
            @RequestParam(value = "year", required = false) String year,
            Model model) {

        List<Employee> employees = payrollService.getAllEmployees();
        if (employees == null) {
            employees = new ArrayList<>();
        }

        List<SalarySlip> salarySlips = payrollService.getFilteredSalarySlips(month, year);

        if (salarySlips == null || salarySlips.isEmpty()) {
            throw new IllegalStateException("Aucune fiche de paie trouvée pour le mois " + month + " et l'année " + year);
        }

        model.addAttribute("employees", employees);
        model.addAttribute("salarySlips", salarySlips);
        model.addAttribute("selectedMonth", month != null ? month : "");
        model.addAttribute("selectedYear", year != null ? year : "");
        return "emp/payroll";
    }


    @ExceptionHandler(IllegalStateException.class)
    public String handleIllegalStateException(IllegalStateException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());

        // Ajoute aussi les données nécessaires pour que la page s'affiche bien
        List<Employee> employees = payrollService.getAllEmployees();
        if (employees == null) employees = new ArrayList<>();
        model.addAttribute("employees", employees);
        model.addAttribute("salarySlips", new ArrayList<SalarySlip>());
        model.addAttribute("selectedMonth", "");
        model.addAttribute("selectedYear", "");

        return "emp/payroll";
    }

    public String getMonthName(String monthNumber) {
        if (monthNumber == null || monthNumber.isEmpty()) {
            return "tous les mois";
        }
        switch (monthNumber) {
            case "01": return "Janvier";
            case "02": return "Février";
            case "03": return "Mars";
            case "04": return "Avril";
            case "05": return "Mai";
            case "06": return "Juin";
            case "07": return "Juillet";
            case "08": return "Août";
            case "09": return "Septembre";
            case "10": return "Octobre";
            case "11": return "Novembre";
            case "12": return "Décembre";
            default: return "mois inconnu";
        }
    }
    


}
