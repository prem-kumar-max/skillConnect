package com.SkillConnect.demo.controller;

import com.SkillConnect.demo.entity.LoginRequest;
import com.SkillConnect.demo.entity.Provider;
import com.SkillConnect.demo.entity.User;
import com.SkillConnect.demo.repository.ProviderRepository;
import com.SkillConnect.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class AdminController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProviderRepository providerRepository;

    @GetMapping("/admin/login")
    public String loginPage() {
        return "adminLogin";
    }

    // Custom login handler (POST)
    @PostMapping("/admin/login/json")
    @ResponseBody
    public String adminLoginJson(@RequestBody LoginRequest loginRequest) {
        String adminEmail = "admin@gmail.com";
        String adminPassword = "admin";

        if (adminEmail.equals(loginRequest.getEmail()) && adminPassword.equals(loginRequest.getPassword())) {
            return "Login successful";
        } else {
            return "Invalid email or password";
        }
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {
        List<User> users = userRepository.findAll();
        List<Provider> providers = providerRepository.findAll();

        model.addAttribute("users", users);
        model.addAttribute("providers", providers);
        return "adminDashboard";  // Thymeleaf will look for adminDashboard.html in the templates directory
    }

    @GetMapping("/admin/delete/provider/{id}")
    public String deleteProvider(@PathVariable Long id) {
        providerRepository.deleteById(id);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/admin/deleteUser/{id}")
    public String deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/admin/logout")
    public String logout() {
        return "redirect:/admin/login";
    }
}