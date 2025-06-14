package com.example.erp.service.salary;

import com.example.erp.config.ErpNextConfig;
import com.example.erp.entity.salary.SalarySlip;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service pour générer des fiches de paie dans ERPNext.
 */
@Service
public class PayslipService {

    public static final Logger logger = LoggerFactory.getLogger(PayslipService.class);

    public final RestTemplate restTemplate;
    public final ObjectMapper objectMapper;
    public final SalarySlipService salarySlipService;
    public final SalaryStructureAssignmentService salaryStructureAssignmentService;
    public final ErpNextConfig erpNextConfig;

    public static final String DEFAULT_SALARY_STRUCTURE = "g1";
    public static final String DEFAULT_COMPANY = "ORINASA SA";

    @Autowired
    public PayslipService(RestTemplate restTemplate, ObjectMapper objectMapper,
                          SalarySlipService salarySlipService,
                          SalaryStructureAssignmentService salaryStructureAssignmentService,
                          ErpNextConfig erpNextConfig) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.salarySlipService = salarySlipService;
        this.salaryStructureAssignmentService = salaryStructureAssignmentService;
        this.erpNextConfig = erpNextConfig;
    }

    /**
     * Génère des fiches de paie pour un employé sur une période donnée.
     * @param employeeName Nom de l'employé.
     * @param startMonth Mois de début (01-12).
     * @param startYear Année de début.
     * @param endMonth Mois de fin (01-12).
     * @param endYear Année de fin.
     * @param baseSalary Salaire de base (optionnel).
     * @return Liste des fiches de paie générées.
     */
    public List<String> generatePayslips(String employeeName, String startMonth, int startYear,
                                     String endMonth, int endYear, Double baseSalary) {
    // Validation des paramètres
    validateParameters(employeeName, startMonth, startYear, endMonth, endYear);

    YearMonth start = YearMonth.of(startYear, Integer.parseInt(startMonth));
    YearMonth end = YearMonth.of(endYear, Integer.parseInt(endMonth));

    if (start.isAfter(end)) {
        throw new IllegalArgumentException("La date de début doit être antérieure ou égale à la date de fin.");
    }

    List<String> generatedPayslips = new ArrayList<>();
    HttpHeaders headers = createHttpHeaders();

    // Déterminer le salaire de référence
    Double referenceSalary = determineSalaryToUse(employeeName, baseSalary, start, end);

    YearMonth current = start;
    while (!current.isAfter(end)) {
        try {
            // Vérifier si fiche de paie déjà existante
            List<SalarySlip> existingSlips = salarySlipService.getSalarySlipsByEmployeeAndMonthYear(
                    employeeName,
                    String.format("%02d", current.getMonthValue()),
                    String.valueOf(current.getYear()));

            if (!existingSlips.isEmpty()) {
                logger.info("Fiche de paie déjà existante pour {} - {}/{}. Ignorée.",
                        employeeName, current.getMonthValue(), current.getYear());
                current = current.plusMonths(1);
                continue;
            }

            // Créer ou s'assurer de l'existence d'une SSA pour le mois en cours
            ensureSalaryStructureAssignment(employeeName, current, referenceSalary);

            // Générer la fiche de paie
            if (processMonthlyPayslip(employeeName, current, referenceSalary, headers)) {
                generatedPayslips.add(String.format("Fiche de paie pour %02d/%d",
                        current.getMonthValue(), current.getYear()));
            }

        } catch (Exception e) {
            logger.error("Erreur lors de la génération de la fiche de paie pour {} {}/{}: {}",
                    employeeName, current.getMonthValue(), current.getYear(), e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la génération de la fiche de paie pour " +
                    current.getMonthValue() + "/" + current.getYear(), e);
        }
        current = current.plusMonths(1);
    }

    return generatedPayslips;
}


    /**
     * S'assure qu'une Salary Structure Assignment existe pour l'employé.
     */private void ensureSalaryStructureAssignment(String employeeName, YearMonth period, Double baseSalary) {
    if (hasSalaryStructureAssignment(employeeName, period)) {
        logger.info("SSA déjà existante pour {} - {}/{}", employeeName,
                period.getMonthValue(), period.getYear());
        return;
    }

    String salaryStructureName = ensureSalaryStructureExists();
    String fromDate = period.getYear() + "-" + String.format("%02d", period.getMonthValue()) + "-01";

    logger.info("Création d'une SSA pour {} à partir du {} avec salaire {}", employeeName, fromDate, baseSalary);

    Map<String, Object> result = salaryStructureAssignmentService.createSalaryStructureAssignment(
            employeeName, salaryStructureName, fromDate, DEFAULT_COMPANY, baseSalary, null);

    if ("success".equals(result.get("status"))) {
        logger.info("SSA créée avec succès pour {}: {}", employeeName, result.get("name"));
    } else {
        String errorMessage = (String) result.get("message");
        logger.error("Erreur de création SSA pour {}: {}", employeeName, errorMessage);
        throw new RuntimeException("Échec de la création de SSA: " + errorMessage);
    }
}

    /**
     * S'assure qu'une Salary Structure existe et retourne son nom.
     */
    public String ensureSalaryStructureExists() {
        try {
            HttpHeaders headers = createHttpHeaders();
            String url = ensureValidUrl(erpNextConfig.ERP_NEXT_API_URL + "Salary Structure/" + DEFAULT_SALARY_STRUCTURE);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Salary Structure '{}' trouvée", DEFAULT_SALARY_STRUCTURE);
                return DEFAULT_SALARY_STRUCTURE;
            }
        } catch (Exception e) {
            logger.warn("Salary Structure '{}' non trouvée: {}", DEFAULT_SALARY_STRUCTURE, e.getMessage());
        }

        return getFirstAvailableSalaryStructure();
    }

    /**
     * Récupère la première Salary Structure disponible.
     */
    public String getFirstAvailableSalaryStructure() {
        try {
            HttpHeaders headers = createHttpHeaders();
            String url = ensureValidUrl(erpNextConfig.ERP_NEXT_API_URL + "Salary Structure?fields=[\"name\"]&limit=1");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                List<Map<String, Object>> data = (List<Map<String, Object>>) responseBody.get("data");
                if (data != null && !data.isEmpty()) {
                    String structureName = (String) data.get(0).get("name");
                    logger.info("Utilisation de la Salary Structure existante: {}", structureName);
                    return structureName;
                }
            }

            throw new RuntimeException("Aucune Salary Structure trouvée dans le système ERPNext");

        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des Salary Structures: {}", e.getMessage(), e);
            throw new RuntimeException("Impossible de récupérer une Salary Structure: " + e.getMessage());
        }
    }

    /**
     * Vérifie si une Salary Structure Assignment existe pour l'employé et la période.
     */
    public boolean hasSalaryStructureAssignment(String employeeName, YearMonth period) {
        try {
            HttpHeaders headers = createHttpHeaders();
            String url = ensureValidUrl(ErpNextConfig.ERP_NEXT_API_SALARY_STRUCTURE_ASSIGNMENT_URL);
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("fields", "[\"name\", \"employee\", \"from_date\"]")
                    .queryParam("filters", String.format("[[\"employee\", \"=\", \"%s\"]]", employeeName));

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                List<Map<String, Object>> data = (List<Map<String, Object>>) responseBody.get("data");
                if (data != null && !data.isEmpty()) {
                    for (Map<String, Object> assignment : data) {
                        String fromDateStr = (String) assignment.get("from_date");
                        if (fromDateStr != null) {
                            LocalDate fromDate = LocalDate.parse(fromDateStr);
                            LocalDate periodStart = period.atDay(1);
                            if (fromDate.isBefore(periodStart) || fromDate.isEqual(periodStart)) {
                                return true;
                            }
                        }
                    }
                }
            }
            return false;

        } catch (Exception e) {
            logger.warn("Erreur lors de la vérification de Salary Structure Assignment pour {}: {}", employeeName, e.getMessage());
            return false;
        }
    }

    /**
     * Valide les paramètres d'entrée.
     */
    public void validateParameters(String employeeName, String startMonth, int startYear,
                                   String endMonth, int endYear) {
        if (employeeName == null || employeeName.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom de l'employé est requis.");
        }
        if (startMonth == null || endMonth == null || !isValidMonth(startMonth) || !isValidMonth(endMonth)) {
            throw new IllegalArgumentException("Les mois de début et de fin doivent être valides (01-12).");
        }
        if (startYear < 2000 || endYear < 2000 || startYear > endYear) {
            throw new IllegalArgumentException("Les années doivent être valides et l'année de début ne peut pas être supérieure à l'année de fin.");
        }
    }

    /**
     * Détermine le salaire à utiliser pour la génération.
     */
    public Double determineSalaryToUse(String employeeName, Double baseSalary, YearMonth start, YearMonth end) {
        if (baseSalary != null && baseSalary > 0) {
            return baseSalary;
        }

        List<SalarySlip> existingSlips = salarySlipService.getSalarySlipsByEmployeeAndMonthYear(employeeName, null, null);
        if (existingSlips.isEmpty()) {
            throw new IllegalArgumentException("Aucun salaire de base fourni et aucune fiche de paie existante pour " + employeeName);
        }

        return existingSlips.get(0).getGrossPay();
    }

    /**
     * Traite la génération d'une fiche de paie pour un mois donné.
     */
    public boolean processMonthlyPayslip(String employeeName, YearMonth yearMonth, Double salary, HttpHeaders headers)
            throws JsonProcessingException {
        String currentMonth = String.format("%02d", yearMonth.getMonthValue());
        String currentYear = String.valueOf(yearMonth.getYear());

        // Vérifier si une fiche de paie existe déjà
        List<SalarySlip> existingSlips = salarySlipService.getSalarySlipsByEmployeeAndMonthYear(
                employeeName, currentMonth, currentYear);
        if (!existingSlips.isEmpty()) {
            logger.info("Fiche de paie existante trouvée pour {} {}/{}, ignorée.", employeeName, currentMonth, currentYear);
            return false;
        }

        // Créer les dates de début et fin
        String startDate = yearMonth.getYear() + "-" + currentMonth + "-01";
        int lastDay = yearMonth.getMonth().length(yearMonth.isLeapYear());
        String endDate = yearMonth.getYear() + "-" + currentMonth + "-" + String.format("%02d", lastDay);

        // Créer et envoyer la fiche de paie
        String payload = createPayslipPayload(employeeName, startDate, endDate);
        HttpEntity<String> entity = new HttpEntity<>(payload, headers);
        String url = ensureValidUrl(ErpNextConfig.ERP_NEXT_API_SALARY_SLIP_URL);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Map<String, Object> responseBody = response.getBody();
            Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
            if (data != null && data.containsKey("name")) {
                logger.info("Fiche de paie générée pour {} {}/{} avec ID: {}", employeeName, currentMonth, currentYear, data.get("name"));
                return true;
            }
        }

        logger.error("Échec de la génération de la fiche de paie pour {} {}/{}", employeeName, currentMonth, currentYear);
        throw new RuntimeException("Réponse inattendue de l'API ERPNext");
    }

    /**
     * Crée les en-têtes HTTP pour les appels API.
     */
    public HttpHeaders createHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + ErpNextConfig.API_KEY + ":" + ErpNextConfig.API_SECRET);
        headers.set("Content-Type", "application/json");
        return headers;
    }

    /**
     * Crée le payload JSON pour une fiche de paie.
     */
    public String createPayslipPayload(String employeeName, String startDate, String endDate)
            throws JsonProcessingException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("employee", employeeName);
        payload.put("start_date", startDate);
        payload.put("end_date", endDate);
        payload.put("posting_date", LocalDate.now().toString());
        payload.put("company", DEFAULT_COMPANY);
        payload.put("docstatus", 1); // Soumettre directement

        return objectMapper.writeValueAsString(payload);
    }

    /**
     * Vérifie et corrige l'URL si nécessaire.
     */
    public String ensureValidUrl(String baseUrl) {
        if (!baseUrl.startsWith("http://") && !baseUrl.startsWith("https://")) {
            return "http://" + baseUrl;
        }
        return baseUrl;
    }

    /**
     * Valide si un mois est correct (01-12).
     */
    public boolean isValidMonth(String month) {
        try {
            int m = Integer.parseInt(month);
            return m >= 1 && m <= 12;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}