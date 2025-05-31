package com.example.erp.controller.employee;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.erp.entity.Department;
import com.example.erp.entity.Designation;
import com.example.erp.entity.Employee;
import com.example.erp.service.employee.EmployeeService;

@Controller
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

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
        return "/emp/employee-details";
    }
}