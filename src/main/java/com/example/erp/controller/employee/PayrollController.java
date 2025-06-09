package com.example.erp.controller.employee;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.erp.entity.Employee;
import com.example.erp.entity.salary.PayrollComponentsResponse;
import com.example.erp.entity.salary.SalarySlip;
import com.example.erp.service.auth.LoginService;
import com.example.erp.service.pagination.PaginationService;
import com.example.erp.service.salary.PayrollService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/payroll")
public class PayrollController {

    private static final Logger logger = LoggerFactory.getLogger(PayrollController.class);

    @Autowired
    private PayrollService payrollService;

    @Autowired
    private PaginationService<SalarySlip> paginationService;

    // Map French month names to numerical values for sorting
    private static final Map<String, String> FRENCH_MONTH_TO_NUMBER = new HashMap<>();
    static {
        FRENCH_MONTH_TO_NUMBER.put("Janvier", "01");
        FRENCH_MONTH_TO_NUMBER.put("Février", "02");
        FRENCH_MONTH_TO_NUMBER.put("Mars", "03");
        FRENCH_MONTH_TO_NUMBER.put("Avril", "04");
        FRENCH_MONTH_TO_NUMBER.put("Mai", "05");
        FRENCH_MONTH_TO_NUMBER.put("Juin", "06");
        FRENCH_MONTH_TO_NUMBER.put("Juillet", "07");
        FRENCH_MONTH_TO_NUMBER.put("Août", "08");
        FRENCH_MONTH_TO_NUMBER.put("Septembre", "09");
        FRENCH_MONTH_TO_NUMBER.put("Octobre", "10");
        FRENCH_MONTH_TO_NUMBER.put("Novembre", "11");
        FRENCH_MONTH_TO_NUMBER.put("Décembre", "12");
    }

    @GetMapping
    public String showPayrollPage(
            @RequestParam(value = "month", required = false) String month,
            @RequestParam(value = "year", required = false) String year,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            Model model, 
            HttpSession session) 
    {
        if (!LoginService.isAuthenticated(session)) {
            return "redirect:/"; 
        }

        List<Employee> employees = payrollService.getAllEmployees();
        if (employees == null) {
            employees = new ArrayList<>();
            logger.warn("No employees found, setting empty list");
        }

        List<SalarySlip> salarySlips = new ArrayList<>();
        if (month != null && !month.isEmpty() && year != null && !year.isEmpty()) {
            salarySlips = payrollService.getFilteredSalarySlips(month, year);
        } else {
            salarySlips = payrollService.getFilteredSalarySlips(null, null);
        }
        if (salarySlips == null) {
            salarySlips = new ArrayList<>();
        }

        List<SalarySlip> paginatedSalarySlips = paginationService.getPaginatedItems(salarySlips, page, pageSize);
        int totalPages = paginationService.getTotalPages(salarySlips, pageSize);
        List<Integer> pageNumbers = paginationService.getPageNumbers(page, totalPages, 5);

        Set<String> uniqueEmployees = salarySlips.stream()
                .map(SalarySlip::getEmployee)
                .collect(Collectors.toSet());
        int uniqueEmployeeCount = uniqueEmployees.size();

        model.addAttribute("employees", employees);
        model.addAttribute("salarySlips", paginatedSalarySlips);
        model.addAttribute("selectedMonth", month != null ? month : "");
        model.addAttribute("selectedYear", year != null ? year : "");
        model.addAttribute("uniqueEmployeeCount", uniqueEmployeeCount);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("activePage", "payroll");

        return "emp/payroll";
    }

    @PostMapping
    public String filterPayroll(
            @RequestParam(value = "month", required = false) String month,
            @RequestParam(value = "year", required = false) String year,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            Model model,
            HttpSession session) {
        
        if (!LoginService.isAuthenticated(session)) {
            return "redirect:/"; 
        }
        List<Employee> employees = payrollService.getAllEmployees();
        if (employees == null) {
            employees = new ArrayList<>();
        }

        List<SalarySlip> salarySlips = payrollService.getFilteredSalarySlips(month, year);
        if (salarySlips == null) {
            salarySlips = new ArrayList<>();
        }

        List<SalarySlip> paginatedSalarySlips = paginationService.getPaginatedItems(salarySlips, page, pageSize);
        int totalPages = paginationService.getTotalPages(salarySlips, pageSize);
        List<Integer> pageNumbers = paginationService.getPageNumbers(page, totalPages, 5);

        Set<String> uniqueEmployees = salarySlips.stream()
                .map(SalarySlip::getEmployee)
                .collect(Collectors.toSet());
        int uniqueEmployeeCount = uniqueEmployees.size();

        model.addAttribute("employees", employees);
        model.addAttribute("salarySlips", paginatedSalarySlips);
        model.addAttribute("selectedMonth", month != null ? month : "");
        model.addAttribute("selectedYear", year != null ? year : "");
        model.addAttribute("uniqueEmployeeCount", uniqueEmployeeCount);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("activePage", "payroll");

        return "emp/payroll";
    }

    @GetMapping("/months")
    public String showPayrollMonthsPage(
            @RequestParam(value = "year", required = false) String year,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int pageSize,
            Model model,
            HttpSession session) {
    
        if (!LoginService.isAuthenticated(session)) {
            return "redirect:/"; 
        }
        List<SalarySlip> salarySlips = payrollService.getMonthlyAggregatedSalarySlips(null, year);
        if (salarySlips == null) {
            salarySlips = new ArrayList<>();
        }

        // Sort salary slips by month (January to December) based on postingDate
        salarySlips.sort(Comparator.comparing(slip -> {
            String date = slip.getPostingDate() != null ? slip.getPostingDate() : "";
            if (date.isEmpty()) return "9999-99"; // Push null/empty dates to the end
            try {
                String[] parts = date.split(" ");
                String monthName = parts[0];
                String yearPart = parts.length > 1 ? parts[1] : "0000";
                String monthNumber = FRENCH_MONTH_TO_NUMBER.getOrDefault(monthName, "99");
                return yearPart + "-" + monthNumber;
            } catch (Exception e) {
                logger.warn("Invalid postingDate format: {}", date);
                return "9999-99";
            }
        }));

        List<SalarySlip> paginatedSalarySlips = paginationService.getPaginatedItems(salarySlips, page, pageSize);
        int totalPages = paginationService.getTotalPages(salarySlips, pageSize);
        List<Integer> pageNumbers = paginationService.getPageNumbers(page, totalPages, 5);

        Set<String> uniquePayDates = salarySlips.stream()
                .map(slip -> slip.getPostingDate() != null ? slip.getPostingDate() : "")
                .filter(date -> !date.isEmpty())
                .collect(Collectors.toSet());
        int uniquePayDateCount = uniquePayDates.size();

        model.addAttribute("salarySlips", paginatedSalarySlips);
        model.addAttribute("selectedYear", year != null ? year : "");
        model.addAttribute("uniquePayDateCount", uniquePayDateCount);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("activePage", "payroll-months");

        return "emp/payroll-months";
    }

    @PostMapping("/months")
    public String filterPayrollMonths(
            @RequestParam(value = "year", required = false) String year,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int pageSize,
            Model model,
            HttpSession session) {
            
        if (!LoginService.isAuthenticated(session)) {
            return "redirect:/"; 
        }
        List<SalarySlip> salarySlips = payrollService.getMonthlyAggregatedSalarySlips(null, year);
        if (salarySlips == null) {
            salarySlips = new ArrayList<>();
        }

        // Sort salary slips by month (January to December) based on postingDate
        salarySlips.sort(Comparator.comparing(slip -> {
            String date = slip.getPostingDate() != null ? slip.getPostingDate() : "";
            if (date.isEmpty()) return "9999-99";
            try {
                String[] parts = date.split(" ");
                String monthName = parts[0];
                String yearPart = parts.length > 1 ? parts[1] : "0000";
                String monthNumber = FRENCH_MONTH_TO_NUMBER.getOrDefault(monthName, "99");
                return yearPart + "-" + monthNumber;
            } catch (Exception e) {
                logger.warn("Invalid postingDate format: {}", date);
                return "9999-99";
            }
        }));

        List<SalarySlip> paginatedSalarySlips = paginationService.getPaginatedItems(salarySlips, page, pageSize);
        int totalPages = paginationService.getTotalPages(salarySlips, pageSize);
        List<Integer> pageNumbers = paginationService.getPageNumbers(page, totalPages, 5);

        Set<String> uniquePayDates = salarySlips.stream()
                .map(slip -> slip.getPostingDate() != null ? slip.getPostingDate() : "")
                .filter(date -> !date.isEmpty())
                .collect(Collectors.toSet());
        int uniquePayDateCount = uniquePayDates.size();

        model.addAttribute("salarySlips", paginatedSalarySlips);
        model.addAttribute("selectedYear", year != null ? year : "");
        model.addAttribute("uniquePayDateCount", uniquePayDateCount);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("activePage", "payroll-months");

        return "emp/payroll-months";
    }

    @GetMapping("/components")
    public String showPayrollComponents(
            @RequestParam(value = "year") String year,
            @RequestParam(value = "month") String month,
            Model model,
            HttpSession session) {

        if (!LoginService.isAuthenticated(session)) {
            return "redirect:/"; 
        }

        try {
            PayrollComponentsResponse components = payrollService.getPayrollComponents(year, month);
            model.addAttribute("components", components);
            model.addAttribute("activePage", "payroll");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Erreur lors de la récupération des composants de salaire pour " + getMonthName(month) + " " + year);
            model.addAttribute("components", new PayrollComponentsResponse());
        }
        return "emp/salary-components";
    }

    @GetMapping("/components/employee")
    public String showPayrollComponentsEmp(
            @RequestParam(value = "year") String year,
            @RequestParam(value = "month") String month,
            @RequestParam(value = "employee") String employee,
            Model model,
            HttpSession session) {

        if (!LoginService.isAuthenticated(session)) {
            return "redirect:/"; 
        }
        try {
            PayrollComponentsResponse components = payrollService.getPayrollComponentsEmp(year, month, employee);
            model.addAttribute("components", components);
            model.addAttribute("employee", employee);
            model.addAttribute("activePage", "payroll");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Erreur lors de la récupération des composants de salaire pour l'employé " + employee + " pour " + getMonthName(month) + " " + year);
            model.addAttribute("components", new PayrollComponentsResponse());
        }
        return "emp/salary-components-emp";
    }

    @ExceptionHandler(IllegalStateException.class)
    public String handleIllegalStateException(IllegalStateException ex, Model model, HttpSession session) {
        if (!LoginService.isAuthenticated(session)) {
            return "redirect:/"; 
        }
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("salarySlips", new ArrayList<SalarySlip>());
        model.addAttribute("selectedYear", "");
        model.addAttribute("activePage", "payroll-months");
        return "emp/payroll-months";
    }

    @GetMapping("/dashboard")
    public String showDashboard(@RequestParam(value = "year", defaultValue = "2025") String year, Model model, HttpSession session) {
        if (!LoginService.isAuthenticated(session)) {
            return "redirect:/"; 
        }
        model.addAttribute("selectedYear", year);
        model.addAttribute("activePage", "dashboard");
        return "home/dashboard";
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