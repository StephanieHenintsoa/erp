package com.example.erp.controller.employee;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.example.erp.service.pagination.PaginationService;
import com.example.erp.service.salary.SalarySlipService;

@Controller
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    
    @Autowired
    private SalarySlipService salarySlipService;
    
    @Autowired
    private PaginationService<Employee> paginationService;

    @GetMapping("/employees")
    public String getAllEmployees(
            @RequestParam(required = false) String minDate,
            @RequestParam(required = false) String maxDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String designation,
            @RequestParam(required = false) String department,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            Model model) {
        List<Employee> allEmployees = employeeService.getAllEmployees(minDate, maxDate, status, designation, department);
        List<Employee> paginatedEmployees = paginationService.getPaginatedItems(allEmployees, page, pageSize);
        int totalPages = paginationService.getTotalPages(allEmployees, pageSize);        
        List<Integer> pageNumbers = paginationService.getPageNumbers(page, totalPages, 5);

        List<Designation> designations = employeeService.getAllDesignations();
        List<Department> departments = employeeService.getAllDepartments();
        List<String> statuses = Arrays.asList("Active", "Inactive", "Suspended", "Left");

        model.addAttribute("employees", paginatedEmployees);
        model.addAttribute("designations", designations);
        model.addAttribute("departments", departments);
        model.addAttribute("statuses", statuses);
        model.addAttribute("minDate", minDate);
        model.addAttribute("maxDate", maxDate);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedDesignation", designation);
        model.addAttribute("selectedDepartment", department);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageNumbers", pageNumbers);
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

        List<SalarySlip> salarySlips = salarySlipService.getSalarySlipsByEmployeeAndMonthYear(name, null, null);
        List<SalarySlip> detailedSlips = new ArrayList<>();
        for (SalarySlip slip : salarySlips) {
            SalarySlip detailedSlip = salarySlipService.getSalarySlipWithDetails(slip.getName());
            if (detailedSlip != null) {
                detailedSlips.add(detailedSlip);
            }
        }

        model.addAttribute("employee", employee);
        model.addAttribute("salarySlips", detailedSlips);
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
        
        Employee employee = employeeService.getEmployeeByName(name);
        model.addAttribute("employee", employee);
        
        if (employee == null) {
            model.addAttribute("error", "Employé non trouvé.");
            model.addAttribute("salarySlips", List.of());
            model.addAttribute("selectedMonth", month);
            model.addAttribute("selectedYear", year);
            return "emp/employee-payslips";
        }

        List<SalarySlip> salarySlips = new ArrayList<>();
        try {
            salarySlips = salarySlipService.getSalarySlipsByEmployeeAndMonthYear(name, month, year);
            List<SalarySlip> detailedSlips = new ArrayList<>();
            for (SalarySlip slip : salarySlips) {
                SalarySlip detailedSlip = salarySlipService.getSalarySlipWithDetails(slip.getName());
                if (detailedSlip != null) {
                    detailedSlips.add(detailedSlip);
                }
            }
            salarySlips = detailedSlips;
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors de la récupération des fiches de paie");
        }

        model.addAttribute("salarySlips", salarySlips);
        model.addAttribute("selectedMonth", month);
        model.addAttribute("selectedYear", year);
        model.addAttribute("activePage", "employees");
        
        return "emp/employee-payslips";
    }
}