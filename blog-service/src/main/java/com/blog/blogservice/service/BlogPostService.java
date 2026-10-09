
package com.blog.blogservice.service;

import com.blog.blogservice.entity.BlogPost;
import com.blog.blogservice.repository.BlogPostRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BlogPostService {

    private final BlogPostRepository blogPostRepository;

    public BlogPostService(BlogPostRepository blogPostRepository) {
        this.blogPostRepository = blogPostRepository;
    }

    public BlogPost createPost(BlogPost post) {
        return blogPostRepository.save(post);
    }

    public List<BlogPost> getAllPosts() {
        return blogPostRepository.findAll();
    }

    public Optional<BlogPost> getPostById(Long id) {
        return blogPostRepository.findById(id);
    }

    public BlogPost updatePost(Long id, BlogPost updatedPost) {
        BlogPost existingPost = blogPostRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Blog post not found: " + id));

        existingPost.setTitle(updatedPost.getTitle());
        existingPost.setContent(updatedPost.getContent());
        existingPost.setAuthor(updatedPost.getAuthor());

        return blogPostRepository.save(existingPost);
    }

    public void deletePost(Long id) {
        blogPostRepository.deleteById(id);
    }

    public List<BlogPost> searchPosts(String term) {
        return blogPostRepository.findAll().stream()
                .filter(post ->
                        post.getTitle() != null &&
                                post.getTitle().toLowerCase()
                                        .contains(term.toLowerCase()))
                .toList();
    }
}