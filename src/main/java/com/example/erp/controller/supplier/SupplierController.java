package com.example.erp.controller.supplier;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.erp.entity.purchase.PurchaseOrder;
import com.example.erp.entity.supplier.Supplier;
import com.example.erp.entity.supplier.SupplierQuotation;
import com.example.erp.service.auth.LoginService;
import com.example.erp.service.purchase.PurchaseOrderService;
import com.example.erp.service.supplier.SupplierQuotationService;
import com.example.erp.service.supplier.SupplierService;

import jakarta.servlet.http.HttpSession;

@Controller
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private SupplierQuotationService supplierQuotationService;

    @Autowired
    private PurchaseOrderService purchaseOrderService;

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
    public String getSupplierQuotations(@PathVariable("name") String supplierName, Model model, HttpSession session) {
        if (!LoginService.isAuthenticated(session)) {
            return "redirect:/";
        }

        List<SupplierQuotation> quotations = supplierQuotationService.getSupplierQuotations(supplierName);
        model.addAttribute("quotations", quotations);
        model.addAttribute("supplierName", supplierName);
        return "supplier/supplier-quotations";
    }

    @GetMapping("/supplier-quotation-items/{quotationName}")
    public String getSupplierQuotationItems(@PathVariable("quotationName") String quotationName, Model model, HttpSession session) {
        if (!LoginService.isAuthenticated(session)) {
            return "redirect:/";
        }

        SupplierQuotation quotation = supplierQuotationService.getSupplierQuotationByName(quotationName);
        model.addAttribute("quotation", quotation);
        model.addAttribute("quotationName", quotationName);
        model.addAttribute("supplierName", quotation.getSupplier());
        model.addAttribute("items", quotation.getItems());
        return "supplier/supplier-quotation-items";
    }

    @GetMapping("/supplier-purchase-orders/{name}")
    public String getSupplierPurchaseOrders(
            @PathVariable("name") String supplierName,
            @RequestParam(value = "status", defaultValue = "all") String status,
            Model model,
            HttpSession session) {
        if (!LoginService.isAuthenticated(session)) {
            return "redirect:/";
        }

        List<PurchaseOrder> purchaseOrders = purchaseOrderService.getPurchaseOrders(supplierName, status);
        model.addAttribute("purchaseOrders", purchaseOrders);
        model.addAttribute("supplierName", supplierName);
        model.addAttribute("status", status);
        return "supplier/supplier-purchase-orders";
    }
}