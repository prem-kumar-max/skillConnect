package com.SkillConnect.demo.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.SkillConnect.demo.entity.Achievement;
import com.SkillConnect.demo.repository.IAchievementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.SkillConnect.demo.entity.Bookings;
import com.SkillConnect.demo.entity.Provider;
import com.SkillConnect.demo.entity.User;
import com.SkillConnect.demo.repository.BookingRepository;
import com.SkillConnect.demo.repository.ProviderRepository;
import com.SkillConnect.demo.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping()
public class UserController {

	@Autowired
	UserRepository userRepository;

	@Autowired
	ProviderRepository providerRepository;

	@Autowired
	BookingRepository bookingRepository;

    @Autowired
    private IAchievementRepository achievementRepository;

    @GetMapping("/user_dashboard")
    public String userDashboard(Model model) {
        model.addAttribute("pageName", "user_dashboard");
        return "user_dashboard";  // Will return 'user_dashboard.html' (user dashboard)
    }

    @GetMapping("/profile")
    public String showProfilePage(HttpSession session,Model model) {
    	User user=userRepository.findByEmail((String)session.getAttribute("userEmail"));
    	model.addAttribute("user", user);

        //model.addAttribute("message", "Profile updated successfully!");
        model.addAttribute("pageName", "profile");
    	System.out.print(user);
        return "profile";  // Return profile.html
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("pageName", "index");
        return "index";  // Renders the 'index.html' page
    }

    @GetMapping("/register-professional")
    public String showProfessionalRegistrationForm(Model model) {
        model.addAttribute("pageName", "register_professional");
        return "register-professional"; // This maps to the register-professional.html file
    }

    @GetMapping("/cooking")
    public String showCookingPage(Model model) {
        model.addAttribute("pageName", "cooking");
        return "cooking"; // This maps to the cooking.html file
    }

    @GetMapping("/music")
    public String showmusicPage(Model model) {
        model.addAttribute("pageName", "music");
        return "music"; // This maps to the cooking.html file
    }

    @GetMapping("/remaking")
    public String showremakingPage(Model model) {
        model.addAttribute("pageName", "remaking");
        return "remaking";
    }

    @GetMapping("/chatbox")
    public String showChatboxPage(Model model) {
        model.addAttribute("pageName", "chatbox");
        return "chatbox"; // This maps to the chatbox.html file
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("pageName", "login");
        return "login"; // Renders the 'login.html' page from the templates folder
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("pageName", "register");
        return "register";  // Renders the 'register.html' page
    }

    @GetMapping("achievements/get/all")
    public String getAllAchievements(Model model) {
        List<Achievement> achievements = achievementRepository.findAll();

        // Debugging Output
        achievements.forEach(achievement -> {
            if (achievement.getProvider() == null) {
                System.out.println("Achievement without a provider: " + achievement);
            } else {
                System.out.println("Achievement: " + achievement.getAchievementName() +
                        " | Provider: " + achievement.getProvider().getFullName());
            }
        });

        model.addAttribute("achievements", achievements);
        return "explore"; // Ensure the explore.html file is in the templates folder
    }


    @PostMapping("/register")
    public String registerUser(@RequestParam String fullName,
                                               @RequestParam String email,
                                               @RequestParam String state,
                                               @RequestParam String city,
                                               @RequestParam String password) {

        if (userRepository.findByEmail(email) != null) {
            return "register";
        }

        // Create a new User object and set values
        User newUser = new User();
        newUser.setFullName(fullName);
        newUser.setEmail(email);
        newUser.setState(state);
        newUser.setCity(city);

        // Encrypt the password before saving
        newUser.setPassword(password);

        // Save the user to the database
        userRepository.save(newUser);

        // Return a success response
        return "login";
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam String email,
                            @RequestParam String password,
                            HttpSession session,Model model) {

        User user = userRepository.findByEmail(email);
        if (user == null) {
            return "login";
        }

        if (!password.equals(user.getPassword())) {
            return "login";
        }

        session.setAttribute("userEmail", email);
        session.setAttribute("userId", user.getId());
        model.addAttribute("user",email);
        return "user_action";
    }

    @PostMapping("/updateProfile")
    public String updateProfile(@RequestParam("fullName") String fullname,
    		@RequestParam("state") String state,
    		@RequestParam("city") String city,
    		HttpSession session, RedirectAttributes redirectAttributes) {
        // Retrieve the current user from the session
        User currentUser = (User) userRepository.findByEmail((String)session.getAttribute("userEmail"));

        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("message", "You must be logged in to update your profile.");
            return "redirect:/login"; // Redirect to login if the user is not logged in
        }

        // Update the current user's details
        currentUser.setFullName(fullname);
        currentUser.setCity(city);
        currentUser.setState(state);

        // Save updated user to the database
        userRepository.save(currentUser);

        // Update the session with the updated user data
        session.setAttribute("user", currentUser);

        // Add a success message to the model
        redirectAttributes.addFlashAttribute("message", "Profile updated successfully!");

        return "redirect:/profile"; // Redirect to the user's dashboard after update
    }

    @GetMapping("/fetchProvidersByCategory")
    public String fetchProvidersByCategory(@RequestParam("category") String category,
                                           HttpSession session,
                                           Model model) {

        List<Provider> providers = providerRepository.getProvidersByCategory(category);


        // Get the email of the currently logged-in user from the session (if exists)
        String userEmail = (String) session.getAttribute("userEmail");

        // Determine whether the user is logged in based on the session
        boolean isLoggedIn = userEmail != null;

        // Add data to the model to pass to the view
        model.addAttribute("providers", providers);
        model.addAttribute("category", category);
        model.addAttribute("isLoggedIn", isLoggedIn);
        model.addAttribute("pageName", "category_" + category.toLowerCase());

        // Conditional logic to return a different view based on the category
        if ("music".equalsIgnoreCase(category)) {
            return "music";
        } else if ("homemaking".equalsIgnoreCase(category)) {
            return "remaking";
        } else {
            return "cooking_details";
        }
    }

    @GetMapping("/fetchProvidersByCategoryInUser")
    public String fetchProvidersByCategoryInUser(@RequestParam("category") String category, HttpSession session, Model model) {
        List<Provider> providers = providerRepository.getProvidersByCategory(category);
        String userEmail = (String) session.getAttribute("userEmail");
        boolean isLoggedIn = userEmail != null;
        model.addAttribute("providers", providers);
        model.addAttribute("category", category);
        model.addAttribute("isLoggedIn", isLoggedIn);

        if ("music".equalsIgnoreCase(category)) {
            return "music";
        } else if ("homemaking".equalsIgnoreCase(category)) {
            return "remaking_details";
        } else {
            return "cooking_details";
        }
    }

    @GetMapping("/bookProvider/{providerId}")
    public String bookProvider(@PathVariable("providerId") Long providerId, Model model) {
        // Fetch the provider's details by ID
        Optional<Provider> providerOptional = providerRepository.findById(providerId);
        if (providerOptional.isPresent()) {
            Provider provider = providerOptional.get();
            model.addAttribute("providers", provider);

            // Use the provider's category to determine the view
            if ("music".equalsIgnoreCase(provider.getCategory())) {
                return "music";
            } else if ("homemaking".equalsIgnoreCase(provider.getCategory())) {
                return "remaking_details";
            } else {
                return "cooking_details";
            }
        } else {
            return "redirect:/error"; // Redirect to an error page if the provider doesn't exist
        }
    }

    @GetMapping("/cooking_details/{id}")
    public String cookingDetails(@PathVariable Long id, HttpSession session, Model model) {
        // Fetch the provider by ID
        Optional<Provider> provider = providerRepository.findById(id); // Ensure this method returns a Provider or null

        // Check if the provider exists
        if (provider.isEmpty()) {
            // Handle the case where the provider is not found
            return "redirect:/error"; // or return an error view
        }

        // Check if the user is logged in
        String userEmail = (String) session.getAttribute("userEmail");
        boolean isLoggedIn = userEmail != null;

        // Add data to the model
        model.addAttribute("provider", provider);
        model.addAttribute("isLoggedIn", isLoggedIn);
        model.addAttribute("pageName", "cooking_details");

        return "cooking_details";  // This will render the 'cooking_details.html' template
    }

    @GetMapping("/booking/details")
    public String bookingDetails(HttpSession session, Model model) {
        String userEmail = (String) session.getAttribute("userEmail");
        List<Bookings> bookings = bookingRepository.findByBookedBy(userEmail);

        model.addAttribute("bookings", bookings);

        model.addAttribute("pageName", "bookingdetails");
        return "bookingdetails"; // This must match the HTML file name
    }

    @GetMapping("/cooking/details")
    public String showCookingDetails(HttpSession session, Model model) {
        String userEmail = (String) session.getAttribute("userEmail");
        model.addAttribute("pageName", "cooking_details");

        if (userEmail != null) {
            return "cooking_details"; // This will render 'cooking_details.html'
        } else {
            return "cooking"; // This will render 'cooking.html'
        }
    }
}
