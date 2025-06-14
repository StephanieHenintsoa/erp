package com.example.erp.controller.employee;


import com.example.erp.entity.Employee;
import com.example.erp.service.auth.LoginService;
import com.example.erp.service.employee.EmployeeService;
import com.example.erp.service.salary.PayslipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
public class PayslipController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private PayslipService payslipService;

   
    @GetMapping("/payslips/generate")
    public String showGeneratePayslipForm(Model model, HttpSession session) {
        if (!LoginService.isAuthenticated(session)) {
            return "redirect:/";
        }
        List<Employee> employees = employeeService.getAllEmployees();
        model.addAttribute("employees", employees);
        model.addAttribute("activePage", "payslips");
        return "emp/generate-payslips";
    }

    
    @PostMapping("/payslips/generate")
    public String generatePayslips(
            @RequestParam String employee,
            @RequestParam String startMonth,
            @RequestParam int startYear,
            @RequestParam String endMonth,
            @RequestParam int endYear,
            @RequestParam(required = false) Double baseSalary,
            Model model,
            HttpSession session) {
        if (!LoginService.isAuthenticated(session)) {
            return "redirect:/";
        }

        try {
            List<String> generatedPayslips = payslipService.generatePayslips(
                    employee, startMonth, startYear, endMonth, endYear, baseSalary);
            model.addAttribute("success", "Fiches de paie générées avec succès : " + String.join(", ", generatedPayslips));
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors de la génération des fiches de paie : " + e.getMessage());
        }

        model.addAttribute("employees", employeeService.getAllEmployees());
        model.addAttribute("activePage", "payslips");
        return "emp/generate-payslips";
    }
}