package com.example.erp.service.salary;

import com.example.erp.config.ErpNextConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Service pour gérer les Salary Structure Assignments dans ERPNext.
 */
@Service
public class SalaryStructureAssignmentService {

    private static final Logger logger = LoggerFactory.getLogger(SalaryStructureAssignmentService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ErpNextConfig erpNextConfig;

    @Autowired
    public SalaryStructureAssignmentService(RestTemplate restTemplate, ObjectMapper objectMapper,
                                            ErpNextConfig erpNextConfig) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.erpNextConfig = erpNextConfig;
    }

    /**
     * Crée une Salary Structure Assignment pour un employé dans ERPNext.
     * @param employee Nom de l'employé.
     * @param salaryStructure Nom de la structure salariale.
     * @param fromDate Date de début (format: YYYY-MM-DD).
     * @param company Nom de la société.
     * @param base Salaire de base.
     * @param variable Salaire variable (optionnel).
     * @return Map contenant le statut et les détails de la création.
     */
    public Map<String, Object> createSalaryStructureAssignment(String employee, String salaryStructure,
                                                              String fromDate, String company,
                                                              Double base, Double variable) {
        // Validation des paramètres
        Map<String, Object> validationResult = validateParameters(employee, salaryStructure, fromDate, company, base, variable);
        if (validationResult != null) {
            return validationResult;
        }

        try {
            // Construire le payload
            String payload = createAssignmentPayload(employee, salaryStructure, fromDate, company, base, variable);

            // Préparer les headers et l'URL
            HttpHeaders headers = createHttpHeaders();
            String url = ensureValidUrl(erpNextConfig.ERP_NEXT_API_SALARY_STRUCTURE_ASSIGNMENT_URL);

            // Envoyer la requête POST
            HttpEntity<String> requestEntity = new HttpEntity<>(payload, headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);

            // Traiter la réponse
            return processResponse(response, employee);

        } catch (Exception e) {
            logger.error("Erreur lors de la création de Salary Structure Assignment pour {}: {}", employee, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Échec de la création de Salary Structure Assignment: " + e.getMessage());
            return errorResponse;
        }
    }

    /**
     * Valide les paramètres d'entrée.
     */
    private Map<String, Object> validateParameters(String employee, String salaryStructure, String fromDate,
                                                  String company, Double base, Double variable) {
        if (employee == null || employee.trim().isEmpty()) {
            return createErrorResponse("Le nom de l'employé est requis.");
        }
        if (salaryStructure == null || salaryStructure.trim().isEmpty()) {
            return createErrorResponse("La structure salariale est requise.");
        }
        if (fromDate == null || !isValidDateFormat(fromDate)) {
            return createErrorResponse("La date de début doit être au format YYYY-MM-DD.");
        }
        if (company == null || company.trim().isEmpty()) {
            return createErrorResponse("Le nom de la société est requis.");
        }
        if (base == null || base <= 0) {
            return createErrorResponse("Le salaire de base doit être supérieur à zéro.");
        }
        if (variable != null && variable < 0) {
            return createErrorResponse("Le salaire variable ne peut pas être négatif.");
        }
        return null;
    }

    /**
     * Crée un payload JSON pour la Salary Structure Assignment.
     */
    private String createAssignmentPayload(String employee, String salaryStructure, String fromDate,
                                          String company, Double base, Double variable) throws JsonProcessingException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("employee", employee);
        payload.put("salary_structure", salaryStructure);
        payload.put("from_date", fromDate);
        payload.put("company", company);
        payload.put("base", base);
        if (variable != null && variable > 0) {
            payload.put("variable", variable);
        }
        payload.put("docstatus", 1); // Soumettre directement

        return objectMapper.writeValueAsString(payload);
    }

    /**
     * Crée les en-têtes HTTP pour les appels API.
     */
    private HttpHeaders createHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + erpNextConfig.API_KEY + ":" + erpNextConfig.API_SECRET);
        headers.set("Content-Type", "application/json");
        return headers;
    }

    /**
     * Vérifie et corrige l'URL si nécessaire.
     */
    private String ensureValidUrl(String baseUrl) {
        if (!baseUrl.startsWith("http://") && !baseUrl.startsWith("https://")) {
            return "http://" + baseUrl;
        }
        return baseUrl;
    }

    /**
     * Traite la réponse de l'API ERPNext.
     */
    private Map<String, Object> processResponse(ResponseEntity<Map> response, String employee) {
        Map<String, Object> result = new HashMap<>();
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Map<String, Object> responseBody = response.getBody();
            Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
            if (data != null && data.containsKey("name")) {
                logger.info("Salary Structure Assignment créée pour {} avec ID: {}", employee, data.get("name"));
                result.put("status", "success");
                result.put("name", data.get("name"));
                return result;
            }
        }
        logger.error("Réponse inattendue de l'API ERPNext pour {}: {}", employee, response.getBody());
        result.put("status", "error");
        result.put("message", "Réponse inattendue ou vide de l'API ERPNext");
        return result;
    }

    /**
     * Crée une réponse d'erreur standard.
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("message", message);
        return errorResponse;
    }

    /**
     * Vérifie si la date est au format YYYY-MM-DD.
     */
    private boolean isValidDateFormat(String date) {
        try {
            LocalDate.parse(date);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}