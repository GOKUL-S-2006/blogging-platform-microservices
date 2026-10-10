package com.blog.webservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
public class HomeController {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public HomeController() {
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:8081")
                .build();

        this.objectMapper = new ObjectMapper();
    }

    // READ: display all posts
    @GetMapping("/home")
    public String home(
            HttpSession session,
            Model model,
            @ModelAttribute("message") String message,
            @ModelAttribute("error") String error) {

        String username =
                (String) session.getAttribute("username");

        String jwt =
                (String) session.getAttribute("jwt");

        if (username == null || jwt == null) {
            return "redirect:/login";
        }

        model.addAttribute("username", username);
        model.addAttribute("message", message);
        model.addAttribute("error", error);

        try {
            String responseBody = restClient.get()
                    .uri("/api/posts")
                    .header(
                            HttpHeaders.AUTHORIZATION,
                            "Bearer " + jwt
                    )
                    .retrieve()
                    .body(String.class);

            JsonNode posts = objectMapper.readTree(responseBody);

            model.addAttribute("posts", posts);

        } catch (Exception ex) {
            ex.printStackTrace();
            model.addAttribute(
                    "error",
                    "Unable to load posts: " + ex.getMessage()
            );
        }

        return "home";
    }

    // CREATE: create a new post
    @PostMapping("/posts/create")
    public String createPost(
            @RequestParam String title,
            @RequestParam String content,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String jwt = (String) session.getAttribute("jwt");

        if (jwt == null) {
            return "redirect:/login";
        }

        try {
            restClient.post()
                    .uri("/api/posts")
                    .header(
                            HttpHeaders.AUTHORIZATION,
                            "Bearer " + jwt
                    )
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "title", title,
                            "content", content
                    ))
                    .retrieve()
                    .toBodilessEntity();

            redirectAttributes.addFlashAttribute(
                    "message", "Post created successfully!"
            );

        } catch (Exception ex) {
            ex.printStackTrace();
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Could not create post: " + ex.getMessage()
            );
        }

        return "redirect:/home";
    }

    // UPDATE: update an existing post
    @PostMapping("/posts/{id}/update")
    public String updatePost(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String content,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String jwt = (String) session.getAttribute("jwt");

        if (jwt == null) {
            return "redirect:/login";
        }

        try {
            restClient.put()
                    .uri("/api/posts/{id}", id)
                    .header(
                            HttpHeaders.AUTHORIZATION,
                            "Bearer " + jwt
                    )
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "title", title,
                            "content", content
                    ))
                    .retrieve()
                    .toBodilessEntity();

            redirectAttributes.addFlashAttribute(
                    "message", "Post updated successfully!"
            );

        } catch (Exception ex) {
            ex.printStackTrace();
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Could not update post: " + ex.getMessage()
            );
        }

        return "redirect:/home";
    }

    // DELETE: delete an existing post
    @PostMapping("/posts/{id}/delete")
    public String deletePost(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String jwt = (String) session.getAttribute("jwt");

        if (jwt == null) {
            return "redirect:/login";
        }

        try {
            restClient.delete()
                    .uri("/api/posts/{id}", id)
                    .header(
                            HttpHeaders.AUTHORIZATION,
                            "Bearer " + jwt
                    )
                    .retrieve()
                    .toBodilessEntity();

            redirectAttributes.addFlashAttribute(
                    "message", "Post deleted successfully!"
            );

        } catch (Exception ex) {
            ex.printStackTrace();
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Could not delete post: " + ex.getMessage()
            );
        }

        return "redirect:/home";
    }
}
