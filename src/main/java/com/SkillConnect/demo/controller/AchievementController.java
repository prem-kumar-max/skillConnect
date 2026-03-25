package com.SkillConnect.demo.controller;

import com.SkillConnect.demo.entity.Achievement;
import com.SkillConnect.demo.entity.Provider;
import com.SkillConnect.demo.repository.IAchievementRepository;
import com.SkillConnect.demo.repository.ProviderRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/achievements")
public class AchievementController {

    @Autowired
    private IAchievementRepository repository;

    @Autowired
    private ProviderRepository providerRepository;

    // Display all achievements
    @GetMapping
    public String listAchievements(Model model) {
        model.addAttribute("achievements", repository.findAll());
        return "achievements";
    }

    @GetMapping("/provider/id")
    public String listOfAchievementsByProviderId(HttpSession session,Model model)
    {
        Long providerId = (Long)session.getAttribute("sid");
        List<Achievement> achievements = repository.findByProviderId(providerId);
        model.addAttribute("achievements", achievements);
        return "provider-achievements";
    }

    // Display the form to add a new achievement
    @GetMapping("/add")
    public String showAddAchievementForm() {
        return "add-achievements";
    }


    // Handle the form submission for adding a new achievement
    @PostMapping("/add")
    public String addAchievement(HttpSession session,
                                 Model model,
                                 @RequestParam("achievementName") String name,
                                 @RequestParam("description") String description,
                                 @RequestParam("media") List<MultipartFile> mediaFiles) {
        try {
            // Retrieve the provider ID from the session
            Long providerId = (Long) session.getAttribute("sid");
            if (providerId == null) {
                model.addAttribute("error", "Please log in to add achievements.");
                return "redirect:/providerlogin"; // Redirect to login if session is invalid
            }

            // Fetch the provider by ID
            Provider provider = providerRepository.findById(providerId)
                    .orElseThrow(() -> new IllegalArgumentException("Provider not found"));

            // Save media files and collect their URLs
            List<String> mediaUrls = new ArrayList<>();
            for (MultipartFile mediaFile : mediaFiles) {
                if (!mediaFile.isEmpty()) {
                    String filename = mediaFile.getOriginalFilename();
                    if (isValidImage(filename)) {
                        Path imageDir = Paths.get("src/main/resources/static/images");
                        mediaUrls.add(saveFile(mediaFile, imageDir)); // Save image and add URL
                    } else if (isValidVideo(filename)) {
                        Path videoDir = Paths.get("src/main/resources/static/images");
                        mediaUrls.add(saveFile(mediaFile, videoDir)); // Save video and add URL
                    } else {
                        model.addAttribute("error", "Invalid file type. Only images and videos are allowed.");
                        return "redirect:/achievements";
                    }
                }
            }

            // Create and save the Achievement entity
            Achievement achievement = Achievement.builder()
                    .achievementName(name)
                    .description(description)
                    .image(mediaUrls)
                    .provider(provider)
                    .build();

            repository.save(achievement); // Save the achievement

        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("error", "An error occurred while saving the achievement.");
            return "redirect:/achievements";
        }

        return "redirect:/achievements";
    }


    private boolean isValidImage(String filename) {
        String lowerCaseFilename = filename.toLowerCase();
        return lowerCaseFilename.endsWith(".jpg") || lowerCaseFilename.endsWith(".jpeg")
                || lowerCaseFilename.endsWith(".png") || lowerCaseFilename.endsWith(".gif");
    }

    private boolean isValidVideo(String filename) {
        String lowerCaseFilename = filename.toLowerCase();
        return lowerCaseFilename.endsWith(".mp4") || lowerCaseFilename.endsWith(".avi")
                || lowerCaseFilename.endsWith(".mov") || lowerCaseFilename.endsWith(".mkv");
    }

    private String saveFile(MultipartFile file, Path directory) throws IOException {
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }

        String uniqueFilename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = directory.resolve(uniqueFilename);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return "/images/" + uniqueFilename;
    }

    @GetMapping("/delete/{id}")
    public String deleteAchievement(@PathVariable Long id) {
        repository.deleteById(id);
        return "redirect:/achievements";
    }
}
