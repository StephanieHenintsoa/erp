package com.example.erp.controller.client;


import com.example.erp.entity.auth.client.Client;
import com.example.erp.service.client.ClientService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/clients")
public class ClientController {

    @Autowired
    private ClientService clientService;

    @Value("${erpnext.api.key}")
    private String API_KEY;

    @Value("${erpnext.api.secret}")
    private String API_SECRET;

    

    @GetMapping
    public String showClients(Model model) {
        try {
            model.addAttribute("clients", clientService.getClients(API_KEY, API_SECRET));
            model.addAttribute("client", new Client());
            return "client/client";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load clients: " + e.getMessage());
            return "client/client";
        }
    }

    @PostMapping
    public String createClient(@ModelAttribute Client client, Model model) {
        try {
            clientService.createClient(client, API_KEY, API_SECRET);
            return "redirect:/clients";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to create client: " + e.getMessage());
            model.addAttribute("clients", clientService.getClients(API_KEY, API_SECRET));
            return "client/client";
        }
    }

    @GetMapping("/edit/{name}")
    public String showEditForm(@PathVariable String name, Model model) {
        try {
            List<Client> clients = clientService.getClients(API_KEY, API_SECRET);
            Client client = clients.stream()
                    .filter(c -> c.getName().equals(name))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Client not found"));
            model.addAttribute("client", client);
            model.addAttribute("clients", clients);
            return "client/client";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load client: " + e.getMessage());
            return "client/client";
        }
    }

    @PostMapping("/update/{name}")
    public String updateClient(@PathVariable String name, @ModelAttribute Client client, Model model) {
        try {
            clientService.updateClient(name, client, API_KEY, API_SECRET);
            return "redirect:/clients";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to update client: " + e.getMessage());
            model.addAttribute("clients", clientService.getClients(API_KEY, API_SECRET));
            return "client/client";
        }
    }

    @PostMapping("/delete/{name}")
    public String deleteClient(@PathVariable String name, Model model) {
        try {
            clientService.deleteClient(name, API_KEY, API_SECRET);
            return "redirect:/clients";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to delete client: " + e.getMessage());
            model.addAttribute("clients", clientService.getClients(API_KEY, API_SECRET));
            return "client/client";
        }
    }
   
}