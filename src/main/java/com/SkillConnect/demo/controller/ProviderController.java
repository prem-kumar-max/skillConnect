package com.SkillConnect.demo.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import com.SkillConnect.demo.dto.ProviderDto;
import com.SkillConnect.demo.entity.Achievement;
import com.SkillConnect.demo.repository.IAchievementRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.SkillConnect.demo.entity.Bookings;
import com.SkillConnect.demo.entity.Provider;
import com.SkillConnect.demo.repository.BookingRepository;
import com.SkillConnect.demo.repository.ProviderRepository;
import com.SkillConnect.demo.service.BookingService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ProviderController {

	@Autowired
	ProviderRepository providerRepository;

	@Autowired
	BookingRepository bookingRepository;

	@Autowired
	BookingService bookingService;

	@Autowired
	private IAchievementRepository achievementRepository;


	@GetMapping("/provider_dashboard")
	public String providerDashboard(HttpSession session, Model model) {
		// Fetch the provider's email from the session
		String providerEmail = (String) session.getAttribute("providerEmail");

		// Fetch the provider by email
		Provider provider = providerRepository.findByEmail(providerEmail);
		if (provider == null) {
			throw new IllegalArgumentException("Provider not found for email: " + providerEmail);
		}

		// Fetch the bookings for the provider with status 'Pending'
		List<Bookings> bookings = bookingRepository.findByProviderIdAndStatus(provider.getId(), "Pending");

		// Add provider and bookings to the model
		model.addAttribute("provider", provider);
		model.addAttribute("bookings", bookings);

		// Return the view name
		return "provider_dashboard";
	}


	@GetMapping("/registerprovider")
	public String registerProviderForm() {
		return "register-professional";
	}


	@PostMapping("/registerprovider")
	public String registerProvider(@ModelAttribute @Valid ProviderDto providerDto,
								   BindingResult bindingResult,
								   Model model) {
		if (bindingResult.hasErrors()) {
			return "register-professional";
		}

		try {
			if (providerRepository.existsByEmail(providerDto.getEmail())) {
				model.addAttribute("error", "Email already in use.");
				return "register-professional";
			}
			if (providerRepository.existsByPhone(providerDto.getPhone())) {
				model.addAttribute("error", "Mobile number already in use.");
				return "register-professional";
			}

			// Save profile image
			String imageUrl = null;
			if (providerDto.getProfileImageUrl() != null && isValidImage(providerDto.getProfileImageUrl().getOriginalFilename())) {
				Path imageDir = Paths.get("src/main/resources/static/images");
				imageUrl = saveFile(providerDto.getProfileImageUrl(), imageDir);
			}


			// Save provider
			Provider provider = Provider.builder()
					.fullName(providerDto.getFullName())
					.email(providerDto.getEmail())
					.password(providerDto.getPassword())
					.phone(providerDto.getPhone())
					.state(providerDto.getState())
					.city(providerDto.getCity())
					.category(providerDto.getCategory())
					.chargePerService(providerDto.getChargePerService())
					.profileImageUrl(imageUrl)
					.build();

			providerRepository.save(provider);
			return "redirect:/providerlogin";
		} catch (IOException e) {
			model.addAttribute("error", "File upload failed. Please try again.");
			return "register-professional";
		} catch (Exception e) {
			model.addAttribute("error", "An unexpected error occurred. Please try again later.");
			return "register-professional";
		}
	}


	private String saveFile(MultipartFile file, Path targetDir) throws IOException {
		if (!Files.exists(targetDir)) {
			Files.createDirectories(targetDir);
		}
		String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
		Path targetFilePath = targetDir.resolve(fileName);
		file.transferTo(targetFilePath);
		return fileName;
	}


	private boolean isValidImage(String fileName) {
		String[] validExtensions = {".jpg", ".jpeg", ".png", ".gif"};
		return Arrays.stream(validExtensions).anyMatch(fileName.toLowerCase()::endsWith);
	}

	private boolean isValidVideo(String fileName) {
		String[] validExtensions = {".mp4", ".avi", ".mov", ".mkv"};
		return Arrays.stream(validExtensions).anyMatch(fileName.toLowerCase()::endsWith);
	}




	@GetMapping("/providerlogin")
	public String providerlogin() {
		return "providerlogin";
	}

//	@GetMapping("/providerProfile")
//	public String providerProfile(){
//		return "provider-profile";
//	}

	@PostMapping("/providerlogin")
	public String loginProvider(@RequestParam("email") String email, @RequestParam("password") String password,
	        HttpSession session, RedirectAttributes redirectAttributes, Model model) {

	    // Find provider by email
	    Provider provider = providerRepository.findByEmail(email);

	    // Check if provider exists
	    if (provider == null) {
	        // If provider does not exist, redirect to login page with error
	        redirectAttributes.addFlashAttribute("message", "Email does not exist");
	        return "providerlogin";
	    }

	    // Validate the password
		if (!password.equals(provider.getPassword())) {
	        // If password does not match, redirect to login page with error
	        redirectAttributes.addFlashAttribute("message", "Incorrect password");
	        return "providerlogin";
	    }

	    // Successful login
	    session.setAttribute("providerEmail", provider.getEmail());
	    session.setAttribute("sid", provider.getId());
	    Provider p=providerRepository.findByEmail(email);
	    model.addAttribute("provider",p);

	    return "redirect:/provider_dashboard"; // Redirect to the provider's dashboard
	}


	@GetMapping("/providerProfile")
	public String providerProfile(HttpSession session, Model model){
		Long providerId = (Long) session.getAttribute("sid");

		if (providerId != null) {
			Optional<Provider> providerOptional = providerRepository.findById(providerId);
			System.out.println(providerOptional.get());
			if (providerOptional.isPresent()) {
				model.addAttribute("provider", providerOptional.get());
				return "provider-profile";
			} else {
				return "redirect:/providerlogin";
			}
		} else {
			return "redirect:/providerlogin";
		}
	}

	@GetMapping("/provider/profile/{id}")
	public String getProviderProfile(@PathVariable Long id, Model model, HttpSession session) {
		Optional<Provider> providerOptional = providerRepository.findById(id);

		if (!providerOptional.isPresent()) {
			model.addAttribute("error", "Provider not found");
			return "error/404"; // Ensure this error template exists
		}

		model.addAttribute("provider", providerOptional.get());
		return "provider-profile"; // Matches the template name "providerProfile.html"
	}

	@GetMapping("/editproviderprofile/{providerId}")
	public String editProviderProfile(@PathVariable("providerId") Long providerId, Model model) {
		Optional<Provider> provider = providerRepository.findById(providerId);

		if (provider.isPresent()) {
			model.addAttribute("provider", provider.get());
		} else {
			return "redirect:/error/404";
		}

		return "edit-provider-profile";
	}

	@PostMapping("/saveprovider")
	public String saveProvider(@ModelAttribute Provider provider, Model model) {
		providerRepository.save(provider);
		return "redirect:/providerprofile";
	}

	@GetMapping("/get/all/achievements")
	public String getAllAchievements(Model model) {
		List<Achievement> achievements = achievementRepository.findAll();

		// Debugging Output
		achievements.forEach(achievement -> {
			if (achievement.getProvider() == null) {
				System.out.println("Achievement without a provider: " + achievement.getId());
			} else {
				System.out.println("Achievement: " + achievement.getAchievementName() +
						" | Provider: " + achievement.getProvider().getFullName());
			}
		});

		model.addAttribute("achievements", achievements);
		return "explore"; // Ensure 'explore.html' is in the templates folder
	}



	// Optionally, you can create a logout endpoint to invalidate the session
	@GetMapping("/logout")
	public String logout(HttpSession session) {
		session.invalidate(); // This will invalidate the session
		return "redirect:/"; // Redirect to login page after logout
	}

	@GetMapping("/provider/{id}")
	public ResponseEntity<Provider> fetchProviderDetails(@PathVariable Long id) {
		Optional<Provider> provider = providerRepository.findById(id);

		if (provider.isPresent()) {
			return ResponseEntity.ok(provider.get()); // Return provider details if found
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Return 404 if not found
		}
	}

	@GetMapping("/bookserviceprovider")
	public String bookServiceProvider(@RequestParam Long id, // Provider ID
			HttpSession session // To retrieve the userEmail from session
	) {
		session.setAttribute("providerid", id);

		return "booknow";
	}

	@PostMapping("/bookprovider")
	public String bookservice(@RequestParam("date") String date, @RequestParam("time") String time, HttpSession session) {
		// Check if session attributes are not available
		Long providerId = (Long) session.getAttribute("providerid");
		String userEmail = (String) session.getAttribute("userEmail");

		// If either attribute is missing, redirect to login page
		if (providerId == null || userEmail == null) {
			return "redirect:/login"; // Redirect to the login page
		}

		// Proceed with booking creation if session attributes are available
		Provider provider = providerRepository.findById(providerId).orElse(null);

		if (provider == null) {
			// If the provider is not found, you might want to handle this scenario,
			// possibly redirecting to an error page or handling the null case gracefully
			return "redirect:/error";
		}

		Bookings booking = new Bookings();
		booking.setBookedBy(userEmail);
		booking.setProviderEmail(provider.getEmail());
		booking.setDate(date);
		booking.setTime(time);
		booking.setCategory(provider.getCategory());
		booking.setCharge(provider.getChargePerService());
		booking.setName(provider.getFullName());
		booking.setStatus("pending");
		booking.setProviderId(provider.getId());

		// Save the booking to the repository
		bookingRepository.save(booking);

		// Redirect to chat page after successful booking
		return "redirect:/chat/list";
	}

	@GetMapping("/bookings")
	public String userbookings(HttpSession session, Model model) {
		// Check if the session attribute "userEmail" exists
		String userEmail = (String) session.getAttribute("userEmail");

		// If userEmail is not found in session, redirect to login page
		if (userEmail == null) {
			return "redirect:/login"; // Redirect to the login page
		}

		// Retrieve the list of bookings for the logged-in user
		List<Bookings> bookings = bookingRepository.findByBookedBy(userEmail);

		// Add the list of bookings to the model to be displayed in the view
		model.addAttribute("bookings", bookings);

		// Return the view name to render
		return "bookings";
	}

	@PostMapping("/deleteBooking")
	public String deleteBooking(@RequestParam("bookingId") Long bookingId, HttpSession session, Model model) {
		// Check if the user is logged in
		String userEmail = (String) session.getAttribute("userEmail");
		if (userEmail == null) {
			// Redirect to login page if the user is not logged in
			return "redirect:/login";
		}

		// Check if the booking exists and belongs to the logged-in user
		Optional<Bookings> bookingOptional = bookingRepository.findById(bookingId);
		if (bookingOptional.isPresent()) {
			Bookings booking = bookingOptional.get();
			if (booking.getBookedBy().equals(userEmail)) {
				// Delete the booking
				bookingRepository.deleteById(bookingId);

				// Redirect to the booking details page
				return "redirect:/booking/details";
			} else {
				// If the booking doesn't belong to the logged-in user, add an error message
				model.addAttribute("errorMessage", "You do not have permission to delete this booking.");
			}
		} else {
			// If the booking is not found, add an error message
			model.addAttribute("errorMessage", "Booking not found.");
		}

		// Redirect back to the booking details page with the error message
		return "bookingdetails";
	}

	@GetMapping("/pendingBookings")
	public String pendingBookings(HttpSession session, Model model) {
	    // Check if service provider is logged in
	    Long providerId = (Long) session.getAttribute("sid");
	    if (providerId == null) {
	        return "redirect:/providerlogin";
	    }

	    List<Bookings> bookings = bookingRepository.findByProviderIdAndStatus(providerId, "Pending");

	    model.addAttribute("bookings", bookings);
	    return "providerbookings";
	}

	@PostMapping("/acceptBooking")
    public String acceptBooking(@RequestParam("bookingId") Long bookingId, RedirectAttributes redirectAttributes) {
        try {
            bookingService.updateBookingStatus(bookingId, "ACCEPTED");
            redirectAttributes.addFlashAttribute("message", "Booking accepted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error accepting booking: " + e.getMessage());
        }
        return "redirect:/pendingBookings"; // Redirect to the pendingBookings page
    }

    @PostMapping("/rejectBooking")
    public String rejectBooking(@RequestParam("bookingId") Long bookingId, RedirectAttributes redirectAttributes) {
        try {
            bookingService.updateBookingStatus(bookingId, "REJECTED");
            redirectAttributes.addFlashAttribute("message", "Booking rejected successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error rejecting booking: " + e.getMessage());
        }
        return "redirect:/pendingBookings"; // Redirect to the pendingBookings page
    }

    @GetMapping("/acceptedbookings")
    public String acceptedbookings(HttpSession session, Model model)
	{
		Long providerId = (Long) session.getAttribute("sid");
		if (providerId == null) {
			return "redirect:/providerlogin";
		}

		List<Bookings> bookings = bookingRepository.findByProviderId(providerId);
    	model.addAttribute("bookings",bookings);
    	return "acceptedbookings";
    }

    @PostMapping("/searchproviders")
    public String searchProviders(@RequestParam("city") String city, @RequestParam("category") String category, Model model) {
        // Query the database for a provider matching the city and category
        List<Provider> provider = providerRepository.findByCityAndCategory(city, category);

        if (provider == null) {
            // Add a message to the model if no provider is found
            model.addAttribute("error", "No provider found for the selected city and category.");

            return "cooking";
        }

        // Add the found provider to the model to display its details
        model.addAttribute("providers", provider);
        model.addAttribute("category",category);
        return "cooking"; // Return a view showing provider details
    }

    @GetMapping("/editprofile/{id}")
    public String editprofile(@PathVariable("id") Long id ,Model model,HttpSession session){
    	Optional<Bookings> booking = bookingRepository.findById(id);
    	session.setAttribute("providerid", id);
    	if(booking.isPresent()) {
    		Bookings booking1=booking.get();
    		Provider provider=providerRepository.findByEmail((String)booking1.getProviderEmail());
    		model.addAttribute("provider",provider);
    	}
    	return "editprofile";
    }

    @PostMapping("/updateprovider")
    public String updateprovider(Provider provider, HttpSession session) {
        // Retrieve the provider from the session based on providerid
        Optional<Provider> optionalProvider = providerRepository.findById((Long) session.getAttribute("providerid"));

        // Check if the provider exists
        if (optionalProvider.isPresent()) {
            // Get the provider object from Optional
            Provider existingProvider = optionalProvider.get();

            // Update the existing provider details
            existingProvider.setCategory(provider.getCategory());
            existingProvider.setChargePerService(provider.getChargePerService());
            existingProvider.setCity(provider.getCity());
            existingProvider.setEmail(provider.getEmail());
            existingProvider.setFullName(provider.getFullName());
            existingProvider.setPhone(provider.getPhone());
            existingProvider.setProfileImageUrl(provider.getProfileImageUrl());
            existingProvider.setState(provider.getState());

            // Save the updated provider object back to the repository
            providerRepository.save(existingProvider);

            // Redirect to the provider dashboard
            return "redirect:/provider_dashboard";
        } else {
            // Handle the case where the provider is not found (you can redirect or show an error)
            return "redirect:/error";
        }
    }
	@GetMapping("/booking/details/{bookingId}")
	public String getBookingDetails(@PathVariable Long bookingId,Model model)
	{
		Optional<Bookings> bookings = bookingRepository.findById(bookingId);

		model.addAttribute("booking",bookings.get());
		return "detailsOfBooking";
	}

}


