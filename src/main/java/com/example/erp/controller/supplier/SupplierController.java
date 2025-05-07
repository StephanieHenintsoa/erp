package com.example.erp.controller.supplier;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.erp.entity.purchase.PurchaseOrder;
import com.example.erp.entity.request.RequestForQuotation;
import com.example.erp.entity.request.RequestForQuotationItem;
import com.example.erp.entity.supplier.Supplier;
import com.example.erp.entity.supplier.SupplierQuotation;
import com.example.erp.service.auth.LoginService;
import com.example.erp.service.purchase.PurchaseOrderService;
import com.example.erp.service.supplier.SupplierQuotationService;
import com.example.erp.service.supplier.SupplierService;
import com.example.erp.service.request.RequestForQuotationService;

import jakarta.servlet.http.HttpSession;

@Controller
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private SupplierQuotationService supplierQuotationService;

    @Autowired
    private PurchaseOrderService purchaseOrderService;
    
    @Autowired
    private RequestForQuotationService requestForQuotationService;

    @GetMapping("/suppliers")
    public String getSuppliers(Model model, HttpSession session) {
        if (!LoginService.isAuthenticated(session)) {
            return "redirect:/";
        }

        List<Supplier> suppliers = supplierService.getSuppliers();
        model.addAttribute("suppliers", suppliers);
        return "supplier/supplier";
    }

    @GetMapping("/supplier-quotations/{name}")
    public String getSupplierQuotations(
            @PathVariable("name") String supplierName, 
            Model model, 
            HttpSession session,
            @RequestParam(value = "message", required = false) String message,
            @RequestParam(value = "messageType", required = false) String messageType) {
        if (!LoginService.isAuthenticated(session)) {
            return "redirect:/";
        }

        List<SupplierQuotation> quotations = supplierQuotationService.getSupplierQuotations(supplierName);
        model.addAttribute("quotations", quotations);
        model.addAttribute("supplierName", supplierName);
        
        if (message != null && !message.isEmpty()) {
            model.addAttribute("message", message);
            model.addAttribute("messageType", messageType);
        }
        
        return "supplier/supplier-quotations";
    }

    @GetMapping("/supplier-quotation-items/{quotationName}")
    public String getSupplierQuotationItems(
            @PathVariable("quotationName") String quotationName, 
            Model model, 
            HttpSession session,
            @RequestParam(value = "message", required = false) String message,
            @RequestParam(value = "messageType", required = false) String messageType,
            RedirectAttributes redirectAttributes) 
    {
        if (!LoginService.isAuthenticated(session)) {
            return "redirect:/";
        }

        try {
            SupplierQuotation quotation = supplierQuotationService.getSupplierQuotationByName(quotationName);
            model.addAttribute("quotation", quotation);
            model.addAttribute("quotationName", quotationName);
            model.addAttribute("supplierName", quotation.getSupplier());
            model.addAttribute("items", quotation.getItems());
            
            // warning if quotation submitted
            if ("Submitted".equalsIgnoreCase(quotation.getStatus())) {
                if (message == null || message.isEmpty()) {
                    model.addAttribute("message", "This quotation has been submitted and cannot be modified. You need to cancel it first in ERPNext.");
                    model.addAttribute("messageType", "warning");
                }
            }
            
            if (message != null && !message.isEmpty()) {
                model.addAttribute("message", message);
                model.addAttribute("messageType", messageType);
            }
            
            return "supplier/supplier-quotation-items";
            
        } catch (Exception e) {
            redirectAttributes.addAttribute("message", "Error retrieving quotation: " + e.getMessage());
            redirectAttributes.addAttribute("messageType", "danger");
            return "redirect:/supplier-quotations/" + supplierQuotationService.getSupplierNameByQuotation(quotationName);
        }
    }

    @GetMapping("/supplier-purchase-orders/{name}")
    public String getSupplierPurchaseOrders(
            @PathVariable("name") String supplierName,
            @RequestParam(value = "status", defaultValue = "all") String status,
            Model model,
            HttpSession session) 
    {
        if (!LoginService.isAuthenticated(session)) {
            return "redirect:/";
        }

        List<PurchaseOrder> purchaseOrders = purchaseOrderService.getPurchaseOrders(supplierName, status);
        model.addAttribute("purchaseOrders", purchaseOrders);
        model.addAttribute("supplierName", supplierName);
        model.addAttribute("status", status);
        return "supplier/supplier-purchase-orders";
    }
    
    @GetMapping("/supplier-rfqs/{supplierName}")
    public String getSupplierRfqs(
            @PathVariable String supplierName, 
            Model model, 
            HttpSession session,
            @RequestParam(value = "message", required = false) String message,
            @RequestParam(value = "messageType", required = false) String messageType,
            RedirectAttributes redirectAttributes) 
    {
        if (!LoginService.isAuthenticated(session)) {
            return "redirect:/";
        }
        
        try {
            // set supplier name into session
            session.setAttribute("supplierName", supplierName);
            
            List<RequestForQuotation> rfqs = requestForQuotationService.getRequestForQuotationsForSupplier(supplierName);
            model.addAttribute("rfqs", rfqs);
            model.addAttribute("supplierName", supplierName);
            
            if (message != null && !message.isEmpty()) {
                model.addAttribute("message", message);
                model.addAttribute("messageType", messageType);
            }
            
            return "supplier/supplier-rfqs";
        } catch (Exception e) {
            redirectAttributes.addAttribute("message", "Error retrieving RFQs: " + e.getMessage());
            redirectAttributes.addAttribute("messageType", "danger");
            return "redirect:/suppliers";
        }
    }
    
    @GetMapping("/supplier-rfq-details/{rfqName}")
    public String getSupplierRfqDetails(
            @PathVariable String rfqName, 
            Model model, 
            HttpSession session,
            RedirectAttributes redirectAttributes) 
    {
        if (!LoginService.isAuthenticated(session)) {
            return "redirect:/";
        }
        
        try {
            RequestForQuotation rfq = requestForQuotationService.getRequestForQuotationByName(rfqName);
            session.setAttribute("rfq", rfq); // set into session

            if (rfq == null) {
                redirectAttributes.addAttribute("message", "RFQ not found: " + rfqName);
                redirectAttributes.addAttribute("messageType", "danger");
                return "redirect:/suppliers";
            }
            
            model.addAttribute("rfq", rfq);
            return "supplier/supplier_rfqs_details";
        } catch (Exception e) {
            redirectAttributes.addAttribute("message", "Error retrieving RFQ details: " + e.getMessage());
            redirectAttributes.addAttribute("messageType", "danger");
            return "redirect:/suppliers";
        }
    }

    @PostMapping("/supplier-quotation-creation")
    public String createSupplierQuotation(@RequestParam Map<String, String> formData, 
                                         HttpSession session, 
                                         RedirectAttributes redirectAttributes) {
        String supplier = (String) session.getAttribute("supplierName");
        RequestForQuotation rfq = (RequestForQuotation) session.getAttribute("rfq");

        System.out.println("supp" + supplier);
        
        if (!LoginService.isAuthenticated(session)) {
            return "redirect:/";
        }
        
        try {
            String quotationId = supplierQuotationService.createSupplierQuotation(supplier, rfq, formData);
            
            redirectAttributes.addFlashAttribute("success", 
                "Supplier Quotation created successfully. ID: " + quotationId);
                
        } catch (Exception e) {
            e.printStackTrace();
            
            redirectAttributes.addFlashAttribute("error", 
                "Error creating Supplier Quotation: " + e.getMessage());
        }
        
        return "redirect:/supplier-quotations/" + supplier;
    }
}