package com.enterprise.employees.controller.web;

import com.enterprise.employees.model.Employee;
import com.enterprise.employees.model.Post;
import com.enterprise.employees.model.Project;
import com.enterprise.employees.model.Task;
import com.enterprise.employees.service.PostServiceImpl;
import com.enterprise.employees.service.ProjectService;
import com.enterprise.employees.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RequestMapping("/posts")
@Controller
public class PostTaskController {
    private final ProjectService projectService;
    private final TaskService taskService;
    private final PostServiceImpl postService;

    @GetMapping("/allTasksPosts/{taskId}")
    public String allPosts(@PathVariable("taskId") Long taskId, Model model) {
        Task task = taskService.findById(taskId);
        Long projectId = task.getProject().getId();
        List<Post> posts = task.getPosts();
        model.addAttribute("posts", posts);
        model.addAttribute("projectId", projectId);
        model.addAttribute("taskId", taskId);
        return "allTasksPosts";
    }

    @GetMapping("/createTaskPost/{projectId}/{taskId}")
    public String createPost(@PathVariable("projectId") Long projectId,@PathVariable("taskId") Long taskId, Model model) {
        Post post = new Post();
        Project project = projectService.findById(projectId);
        Task task = taskService.findById(taskId);
        post.setTask(task);
        post.setProject(project);
        model.addAttribute("post", post);
        return "createTaskPost";
    }

    @PostMapping("/createdTaskPost")
    public String createdPost(@Validated @ModelAttribute("post") Post post, RedirectAttributes redirectAttributes) {
        Long taskId = post.getTask().getId();
        Employee employee = (Employee) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        post.setAuthor(employee);
        postService.create(post);
        redirectAttributes.addFlashAttribute("postCreated", true);
        redirectAttributes.addFlashAttribute("postId", post.getId());
        redirectAttributes.addFlashAttribute("postTitle", post.getTitle());

        return "redirect:/posts/allTasksPosts/" + taskId;
    }

    @GetMapping("/deleteTaskPost/{postId}")
    public String deleteTaskPost(@PathVariable("postId") Long postId, RedirectAttributes redirectAttributes) {
        Post post = postService.findById(postId);
        String postTitle = post.getTitle();
        postService.deleteById(postId);

        redirectAttributes.addFlashAttribute("postDeleted", true);
        redirectAttributes.addFlashAttribute("postId", postId);
        redirectAttributes.addFlashAttribute("postTitle", postTitle);
        return "redirect:/posts/allTasksPosts/" + post.getTask().getId();
    }

    @GetMapping("/viewTaskPost/{postId}")
    public String viewTaskPost(@PathVariable("postId") Long postId, Model model) {
        Optional<Post> optionalPost = Optional.ofNullable(postService.findById(postId));

        if (optionalPost.isPresent()) {
            Post post = optionalPost.get();
            model.addAttribute("taskId", post.getTask().getId());
            model.addAttribute("post", post);
            return "viewTaskPost";
        } else {
            throw new IllegalArgumentException("Post not found with id: " + postId);
        }
    }
}
