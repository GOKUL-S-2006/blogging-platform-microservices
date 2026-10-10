package com.blog.webservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

@Controller
public class AuthPageController {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public AuthPageController() {
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:8081")
                .build();

        this.objectMapper = new ObjectMapper();
    }

    // Redirect the root page to login
    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    // Show signup page
    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }

    // Show login page
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    // Handle signup form
    @PostMapping("/signup")
    public String signup(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            Model model) {

        try {
            restClient.post()
                    .uri("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "username", username,
                            "email", email,
                            "password", password
                    ))
                    .retrieve()
                    .toBodilessEntity();

            model.addAttribute(
                    "message",
                    "Account created successfully. Please log in."
            );

            return "login";

        } catch (RestClientResponseException ex) {
            ex.printStackTrace();

            model.addAttribute(
                    "error",
                    "Signup failed: HTTP " + ex.getStatusCode()
            );

            return "signup";

        } catch (Exception ex) {
            ex.printStackTrace();

            model.addAttribute(
                    "error",
                    "Could not connect to the authentication service: "
                            + ex.getMessage()
            );

            return "signup";
        }
    }

    // Handle login form
    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        try {
            // Send login credentials to the backend
            String responseBody = restClient.post()
                    .uri("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "username", username,
                            "password", password
                    ))
                    .retrieve()
                    .body(String.class);

            if (responseBody == null || responseBody.isBlank()) {
                model.addAttribute(
                        "error",
                        "The authentication service returned an empty response."
                );
                return "login";
            }

            // Parse the JSON response
            JsonNode response = objectMapper.readTree(responseBody);

            // Find the JWT field returned by the backend
            JsonNode tokenNode = response.hasNonNull("token")
                    ? response.get("token")
                    : response.hasNonNull("accessToken")
                      ? response.get("accessToken")
                      : response.hasNonNull("jwt")
                        ? response.get("jwt")
                        : null;

            if (tokenNode == null
                    || !tokenNode.isTextual()
                    || tokenNode.asText().isBlank()) {

                model.addAttribute(
                        "error",
                        "Login response did not contain a recognized JWT field."
                );

                return "login";
            }

            // Store the JWT and username in the session
            session.setAttribute("jwt", tokenNode.asText());
            session.setAttribute("username", username);

            // Redirect after successful authentication
            return "redirect:/home";

        } catch (RestClientResponseException ex) {
            ex.printStackTrace();

            model.addAttribute(
                    "error",
                    "Login failed. HTTP " + ex.getStatusCode()
                            + ". Check your username and password."
            );

            return "login";

        } catch (Exception ex) {
            ex.printStackTrace();

            model.addAttribute(
                    "error",
                    "Login error: " + ex.getMessage()
            );

            return "login";
        }
    }

    // Handle logout
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
