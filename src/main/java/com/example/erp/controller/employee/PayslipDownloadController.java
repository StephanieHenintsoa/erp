package com.example.erp.controller.employee;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.erp.entity.Employee;
import com.example.erp.entity.salary.SalarySlip;
import com.example.erp.service.employee.EmployeeService;
import com.example.erp.service.salary.SalarySlipService;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

@Controller
@RequestMapping("/api")
public class PayslipDownloadController {

    private static final Logger logger = LoggerFactory.getLogger(PayslipDownloadController.class);
    
    // Updated formatter for better number formatting
    private static final DecimalFormat DECIMAL_FORMAT;
    
    static {
        // Create custom DecimalFormatSymbols for French locale (space as grouping separator)
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.FRENCH);
        symbols.setGroupingSeparator(' '); // Use space instead of comma for thousands
        symbols.setDecimalSeparator(',');   // Use comma for decimal separator (French style)
        
        // Create formatter with custom symbols
        DECIMAL_FORMAT = new DecimalFormat("#,##0.00", symbols);
        
        // Alternative: For English-style formatting with comma as thousands separator
        // DECIMAL_FORMAT = new DecimalFormat("#,##0.00", new DecimalFormatSymbols(Locale.US));
    }

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private SalarySlipService salarySlipService;

    @PostMapping("/payslips/download")
    public ResponseEntity<InputStreamResource> downloadPayslip(
            @RequestParam String employeeName,
            @RequestParam String slipName) {
        try {
            logger.info("Téléchargement PDF demandé - Employé: '{}', Fiche: '{}'", employeeName, slipName);
            
            // Récupération de l'employé
            Employee employee = employeeService.getEmployeeByName(employeeName);
            if (employee == null) {
                logger.warn("Employé non trouvé: '{}'", employeeName);
                return ResponseEntity.notFound().build();
            }
            logger.debug("Employé trouvé: ID={}, Nom complet={}", employee.getName(), employee.getEmployeeName());
            
            // Récupération de la fiche de paie
            SalarySlip salarySlip = salarySlipService.getSalarySlipByName(slipName);
            if (salarySlip == null) {
                logger.warn("Fiche de paie non trouvée: '{}'", slipName);
                return ResponseEntity.notFound().build();
            }
            logger.debug("Fiche de paie trouvée: ID={}, Période={} à {}", 
                        salarySlip.getName(), salarySlip.getStartDate(), salarySlip.getEndDate());
            
            // Génération du PDF
            ByteArrayInputStream pdfStream = generatePayslipPdf(employee, salarySlip);
            String fileName = createFileName(employee.getEmployeeName(), salarySlip.getName());
            
            logger.info("PDF généré avec succès: fichier={}, taille={} bytes", 
                       fileName, pdfStream.available());
            
            // Configuration des headers pour le téléchargement
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
            headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
            headers.add(HttpHeaders.PRAGMA, "no-cache");
            headers.add(HttpHeaders.EXPIRES, "0");
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(new InputStreamResource(pdfStream));
            
        } catch (IOException e) {
            logger.error("Erreur lors de la génération du PDF pour employé='{}', fiche='{}'", 
                        employeeName, slipName, e);
            return ResponseEntity.internalServerError().build();
        } catch (Exception e) {
            logger.error("Erreur inattendue lors du téléchargement pour employé='{}', fiche='{}': {}", 
                        employeeName, slipName, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private ByteArrayInputStream generatePayslipPdf(Employee employee, SalarySlip salarySlip) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try (PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {
            
            // Titre principal
            document.add(new Paragraph("BULLETIN DE PAIE")
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));
            
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            // Section Informations Employé
            document.add(new Paragraph("INFORMATIONS EMPLOYÉ")
                    .setFontSize(14)
                    .setBold()
                    .setUnderline());
            
            document.add(new Paragraph("Nom complet: " + safeString(employee.getEmployeeName())));
            document.add(new Paragraph("Prénom: " + safeString(employee.getFirstName())));
            document.add(new Paragraph("Département: " + safeString(employee.getDepartment())));
            document.add(new Paragraph("Poste: " + safeString(employee.getDesignation())));
            document.add(new Paragraph("Entreprise: " + safeString(employee.getCompany())));
            document.add(new Paragraph("Matricule: " + safeString(employee.getName())));

            document.add(new Paragraph(" "));

            // Section Période de Paie
            document.add(new Paragraph("PÉRIODE DE PAIE")
                    .setFontSize(14)
                    .setBold()
                    .setUnderline());
            
            document.add(new Paragraph("Période: du " + safeString(salarySlip.getStartDate()) + 
                                     " au " + safeString(salarySlip.getEndDate())));
            document.add(new Paragraph("Date de publication: " + safeString(salarySlip.getPostingDate())));
            document.add(new Paragraph("Statut: " + safeString(salarySlip.getStatus())));
            document.add(new Paragraph("Référence: " + safeString(salarySlip.getName())));

            document.add(new Paragraph(" "));

            // Section Détails Financiers
            document.add(new Paragraph("DÉTAILS FINANCIERS")
                    .setFontSize(14)
                    .setBold()
                    .setUnderline());
            
            // Tableau des Gains
            document.add(new Paragraph("GAINS:")
                    .setFontSize(12)
                    .setBold());
            Table earningsTable = new Table(UnitValue.createPercentArray(new float[]{70, 30}));
            earningsTable.setWidth(UnitValue.createPercentValue(100));
            earningsTable.addHeaderCell(new Cell().add(new Paragraph("Composant").setBold()));
            earningsTable.addHeaderCell(new Cell().add(new Paragraph("Montant (€)").setBold().setTextAlignment(TextAlignment.RIGHT)));
            
            if (salarySlip.getEarnings() != null && !salarySlip.getEarnings().isEmpty()) {
                for (var earning : salarySlip.getEarnings()) {
                    String component = safeString(earning.getSalaryComponent()) + 
                                       (earning.getAbbr() != null ? " (" + safeString(earning.getAbbr()) + ")" : "");
                    earningsTable.addCell(new Cell().add(new Paragraph(component)));
                    earningsTable.addCell(new Cell().add(new Paragraph(formatAmount(earning.getAmount()) + " €")
                            .setTextAlignment(TextAlignment.RIGHT)));
                }
            } else {
                earningsTable.addCell(new Cell(1, 2).add(new Paragraph("Aucun gain").setTextAlignment(TextAlignment.CENTER)));
            }
            document.add(earningsTable);
            document.add(new Paragraph("Total Gains: " + formatAmount(salarySlip.getTotalEarnings()) + " €")
                    .setBold());
            document.add(new Paragraph("Salaire Brut: " + formatAmount(salarySlip.getGrossPay()) + " €")
                    .setBold());
            
            document.add(new Paragraph(" "));
            
            // Tableau des Déductions
            document.add(new Paragraph("DÉDUCTIONS:")
                    .setFontSize(12)
                    .setBold());
            Table deductionsTable = new Table(UnitValue.createPercentArray(new float[]{70, 30}));
            deductionsTable.setWidth(UnitValue.createPercentValue(100));
            deductionsTable.addHeaderCell(new Cell().add(new Paragraph("Composant").setBold()));
            deductionsTable.addHeaderCell(new Cell().add(new Paragraph("Montant (€)").setBold().setTextAlignment(TextAlignment.RIGHT)));
            
            if (salarySlip.getDeductions() != null && !salarySlip.getDeductions().isEmpty()) {
                for (var deduction : salarySlip.getDeductions()) {
                    String component = safeString(deduction.getSalaryComponent()) + 
                                       (deduction.getAbbr() != null ? " (" + safeString(deduction.getAbbr()) + ")" : "");
                    deductionsTable.addCell(new Cell().add(new Paragraph(component)));
                    deductionsTable.addCell(new Cell().add(new Paragraph(formatAmount(deduction.getAmount()) + " €")
                            .setTextAlignment(TextAlignment.RIGHT)));
                }
            } else {
                deductionsTable.addCell(new Cell(1, 2).add(new Paragraph("Aucune déduction").setTextAlignment(TextAlignment.CENTER)));
            }
            document.add(deductionsTable);
            document.add(new Paragraph("Total Déductions: " + formatAmount(salarySlip.getTotalDeduction()) + " €")
                    .setBold());
            
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));
            
            // Section Net à Payer
            document.add(new Paragraph("═══════════════════════════════════════")
                    .setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("NET À PAYER: " + formatAmount(salarySlip.getNetPay()) + " €")
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("═══════════════════════════════════════")
                    .setTextAlignment(TextAlignment.CENTER));
            
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));
            
            // Pied de page
            document.add(new Paragraph("Document généré automatiquement le " + 
                        java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")))
                    .setFontSize(8)
                    .setTextAlignment(TextAlignment.CENTER));
        }
        
        return new ByteArrayInputStream(baos.toByteArray());
    }

    private String createFileName(String employeeName, String slipName) {
        String cleanEmployeeName = safeString(employeeName)
                .replaceAll("[^a-zA-Z0-9\\-_\\s]", "")
                .replaceAll("\\s+", "_")
                .toLowerCase();
                
        String cleanSlipName = safeString(slipName)
                .replaceAll("[^a-zA-Z0-9\\-_\\s]", "")
                .replaceAll("\\s+", "_")
                .toLowerCase();
        
        return "bulletin_paie_" + cleanEmployeeName + "_" + cleanSlipName + ".pdf";
    }

    /**
     * Formats monetary amounts with proper thousands separator and decimal places
     * Examples:
     * - 100000.0 -> "100 000,00" (French style)
     * - 1234.56 -> "1 234,56"
     * - 0 -> "0,00"
     */
    private String formatAmount(Double amount) {
        if (amount == null) {
            return "0,00";
        }
        return DECIMAL_FORMAT.format(amount);
    }
    
    private String safeString(Object value) {
        return value != null ? value.toString().trim() : "";
    }
}