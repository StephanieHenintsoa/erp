package com.example.erp.controller.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.erp.entity.auth.LoginRequest;
import com.example.erp.service.auth.LoginService;

import jakarta.servlet.http.HttpSession;

@Controller
public class LoginController {

    @Autowired
    private LoginService loginService;


    @GetMapping("/")
    public String showLoginForm(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute LoginRequest loginRequest, Model model, HttpSession session) {
        try {
            String response = loginService.login(loginRequest.getUsername(), loginRequest.getPassword(), session);
            return "redirect:/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Login failed: username or password was wrong");
            return "auth/login";
        }
    }

    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session) {
        if (!LoginService.isAuthenticated(session)) {
            return "redirect:/"; 
        }
        return "home/home";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Invalider la session
        return "redirect:/";
    }

}