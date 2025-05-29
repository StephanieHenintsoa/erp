package com.example.erp.controller.rfq;

import com.example.erp.service.auth.LoginService;
import com.example.erp.service.rfq.RfqService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@Controller
public class RfqController {

    @Autowired
    private RfqService rfqService;

    @GetMapping("/entities")
    public String getAllEntities(Model model, HttpSession session) {
        if (!LoginService.isAuthenticated(session)) {
            return "redirect:/";
        }

        // Fetch all entities
        Map<String, List<?>> entities = rfqService.getAllEntities();
        
        model.addAttribute("items", entities.get("items"));
        model.addAttribute("warehouses", entities.get("warehouses"));
        model.addAttribute("suppliers", entities.get("suppliers"));
        
        return "rfq/rfq-form";
    }

    @GetMapping("/rfq-form")
    public String showRfqForm(Model model, HttpSession session) {
        if (!LoginService.isAuthenticated(session)) {
            return "redirect:/";
        }

        // Fetch all entities for the RFQ form
        Map<String, List<?>> entities = rfqService.getAllEntities();
        
        model.addAttribute("items", entities.get("items"));
        model.addAttribute("warehouses", entities.get("warehouses"));
        model.addAttribute("suppliers", entities.get("suppliers"));
        
        return "rfq/rfq-form";
    }

    @GetMapping("/update-uom")
    public String showUpdateUomForm(HttpSession session) {
        if (!LoginService.isAuthenticated(session)) {
            return "redirect:/";
        }
        return "rfq/update-uom";
    }

    @PostMapping("/update-uom")
    public String updateItemUom(
            @RequestParam String itemCode,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        if (!LoginService.isAuthenticated(session)) {
            return "redirect:/";
        }
        try {
            String result = rfqService.updateItemUom(itemCode);
            redirectAttributes.addFlashAttribute("message", result);
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Error updating UOM: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }
        return "redirect:/update-uom";
    }

    @PostMapping("/submit")
    public String submitRfq(
            @RequestParam String suppliers,
            @RequestParam String transaction_date,
            @RequestParam String main_schedule_date,
            @RequestParam String status,
            @RequestParam(required = false) String message_for_supplier,
            @RequestParam String items,
            @RequestParam(required = false) String quantity,
            @RequestParam(required = false) String item_schedule_date,
            @RequestParam String warehouses,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        if (!LoginService.isAuthenticated(session)) {
            return "redirect:/";
        }

        try {
            // Conversion de quantity (String -> Double)
            Double quantityDouble = (quantity != null && !quantity.trim().isEmpty()) ? Double.parseDouble(quantity) : null;

            // Appel au service saveRfq
            Map<String, Object> response = rfqService.saveRfq(
                    suppliers,
                    transaction_date,
                    main_schedule_date,
                    status,
                    message_for_supplier,
                    items,
                    quantityDouble,
                    item_schedule_date,
                    warehouses
            );

            // Gestion de la réponse
            if (Boolean.TRUE.equals(response.get("success"))) {
                redirectAttributes.addFlashAttribute("message", response.get("message"));
                redirectAttributes.addFlashAttribute("messageType", "success");
            } else {
                redirectAttributes.addFlashAttribute("message", response.get("message"));
                redirectAttributes.addFlashAttribute("messageType", "danger");
            }
        } catch (NumberFormatException e) {
            redirectAttributes.addFlashAttribute("message", "Invalid quantity format");
            redirectAttributes.addFlashAttribute("messageType", "danger");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Error submitting RFQ: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }

        return "redirect:/rfq-form";
    }
}