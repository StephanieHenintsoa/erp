package com.example.erp.controller.employee;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.erp.entity.Department;
import com.example.erp.entity.Designation;
import com.example.erp.entity.Employee;
import com.example.erp.entity.salary.SalarySlip;
import com.example.erp.service.employee.EmployeeService;
import com.example.erp.service.salary.SalarySlipService;
import com.example.erp.service.salary.SalaryStructureAssignmentService;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.example.erp.service.salary.SalaryComponentService;



@Controller
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    
    @Autowired
    private SalarySlipService salarySlipService;
    
    @Autowired
    private SalaryComponentService salaryComponentService;
    
    @Autowired
    private SalaryStructureAssignmentService salaryStructureAssignmentService;

    @GetMapping("/employees")
    public String getAllEmployees(
            @RequestParam(required = false) String minDate,
            @RequestParam(required = false) String maxDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String designation,
            @RequestParam(required = false) String department,
            Model model) {
        List<Employee> employees = employeeService.getAllEmployees(minDate, maxDate, status, designation, department);
        List<Designation> designations = employeeService.getAllDesignations();
        List<Department> departments = employeeService.getAllDepartments();
        List<String> statuses = Arrays.asList("Active", "Inactive", "Suspended", "Left");

        model.addAttribute("employees", employees);
        model.addAttribute("designations", designations);
        model.addAttribute("departments", departments);
        model.addAttribute("statuses", statuses);
        model.addAttribute("minDate", minDate);
        model.addAttribute("maxDate", maxDate);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedDesignation", designation);
        model.addAttribute("selectedDepartment", department);
        model.addAttribute("activePage", "employees");

        return "emp/employees";
    }

    @GetMapping("/employees/{name}")
    public String getEmployeeDetails(@PathVariable String name, Model model) {
        Employee employee = employeeService.getEmployeeByName(name);
        model.addAttribute("employee", employee);
        model.addAttribute("activePage", "employees");
        return "emp/employee-details";
    }

    @GetMapping("/employees/{name}/payslips")
    public String showEmployeePayslips(
            @PathVariable String name,
            Model model) {
        
        Employee employee = employeeService.getEmployeeByName(name);
        if (employee == null) {
            model.addAttribute("error", "Employé non trouvé.");
            model.addAttribute("salarySlips", List.of());
            model.addAttribute("selectedMonth", "");
            model.addAttribute("selectedYear", "");
            return "emp/employee-payslips";
        }

        model.addAttribute("employee", employee);
        model.addAttribute("salarySlips", List.of()); 
        model.addAttribute("selectedMonth", "");
        model.addAttribute("selectedYear", "");
        model.addAttribute("activePage", "employees");

        return "emp/employee-payslips";
    }

    @PostMapping("/employees/{name}/payslips")
    public String filterEmployeePayslips(
            @PathVariable String name,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) String year,
            Model model) {
        
        System.out.println("=== DEBUT CONTROLLEUR ===");
        System.out.println("Paramètres reçus - name: " + name + ", month: " + month + ", year: " + year);

        Employee employee = employeeService.getEmployeeByName(name);
        model.addAttribute("employee", employee);
        
        if (employee == null) {
            System.out.println("Employee non trouvé");
            model.addAttribute("error", "Employé non trouvé.");
            return "emp/employee-payslips";
        }

        List<SalarySlip> salarySlips = new ArrayList<>();
        
        try {
            salarySlips = salarySlipService.getSalarySlipsByEmployeeAndMonthYear(name, month, year);
            System.out.println("Nombre de fiches trouvées: " + salarySlips.size());
            
        } catch (Exception e) {
            System.out.println("Erreur lors de la récupération des fiches: " + e.getMessage());
            model.addAttribute("error", "Erreur lors de la récupération des fiches de paie");
        }

        model.addAttribute("salarySlips", salarySlips);
        model.addAttribute("selectedMonth", month);
        model.addAttribute("selectedYear", year);
        
        System.out.println("=== FIN CONTROLLEUR ===");
        return "emp/employee-payslips";
    }
}