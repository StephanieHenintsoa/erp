Given those API endpoint and the JSOn data returned from Frappe HR App : 

GET http://erpnext.localhost:8000/api/resource/Salary Slip?fields=["*"]
            
{
    "data": [
        {
            "name": "Sal Slip/HR-EMP-00005/00001",
            "owner": "Administrator",
            "creation": "2025-06-01 20:52:03.298117",
            "modified": "2025-06-01 20:52:14.849999",
            "modified_by": "Administrator",
            "docstatus": 1,
            "idx": 2,
            "employee": "HR-EMP-00005",
            "employee_name": "Mariah",
            "company": "ITU (Demo)",
            ...
        },
        ...
    ]
}

required fields from this JSON data : 
- name
- employee
- employee_name
- gross pay
- net_pay


---
---
---

GET http://erpnext.localhost:8000/api/resource/Salary Slip/Sal Slip/HR-EMP-00005/00001?fields=["*"]

{
    "data": {
        "name": "Sal Slip/HR-EMP-00005/00001",
        "owner": "Administrator",
        "docstatus": 1,
        "idx": 2,
        "employee": "HR-EMP-00005",
        "employee_name": "Mariah",
        "company": "ITU (Demo)",
        "department": "Management - ITUD",
        ... 
        "doctype": "Salary Slip",
        "timesheets": [],
        "earnings": [
            {
                "name": "g53quk5sbt",
                "owner": "Administrator",
                "creation": "2025-06-01 20:52:03.298117",
                "modified": "2025-06-01 20:52:14.849999",
                "modified_by": "Administrator",
                "docstatus": 1,
                "idx": 1,
                "salary_component": "S_mah",
                "abbr": "smah",
                "amount": 500.0,
                "year_to_date": 500.0,
                ...
            }
        ],
        "deductions": [
            {
                "name": "g53oh2cejp",
                "owner": "Administrator",
                "creation": "2025-06-01 20:52:03.298117",
                "modified": "2025-06-01 20:52:14.849999",
                "modified_by": "Administrator",
                "docstatus": 1,
                "idx": 1,
                "salary_component": "Income Tax",
                "abbr": "IT",
                "amount": 50.0,
                "year_to_date": 50.0,
                ...
            }
        ],
        "leave_details": []
    }
}

required fields from this JSON data : 
- earnings 
    - name
    - amount
    - salary_component
    - abbr

- deductions
    - name
    - amount
    - salary_component
    - abbr

---
---
---


the goal is to display more details about the salary slips :
- the earnings and deductions fields

use those api endpoint, retrieve only the required mentionned fields and udpate my current code to display the earnings and deductions fields on the employee-payslips.html

Dont re-write the whole code, just give me the prt to be updated or the new code part to add. tell me just where to paste it

---

package com.example.erp.controller.employee;

import ...


@Controller
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    
    @Autowired
    private SalarySlipService salarySlipService;

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
        
       
        Employee employee = employeeService.getEmployeeByName(name);
        model.addAttribute("employee", employee);
        
        if (employee == null) {
            model.addAttribute("error", "Employé non trouvé.");
            return "emp/employee-payslips";
        }

        List<SalarySlip> salarySlips = new ArrayList<>();
        
        try {
            salarySlips = salarySlipService.getSalarySlipsByEmployeeAndMonthYear(name, month, year);
            
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors de la récupération des fiches de paie");
        }

        model.addAttribute("salarySlips", salarySlips);
        model.addAttribute("selectedMonth", month);
        model.addAttribute("selectedYear", year);
        
        return "emp/employee-payslips";
    }
}


---


package com.example.erp.service.salary;

import ...

@Service
public class SalarySlipService {

    private static final Logger logger = LoggerFactory.getLogger(SalarySlipService.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public List<SalarySlip> getAllSalarySlips(String employee, String startDate, String endDate, String status) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + ErpNextConfig.API_KEY + ":" + ErpNextConfig.API_SECRET);
        headers.set("Content-Type", "application/json");

        List<String> fieldsList = Arrays.asList(
            "name", "posting_date", "start_date", "end_date", "status",
            "total_deduction", "total_earnings", "net_pay", "gross_pay"
        );

        List<List<String>> filters = new ArrayList<>();
        if (employee != null && !employee.isEmpty()) {
            filters.add(Arrays.asList("employee", "=", employee));
        }
        if (startDate != null && !startDate.isEmpty()) {
            filters.add(Arrays.asList("start_date", "=", startDate));
        }
        if (endDate != null && !endDate.isEmpty()) {
            filters.add(Arrays.asList("end_date", "=", endDate));
        }
        if (status != null && !status.isEmpty()) {
            filters.add(Arrays.asList("status", "=", status));
        }

        try {
            String fieldsJson = objectMapper.writeValueAsString(fieldsList);
            String url = ErpNextConfig.ERP_NEXT_API_SALARY_SLIP_URL;
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("fields", fieldsJson);

            if (!filters.isEmpty()) {
                String filtersJson = objectMapper.writeValueAsString(filters);
                builder.queryParam("filters", filtersJson);
            }

            String finalUrl = builder.build(false).toUriString();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<SalarySlipResponse> response = restTemplate.exchange(finalUrl, HttpMethod.GET, entity, SalarySlipResponse.class);
            return response.getBody() != null ? response.getBody().getData() : new ArrayList<>();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error processing JSON for ERPNext Salary Slip API call", e);
        }
    }

    public List<SalarySlip> getSalarySlipsByEmployeeAndDate(String employee, String payslipDate) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + ErpNextConfig.API_KEY + ":" + ErpNextConfig.API_SECRET);
        headers.set("Content-Type", "application/json");

        List<String> fieldsList = Arrays.asList(
            "name", "posting_date", "start_date", "end_date", "status",
            "total_deduction", "total_earnings", "net_pay", "gross_pay", "employee"
        );

        List<List<String>> filters = new ArrayList<>();
        
        if (employee != null && !employee.isEmpty()) {
            filters.add(Arrays.asList("employee", "=", employee));
        }
        
        if (payslipDate != null && !payslipDate.isEmpty()) {
            filters.add(Arrays.asList("posting_date", "=", payslipDate));
        }

        try {
            String fieldsJson = objectMapper.writeValueAsString(fieldsList);
            String url = ErpNextConfig.ERP_NEXT_API_SALARY_SLIP_URL;
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("fields", fieldsJson)
                    .queryParam("order_by", "posting_date desc");

            if (!filters.isEmpty()) {
                String filtersJson = objectMapper.writeValueAsString(filters);
                builder.queryParam("filters", filtersJson);
            }

            String finalUrl = builder.build(false).toUriString();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<SalarySlipResponse> response = restTemplate.exchange(
                finalUrl, HttpMethod.GET, entity, SalarySlipResponse.class);
            
            return response.getBody() != null ? response.getBody().getData() : new ArrayList<>();
            
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error processing JSON for ERPNext Salary Slip API call", e);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching salary slips for employee: " + employee + " on date: " + payslipDate, e);
        }
    }

    public SalarySlip getSalarySlipByName(String name) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + ErpNextConfig.API_KEY + ":" + ErpNextConfig.API_SECRET);
        headers.set("Content-Type", "application/json");
    
        List<String> fieldsList = Arrays.asList(
            "name", "posting_date", "start_date", "end_date", "status",
            "total_deduction", "total_earnings", "net_pay", "gross_pay"
        );
    
        try {
    
            String fieldsJson = objectMapper.writeValueAsString(fieldsList);
    
            // Construction manuelle de l'URL sans encoder le path (le nom contient des espaces)
            String baseUrl = ErpNextConfig.ERP_NEXT_API_SALARY_SLIP_URL + "/" + name;
    
            // UriComponentsBuilder va encoder uniquement les query params (ce qu'on veut)
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl)
                    .queryParam("fields", fieldsJson);
    
            String finalUrl = builder.build(false).toUriString(); // false → ne pas encoder le chemin
    
    
            logger.debug("Fetching salary slip from ERP Next: {}", finalUrl);
    
            HttpEntity<String> entity = new HttpEntity<>(headers);
    
            ResponseEntity<SingleSalarySlipResponse> response = restTemplate.exchange(
                finalUrl, HttpMethod.GET, entity, SingleSalarySlipResponse.class
            );
    
            if (response.getBody() != null && response.getBody().getData() != null) {
                logger.debug("Salary slip retrieved: {}", response.getBody().getData().getName());
                return response.getBody().getData();
            } else {
                logger.warn("No salary slip found for name: {}", name);
                return null;
            }
    
        } catch (HttpClientErrorException e) {
            return null;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error processing JSON for ERPNext Salary Slip API call", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch salary slip", e);
        }
    }

    public List<SalarySlip> getSalarySlipsByEmployeeAndMonthYear(String employee, String month, String year) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + ErpNextConfig.API_KEY + ":" + ErpNextConfig.API_SECRET);
        headers.set("Content-Type", "application/json");
    
        // Define fields to retrieve from Salary Slip
        List<String> fieldsList = Arrays.asList(
                "name", "posting_date", "start_date", "end_date", "status",
                "total_deduction", "total_earnings", "net_pay", "gross_pay", "employee"
        );
    
        // Build filters dynamically
        List<List<String>> filters = new ArrayList<>();
    
        // Add employee filter if provided
        if (employee != null && !employee.isEmpty()) {
            filters.add(Arrays.asList("employee", "=", employee));
        }
    
        // Add date-based filters based on provided month and/or year
        if (month != null && !month.isEmpty() && year != null && !year.isEmpty()) {
            // Case 1: Both month and year provided
            String startDate = year + "-" + month + "-01";
            int lastDay = getLastDayOfMonth(Integer.parseInt(month), Integer.parseInt(year));
            String endDate = year + "-" + month + "-" + String.format("%02d", lastDay);
            filters.add(Arrays.asList("posting_date", ">=", startDate));
            filters.add(Arrays.asList("posting_date", "<=", endDate));
        } else if (month != null && !month.isEmpty()) {
            // Case 2: Only month provided - use BETWEEN with current year
            int currentYear = LocalDate.now().getYear();
            String startDate = currentYear + "-" + month + "-01";
            int lastDay = getLastDayOfMonth(Integer.parseInt(month), currentYear);
            String endDate = currentYear + "-" + month + "-" + String.format("%02d", lastDay);
            filters.add(Arrays.asList("posting_date", ">=", startDate));
            filters.add(Arrays.asList("posting_date", "<=", endDate));
        } else if (year != null && !year.isEmpty()) {
            // Case 3: Only year provided - use BETWEEN with full year
            String startDate = year + "-01-01";
            String endDate = year + "-12-31";
            filters.add(Arrays.asList("posting_date", ">=", startDate));
            filters.add(Arrays.asList("posting_date", "<=", endDate));
        }
    
        try {
            // Convert fields to JSON for API query
            String fieldsJson = objectMapper.writeValueAsString(fieldsList);
            String url = ErpNextConfig.ERP_NEXT_API_SALARY_SLIP_URL;
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("fields", fieldsJson)
                    .queryParam("order_by", "posting_date desc");
    
            // Add filters to query if any exist
            if (!filters.isEmpty()) {
                String filtersJson = objectMapper.writeValueAsString(filters);
                builder.queryParam("filters", filtersJson);
            }
    
            String finalUrl = builder.build(false).toUriString();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<SalarySlipResponse> response = restTemplate.exchange(
                    finalUrl, HttpMethod.GET, entity, SalarySlipResponse.class);
    
            return response.getBody() != null ? response.getBody().getData() : new ArrayList<>();
    
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error processing JSON for ERPNext Salary Slip API call", e);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching salary slips for employee: " + employee + " for month: " + month + "/" + year, e);
        }
    }
    // Utility method to calculate the last day of a given month and year
    private int getLastDayOfMonth(int month, int year) {
        switch (month) {
            case 1: case 3: case 5: case 7: case 8: case 10: case 12:
                return 31;
            case 4: case 6: case 9: case 11:
                return 30;
            case 2:
                if ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)) {
                    return 29;
                } else {
                    return 28;
                }
            default:
                return 31; // Fallback for invalid month
        }
    }    
}


----

<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Fiche Employé - Fiches de Paie</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="/css/style.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css">
    <style>
        .main-container {
            display: flex;
        }
        .content-wrapper {
            flex-grow: 1;
            padding: 20px;
        }
        .filter-container {
            background-color: #f8f9fa;
            padding: 15px;
            border-radius: 5px;
            margin-bottom: 20px;
        }
        .nav-item.active a {
            font-weight: bold;
            color: #007bff;
        }
        .card {
            margin-top: 20px;
        }
        .card-title {
            font-size: 1.5rem;
        }
        .payslip-card {
            border-left: 4px solid #1d3043;
            margin-bottom: 15px;
            transition: box-shadow 0.3s ease;
        }
        .payslip-card:hover {
            box-shadow: 0 4px 8px rgba(0,0,0,0.1);
        }
        .payslip-header {
            background-color: #f8f9fa;
            padding: 15px;
            border-bottom: 1px solid #dee2e6;
        }
        .amount-positive {
            color: #28a745;
            font-weight: bold;
        }
        .amount-negative {
            color: #dc3545;
            font-weight: bold;
        }
        .amount-neutral {
            color: #333;
            font-weight: bold;
        }
        .status-badge {
            font-size: 0.875rem;
        }
        .employee-info-card {
            background: linear-gradient(135deg, #111112 0%, #4b7ca2 100%);
            color: white;
        }
        .employee-info-card .card-body {
            background: rgba(255,255,255,0.1);
            backdrop-filter: blur(10px);
        }
        .filter-active {
            border-left: 4px solid #28a745;
        }
        .download-btn {
            transition: all 0.3s ease;
        }
        .download-btn:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(0,123,255,0.3);
        }
    </style>
</head>
<body>
    <div class="main-container">
        <!-- Sidebar -->
        <div th:replace="~{fragments/sidebar :: sidebar}"></div>

        <!-- Content -->
        <div class="content-wrapper">
            <!-- Employee Title -->
            <div class="d-flex justify-content-between align-items-center mb-4">
                <h1>
                    <i class="fas fa-user-circle text-primary"></i>
                    <span th:if="${employee != null}" th:text="'Fiche Employé - ' + ${employee.firstName ?: 'N/A'}">Fiche Employé</span>
                    <span th:unless="${employee != null}">Fiche Employé - Non Trouvé</span>
                </h1>
                <a th:href="@{/employees}" class="btn btn-outline-secondary">
                    <i class="fas fa-arrow-left"></i> Retour à la Liste
                </a>
            </div>

            <!-- Error Alert -->
            <div th:if="${error != null}" class="alert alert-danger alert-dismissible fade show" role="alert">
                <i class="fas fa-exclamation-triangle"></i>
                <span th:text="${error}"></span>
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>

            <!-- Employee Info Card -->
            <div class="card employee-info-card" th:if="${employee != null}">
                <div class="card-header">
                    <h5 class="mb-0"><i class="fas fa-id-card"></i> Informations Employé</h5>
                </div>
                <div class="card-body">
                    <div class="row">
                        <div class="col-md-6">
                            <dl class="row">
                                <dt class="col-sm-6">Nom d'utilisateur</dt>
                                <dd class="col-sm-6" th:text="${employee.name ?: 'N/A'}">N/A</dd>

                                <dt class="col-sm-6">Nom complet</dt>
                                <dd class="col-sm-6" th:text="${employee.employeeName ?: 'N/A'}">N/A</dd>

                                <dt class="col-sm-6">Prénom</dt>
                                <dd class="col-sm-6" th:text="${employee.firstName ?: 'N/A'}">N/A</dd>

                                <dt class="col-sm-6">Genre</dt>
                                <dd class="col-sm-6" th:text="${employee.gender ?: 'N/A'}">N/A</dd>

                                <dt class="col-sm-6">Date de naissance</dt>
                                <dd class="col-sm-6" th:text="${employee.dateOfBirth ?: 'N/A'}">N/A</dd>
                            </dl>
                        </div>
                        <div class="col-md-6">
                            <dl class="row">
                                <dt class="col-sm-6">Date d'embauche</dt>
                                <dd class="col-sm-6" th:text="${employee.dateOfJoining ?: 'N/A'}">N/A</dd>

                                <dt class="col-sm-6">Entreprise</dt>
                                <dd class="col-sm-6" th:text="${employee.company ?: 'N/A'}">N/A</dd>

                                <dt class="col-sm-6">Département</dt>
                                <dd class="col-sm-6" th:text="${employee.department ?: 'N/A'}">N/A</dd>

                                <dt class="col-sm-6">Poste</dt>
                                <dd class="col-sm-6" th:text="${employee.designation ?: 'N/A'}">N/A</dd>

                                <dt class="col-sm-6">Statut</dt>
                                <dd class="col-sm-6">
                                    <span class="badge" 
                                          th:classappend="${employee.status == 'Active'} ? 'bg-success' : 'bg-danger'"
                                          th:text="${employee.status ?: 'N/A'}">N/A</span>
                                </dd>
                            </dl>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Date Filter Section -->
            <div class="filter-container" th:classappend="${(selectedMonth != null and !selectedMonth.isEmpty()) or (selectedYear != null and !selectedYear.isEmpty())} ? 'filter-active' : ''">
                <h5><i class="fas fa-filter"></i> Filtrer les Fiches de Paie</h5>
                <form method="post" th:action="@{/employees/{name}/payslips(name=${employee?.name ?: 'unknown'})}">
                    <div class="row g-3">
                        <div class="col-md-4">
                            <label for="month" class="form-label">Mois</label>
                            <select class="form-select" id="month" name="month">
                                <option value="" th:selected="${selectedMonth == null or selectedMonth.isEmpty()}">-- Tous les mois --</option>
                                <option value="01" th:selected="${selectedMonth == '01'}">Janvier</option>
                                <option value="02" th:selected="${selectedMonth == '02'}">Février</option>
                                <option value="03" th:selected="${selectedMonth == '03'}">Mars</option>
                                <option value="04" th:selected="${selectedMonth == '04'}">Avril</option>
                                <option value="05" th:selected="${selectedMonth == '05'}">Mai</option>
                                <option value="06" th:selected="${selectedMonth == '06'}">Juin</option>
                                <option value="07" th:selected="${selectedMonth == '07'}">Juillet</option>
                                <option value="08" th:selected="${selectedMonth == '08'}">Août</option>
                                <option value="09" th:selected="${selectedMonth == '09'}">Septembre</option>
                                <option value="10" th:selected="${selectedMonth == '10'}">Octobre</option>
                                <option value="11" th:selected="${selectedMonth == '11'}">Novembre</option>
                                <option value="12" th:selected="${selectedMonth == '12'}">Décembre</option>
                            </select>
                        </div>
                        <div class="col-md-4">
                            <label for="year" class="form-label">Année</label>
                            <select class="form-select" id="year" name="year">
                                <option value="" th:selected="${selectedYear == null or selectedYear.isEmpty()}">-- Toutes les années --</option>
                                <option th:each="y : ${#numbers.sequence(2020, 2025)}" 
                                        th:value="${y}" 
                                        th:text="${y}" 
                                        th:selected="${selectedYear != null and selectedYear == #strings.toString(y)}"></option>
                            </select>
                        </div>
                        <div class="col-md-4 d-flex align-items-end">
                            <button type="submit" class="btn btn-primary me-2">
                                <i class="fas fa-search"></i> Filtrer
                            </button>
                            <a th:href="@{/employees/{name}/payslips(name=${employee?.name ?: 'unknown'})}" class="btn btn-outline-secondary">
                                <i class="fas fa-times"></i> Effacer
                            </a>
                        </div>
                    </div>
                </form>
                
                <!-- Active Filters Display -->
                <div th:if="${(selectedMonth != null and !selectedMonth.isEmpty()) or (selectedYear != null and !selectedYear.isEmpty())}" class="mt-3">
                    <small class="text-muted">Filtres actifs:</small>
                    <span th:if="${selectedMonth != null and !selectedMonth.isEmpty()}" class="badge bg-info me-1">
                        Mois: <span th:text="${selectedMonth}"></span>
                    </span>
                    <span th:if="${selectedYear != null and !selectedYear.isEmpty()}" class="badge bg-info me-1">
                        Année: <span th:text="${selectedYear}"></span>
                    </span>
                </div>
            </div>

            <!-- Payslips Section -->
            <div class="card">
                <div class="card-header d-flex justify-content-between align-items-center">
                    <h5 class="mb-0">
                        <i class="fas fa-file-invoice-dollar"></i> Fiches de Paie
                        <span class="badge bg-secondary ms-2" th:text="${salarySlips != null ? #lists.size(salarySlips) : 0}">0</span>
                    </h5>
                    <div th:if="${salarySlips != null and !#lists.isEmpty(salarySlips)}">
                        <small class="text-muted">
                            Total: <span th:text="${#lists.size(salarySlips)}"></span> fiche(s) trouvée(s)
                        </small>
                    </div>
                </div>
                <div class="card-body">
                    <!-- No payslips found -->
                    <div th:if="${salarySlips == null or #lists.isEmpty(salarySlips)}" class="text-center py-5">
                        <i class="fas fa-inbox fa-3x text-muted mb-3"></i>
                        <h5 class="text-muted">Aucune fiche de paie trouvée</h5>
                        <p class="text-muted">
                            <span th:if="${(selectedMonth != null and !selectedMonth.isEmpty()) or (selectedYear != null and !selectedYear.isEmpty())}">
                                Aucune fiche de paie pour les critères sélectionnés.
                            </span>
                            <span th:unless="${(selectedMonth != null and !selectedMonth.isEmpty()) or (selectedYear != null and !selectedYear.isEmpty())}">
                                Utilisez les filtres ci-dessus pour rechercher des fiches de paie.
                            </span>
                        </p>
                    </div>

                    <!-- Payslips List -->
                    <div th:unless="${salarySlips == null or #lists.isEmpty(salarySlips)}">
                        <div th:each="slip : ${salarySlips}" class="payslip-card card">
                            <div class="payslip-header">
                                <div class="row align-items-center">
                                    <div class="col-md-5">
                                        <h6 class="mb-1" th:text="${slip.name ?: 'N/A'}">Fiche de Paie</h6>
                                        <small class="text-muted">
                                            <i class="fas fa-calendar-alt"></i>
                                            Période: <span th:text="${slip.startDate ?: 'N/A'}"></span> - <span th:text="${slip.endDate ?: 'N/A'}"></span>
                                        </small>
                                    </div>
                                    <div class="col-md-4 text-center">
                                        <small class="text-muted">Date de publication:</small><br>
                                        <strong th:text="${slip.postingDate ?: 'N/A'}"></strong>
                                    </div>
                                    <div class="col-md-3 text-end">
                                        <span class="badge status-badge"
                                              th:classappend="${slip.status == 'Submitted'} ? 'bg-success' : (${slip.status == 'Draft'} ? 'bg-warning text-dark' : 'bg-danger')"
                                              th:text="${slip.status == 'Submitted'} ? 'Validé' : (${slip.status == 'Draft'} ? 'Brouillon' : ${slip.status ?: 'Inconnu'})">N/A</span>
                                    </div>
                                </div>
                            </div>
                            <div class="card-body">
                                <div class="row mb-3">
                                    <div class="col-md-3">
                                        <div class="text-center p-2 bg-light rounded">
                                            <small class="text-muted d-block">Salaire Brut</small>
                                            <div class="amount-positive fs-6" th:text="${slip.grossPay != null ? (#numbers.formatDecimal(slip.grossPay, 1, 2) + ' €') : '0,00 €'}">0,00 €</div>
                                        </div>
                                    </div>
                                    <div class="col-md-3">
                                        <div class="text-center p-2 bg-light rounded">
                                            <small class="text-muted d-block">Total Gains</small>
                                            <div class="amount-positive fs-6" th:text="${slip.totalEarnings != null ? (#numbers.formatDecimal(slip.totalEarnings, 1, 2) + ' €') : '0,00 €'}">0,00 €</div>
                                        </div>
                                    </div>
                                    <div class="col-md-3">
                                        <div class="text-center p-2 bg-light rounded">
                                            <small class="text-muted d-block">Total Déductions</small>
                                            <div class="amount-negative fs-6" th:text="${slip.totalDeduction != null ? (#numbers.formatDecimal(slip.totalDeduction, 1, 2) + ' €') : '0,00 €'}">0,00 €</div>
                                        </div>
                                    </div>
                                    <div class="col-md-3">
                                        <div class="text-center p-3 bg-primary text-white rounded">
                                            <small class="d-block opacity-75">Salaire Net</small>
                                            <div class="fw-bold fs-5" th:text="${slip.netPay != null ? (#numbers.formatDecimal(slip.netPay, 1, 2) + ' €') : '0,00 €'}">0,00 €</div>
                                        </div>
                                    </div>
                                </div>
                                
                                <!-- Download Form -->
                                <div th:if="${employee != null and slip != null}" class="text-center">
                                    <form action="/api/payslips/download" method="post" target="_blank" class="download-form">
                                        <input type="hidden" name="employeeName" th:value="${employee.name}" />
                                        <input type="hidden" name="slipName" th:value="${slip.name}" />
                                        
                                        <button type="submit" class="btn btn-primary download-btn">
                                            <i class="fas fa-download"></i> Télécharger le Bulletin (PDF)
                                        </button>
                                    </form>
                                </div>
                                
                                <!-- Warning if data is missing -->
                                <div th:unless="${employee != null and slip != null}" class="alert alert-warning text-center">
                                    <i class="fas fa-exclamation-triangle"></i>
                                    Téléchargement indisponible - Données manquantes
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Footer -->
    <div th:replace="~{fragments/footer :: footer}"></div>

    <!-- Bootstrap JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>



---

package com.example.erp.entity.salary;

import com.example.erp.entity.salary.SalaryDetail;

import java.beans.Transient;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.itextpdf.layout.element.List;

public class SalarySlip {

    @JsonProperty("name")
    private String name;

    @JsonProperty("posting_date")
    private String postingDate;

    @JsonProperty("start_date")
    private String startDate;

    @JsonProperty("end_date")
    private String endDate;

    @JsonProperty("status")
    private String status;

    @JsonProperty("total_deduction")
    private Double totalDeduction;

    @JsonProperty("total_earnings")
    private Double totalEarnings;

    @JsonProperty("net_pay")
    private Double netPay;

    @JsonProperty("gross_pay")
    private Double grossPay;

    @JsonProperty("employee")
    private String employee;

    // Getters and Setters pour les nouveaux champs
    public String getEmployee() {
        return employee;
    }

    public void setEmployee(String employee) {
        this.employee = employee;
    }

    // getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPostingDate() {
        return postingDate;
    }

    public void setPostingDate(String postingDate) {
        this.postingDate = postingDate;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getTotalDeduction() {
        return totalDeduction;
    }

    public void setTotalDeduction(Double totalDeduction) {
        this.totalDeduction = totalDeduction;
    }

    public Double getTotalEarnings() {
        return totalEarnings;
    }

    public void setTotalEarnings(Double totalEarnings) {
        this.totalEarnings = totalEarnings;
    }

    public Double getNetPay() {
        return netPay;
    }

    public void setNetPay(Double netPay) {
        this.netPay = netPay;
    }

    public Double getGrossPay() {
        return grossPay;
    }

    public void setGrossPay(Double grossPay) {
        this.grossPay = grossPay;
    }
   
}