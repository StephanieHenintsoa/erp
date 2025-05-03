package com.example.erp.controller.invoice;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.erp.entity.invoice.PurchaseInvoice;
import com.example.erp.service.auth.LoginService;
import com.example.erp.service.invoice.PurchaseInvoiceService;

import jakarta.servlet.http.HttpSession;

@Controller
public class PurchaseInvoiceController {

    @Autowired
    private PurchaseInvoiceService purchaseInvoiceService;

    @GetMapping("/purchase-invoices")
    public String getPurchaseInvoices(Model model, HttpSession session) {
        if (!LoginService.isAuthenticated(session)) {
            return "redirect:/";
        }

        List<PurchaseInvoice> purchaseInvoices = purchaseInvoiceService.getPurchaseInvoices();
        model.addAttribute("purchaseInvoices", purchaseInvoices);
        return "invoice/purchase-invoices";
    }
}
