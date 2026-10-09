
package com.blog.blogservice.controller;

import com.blog.blogservice.entity.BlogPost;
import com.blog.blogservice.service.BlogPostService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class BlogPostController {

    private final BlogPostService blogPostService;

    public BlogPostController(BlogPostService blogPostService) {
        this.blogPostService = blogPostService;
    }

    // Create a blog post
    @PostMapping
    public BlogPost createPost(@RequestBody BlogPost post) {
        return blogPostService.createPost(post);
    }

    // Get all blog posts
    @GetMapping
    public List<BlogPost> getAllPosts() {
        return blogPostService.getAllPosts();
    }

    // Get a single blog post
    @GetMapping("/{id}")
    public BlogPost getPostById(@PathVariable Long id) {
        return blogPostService.getPostById(id)
                .orElseThrow(() ->
                        new RuntimeException("Blog post not found: " + id));
    }

    // Update a blog post
    @PutMapping("/{id}")
    public BlogPost updatePost(
            @PathVariable Long id,
            @RequestBody BlogPost post) {
        return blogPostService.updatePost(id, post);
    }

    // Delete a blog post
    @DeleteMapping("/{id}")
    public void deletePost(@PathVariable Long id) {
        blogPostService.deletePost(id);
    }

    // Search blog posts by title
    @GetMapping("/search")
    public List<BlogPost> searchPosts(@RequestParam String term) {
        return blogPostService.searchPosts(term);
    }
}