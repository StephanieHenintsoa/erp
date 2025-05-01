package com.example.erp.controller.auth;

import com.example.erp.entity.auth.LoginRequest;
import com.example.erp.service.auth.LoginService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

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
    public String login(@ModelAttribute LoginRequest loginRequest, Model model) {
        try {
            String response = loginService.login(loginRequest.getUsername(), loginRequest.getPassword());
            // Rediriger vers une page de succès ou tableau de bord
            return "redirect:/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Login failed: username or password was wrong" );
            return "auth/login";
        }
    }

    @GetMapping("/dashboard")
    public String showDashboard() {
        return "home/home";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/"; 
    }
}

