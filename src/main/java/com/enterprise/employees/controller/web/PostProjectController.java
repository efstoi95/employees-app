package com.enterprise.employees.controller.web;

import com.enterprise.employees.model.Employee;
import com.enterprise.employees.model.Post;
import com.enterprise.employees.model.Project;
import com.enterprise.employees.service.FileStorageService;
import com.enterprise.employees.service.PostServiceImpl;
import com.enterprise.employees.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.*;

@RequiredArgsConstructor
@RequestMapping("/posts")
@Controller
public class PostProjectController {
    private final PostServiceImpl postService;
    private final ProjectService projectService;
    private final FileStorageService fileStorageService;

    @GetMapping("/allPosts/{projectId}")
    public String allPosts(@PathVariable("projectId") Long projectId, Model model) {
        Project project = projectService.findById(projectId);
        List<Post> posts = project.getPosts();
        model.addAttribute("posts", posts);
        return "allPosts";
    }

    @GetMapping("/createPost/{projectId}")
    public String createPost(@PathVariable("projectId") Long projectId, Model model) {
        Post post = new Post();
        Project project = projectService.findById(projectId);
        post.setProject(project);
        model.addAttribute("post", post);
        return "createPost";
    }
    @PostMapping("/createdPost")
    public String createdPost(@Validated @ModelAttribute("post") Post post, RedirectAttributes redirectAttributes) {
        Long projectId = post.getProject().getId();
       Employee employee = (Employee) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        post.setAuthor(employee);
        postService.create(post);
        redirectAttributes.addFlashAttribute("postCreated", true);
        redirectAttributes.addFlashAttribute("postId", post.getId());
        redirectAttributes.addFlashAttribute("postTitle", post.getTitle());

        return "redirect:/posts/allPosts/" + projectId;
    }
    @GetMapping("/deletePost/{postId}")
    public String deletePost(@PathVariable("postId") Long postId, RedirectAttributes redirectAttributes) {
        Post post = postService.findById(postId);
        String postTitle = post.getTitle();
        postService.deleteById(postId);

        redirectAttributes.addFlashAttribute("postDeleted", true);
        redirectAttributes.addFlashAttribute("postId", postId);
        redirectAttributes.addFlashAttribute("postTitle", postTitle);
        return "redirect:/posts/allPosts/" + post.getProject().getId();
    }

    @GetMapping("/viewPost/{postId}")
    public String viewPost(@PathVariable("postId") Long postId, Model model) {
        Optional<Post> optionalPost = Optional.ofNullable(postService.findById(postId));

        if (optionalPost.isPresent()) {
            Post post = optionalPost.get();
            model.addAttribute("projectId", post.getProject().getId());
            model.addAttribute("post", post);
            return "viewPost";
        } else {
            throw new IllegalArgumentException("Post not found with id: " + postId);
        }
    }

}
