package com.SkillConnect.demo.controller;

import com.SkillConnect.demo.entity.Bookings;
import com.SkillConnect.demo.entity.Chat;
import com.SkillConnect.demo.entity.Provider;
import com.SkillConnect.demo.entity.User;
import com.SkillConnect.demo.repository.BookingRepository;
import com.SkillConnect.demo.repository.ChatRepository;
import com.SkillConnect.demo.repository.ProviderRepository;
import com.SkillConnect.demo.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    // Method to show the form to send a new message
    @GetMapping("/create/{id}")
    public String showChatForm(Model model) {
        model.addAttribute("chat", new Chat());  // Create a new Chat object for the form
        return "chatbox";  // Thymeleaf template for the chat creation form
    }

    // Method to save a new chat message
    @PostMapping("/save")
    @ResponseBody
    public String saveChat(@RequestParam String message,
                           @RequestParam Long userId,
                           @RequestParam Long providerId
    ) {
        Chat chat = new Chat();
        chat.setSendAt(LocalDateTime.now());  // Set the current time when the message is sent
        chat.setReceiverId(providerId);
        chat.setSenderId(userId);
        chat.setMessage(message);
        chatRepository.save(chat);
        return "OK";  // Return response after saving
    }

    @GetMapping("/providerList")  // This will list chats for a provider
    public String listOfProviderChats(Model model, HttpSession session,
                                      @RequestParam("id") Optional<Long> userId) {
        Long providerId = (Long) session.getAttribute("sid");

        List<Bookings> bookings = bookingRepository.findByProviderId(providerId);

        List<User> userList = bookings.stream()
                .map(booking -> userRepository.findByEmail(booking.getBookedBy()))
                .filter(user -> user != null)
                .distinct()
                .toList();

        model.addAttribute("users", userList);
        model.addAttribute("providerId", providerId);

        if (userId.isPresent()) {
            boolean userHasBookedProvider = bookings.stream()
                    .anyMatch(booking -> booking.getBookedBy().equals(userRepository.findById(userId.get()).orElseThrow().getEmail()));

            if (userHasBookedProvider) {
                model.addAttribute("userId", userId.get());
                List<Chat> messages = chatRepository.findBySenderIdOrReceiverId(providerId, userId.get());
                model.addAttribute("messages", messages);
            } else {
                model.addAttribute("error", "You cannot chat with this provider as you have not booked their services.");
            }
        }

        return "chat/providerList";
    }


    @GetMapping("/list")
    public String listChats(Model model, HttpSession session, @RequestParam("id") Optional<Long> providerId) {
        String email = (String) session.getAttribute("userEmail");
        List<Bookings> bookings = bookingRepository.findByBookedBy(email);
        List<Provider> providerList = bookings.stream()
                .map(booking -> providerRepository.findById(booking.getProviderId()).orElse(null))
                .filter(provider -> provider != null)
                .distinct()
                .toList();

        model.addAttribute("providers", providerList);
        model.addAttribute("userId", session.getAttribute("userId")); // Add user ID to the model

        if (providerId.isPresent()) {
            model.addAttribute("providerId", providerId.get());

            Long userId = (Long) session.getAttribute("userId");
            List<Chat> messages = chatRepository.findBySenderIdOrReceiverId(userId, providerId.get());
            model.addAttribute("messages", messages);
        }

        return "chat/list";
    }


    // Method to show chat details for a specific chat id
    @GetMapping("/details/{id}")
    public String showChatDetails(@PathVariable("id") Long providerId, Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        List<Chat> messages = chatRepository.findBySenderIdAndReceiverId(userId, providerId);
        model.addAttribute("messages", messages);
        return "chat/details";  // Thymeleaf template for the chat details page
    }

    // Method to delete a chat message
    @GetMapping("/delete/{id}")
    public String deleteChat(@PathVariable("id") Long id) {
        chatRepository.deleteById(id);
        return "redirect:/chat/userList";  // Redirect to the user chat list after deletion
    }

    // Method to show edit form for a specific chat message
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Chat chat = chatRepository.findById(id).orElse(null);
        model.addAttribute("chat", chat);
        return "chat/edit";  // Thymeleaf template for editing the chat
    }

    // Method to update an existing chat message
    @PostMapping("/update/{id}")
    public String updateChat(@PathVariable("id") Long id, @ModelAttribute("chat") Chat chat) {
        chat.setId(id);  // Ensure the ID is preserved during update
        chat.setSendAt(LocalDateTime.now());  // Update the send time during modification
        chatRepository.save(chat);
        return "redirect:/chat/userList";  // Redirect to the user chat list after update
    }

    // Method to get all messages for a specific user and provider (optional feature)
    @GetMapping("/user/{userId}/provider/{providerId}")
    public String getChatsByUserAndProvider(@PathVariable("userId") Long userId,
                                            @PathVariable("providerId") Long providerId, Model model) {
        List<Chat> chatList = chatRepository.findBySenderIdAndReceiverId(userId, providerId);
        model.addAttribute("chatList", chatList);
        return "chat/list";  // Show the list of chats between user and provider
    }
}
