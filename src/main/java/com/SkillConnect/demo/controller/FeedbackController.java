package com.SkillConnect.demo.controller;

import com.SkillConnect.demo.entity.FeedBack;
import com.SkillConnect.demo.entity.Provider;
import com.SkillConnect.demo.entity.User;
import com.SkillConnect.demo.repository.FeedbackRepository;
import com.SkillConnect.demo.repository.ProviderRepository;
import com.SkillConnect.demo.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
public class FeedbackController {
    @Autowired
    private FeedbackRepository repository;

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private UserRepository userRepository;

    // Method to show the form to submit feedback
    @GetMapping("/create")
    public String showFeedbackForm(Model model, @RequestParam Long id) {
        model.addAttribute("feedback", new FeedBack());  // Create a new FeedBack object for the form
        model.addAttribute("providerId", id);  // Pass the provider ID to the model
        return "feedbackForm";  // Ensure this matches the template name in templates folder
    }


    @GetMapping("/create/{id}")
    public String showFeedbackForm(@PathVariable("id") Long providerId, Model model) {
        Optional<Provider> providerOptional = providerRepository.findById(providerId);

        if (providerOptional.isPresent()) {
            model.addAttribute("provider", providerOptional.get());  // Pass the provider to the form
            return "feedback-form";  // Thymeleaf page where the user can submit feedback
        } else {
            model.addAttribute("error", "Provider not found");
            return "redirect:/error/404";  // Redirect to an error page if the provider is not found
        }
    }


    @PostMapping("/savefeedback/{providerId}")
    public String saveFeedback(@PathVariable Long providerId,
                               Model model,
                               HttpSession session,
                               @ModelAttribute FeedBack feedback) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            model.addAttribute("error", "User not logged in");
            return "redirect:/login";
        }

        Optional<Provider> providerOptional = providerRepository.findById(providerId);
        Optional<User> userOptional = userRepository.findById(userId);

        if (providerOptional.isPresent() && userOptional.isPresent()) {
            Provider provider = providerOptional.get();
            User user = userOptional.get();
            feedback.setProvider(provider);
            feedback.setUser(user);

            repository.save(feedback);

            model.addAttribute("success", "Feedback submitted successfully");
            return "redirect:/booking/details";  // Redirect to the provider's profile or appropriate page
        } else {
            model.addAttribute("error", "Provider or User not found");
            return "redirect:/error/404";  // Redirect to an error page if not found
        }
    }

    @PostMapping("/save")
    public String saveFeedback(@ModelAttribute("feedback") FeedBack feedback) {
        repository.save(feedback);
        return "redirect:/feedback/list";  // Redirect to the feedback list after saving
    }

    @GetMapping("/provider/profile/{id}/{userId}")
    public String getProviderProfile(@PathVariable Long id, @PathVariable Long userId, Model model) {
        // Fetch provider details
        Optional<Provider> providerOptional = providerRepository.findById(id);
        Optional<User> userOptional = userRepository.findById(userId);

        // Check if the provider exists
        if (!providerOptional.isPresent()) {
            model.addAttribute("error", "Provider not found");
            return "error/404"; // Redirect to a 404 error page or an appropriate error view
        }

        // Check if the user exists
        if (!userOptional.isPresent()) {
            model.addAttribute("error", "User  not found");
            return "error/404"; // Redirect to a 404 error page or an appropriate error view
        }

        // Get the provider and user objects
        Provider provider = providerOptional.get();
        User user = userOptional.get();

        // Add provider and user to the model
        model.addAttribute("provider", provider);
        model.addAttribute("user", user);

        return "provider/profile"; // Return the view name
    }

    // Method to list all feedback
    @GetMapping("/list")
    public String listFeedback(Model model) {
        List<FeedBack> feedbackList = repository.findAll();
        model.addAttribute("feedbackList", feedbackList);
        return "/feedback/list";  // Thymeleaf template for the feedback list
    }

    // Method to show feedback details for a specific provider
    @GetMapping("/details/{providerId}")
    public String showFeedbackDetails(@PathVariable("providerId") Long providerId, Model model) {
        List<FeedBack> feedbackList = repository.getFeedbacksByProviderId(providerId);
        model.addAttribute("feedbackList", feedbackList);
        return "providedFeedbacks";
    }

    // Method to delete feedback
    @GetMapping("/delete/{id}")
    public String deleteFeedback(@PathVariable("id") Long id) {
        repository.deleteById(id);
        return "redirect:/feedback/list";  // Redirect to the feedback list after deletion
    }

    // Method to edit an existing feedback
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        FeedBack feedback = repository.findById(id).orElse(null);
        model.addAttribute("feedback", feedback);
        return "feedback/edit";  // Thymeleaf template for editing the feedback
    }

    // Method to update the feedback
    @PostMapping("/update/{id}")
    public String updateFeedback(@PathVariable("id") Long id, @ModelAttribute("feedback") FeedBack feedback) {
        feedback.setId(id);  // Ensure the ID is preserved during update
        repository.save(feedback);
        return "redirect:/feedback/list";  // Redirect to the feedback list after update
    }

    @GetMapping("/providerFeedbacks")
    public String getMyFeedbacks(HttpSession session, Model model) {
        Long providerId = (Long) session.getAttribute("sid");
        List<FeedBack> feedBacks = repository.getFeedbacksByProviderId(providerId);
        model.addAttribute("feedbacks", feedBacks);
        System.out.println(feedBacks);
        return "providerFeedbacks";
    }

}
