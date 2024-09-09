package com.enterprise.employees.controller.web;

import com.enterprise.employees.model.Employee;
import com.enterprise.employees.model.Post;
import com.enterprise.employees.model.Project;
import com.enterprise.employees.model.Task;
import com.enterprise.employees.service.PostServiceImpl;
import com.enterprise.employees.service.ProjectService;
import com.enterprise.employees.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@RequiredArgsConstructor
@RequestMapping("/posts")
@Controller
public class PostTaskController {
    private final ProjectService projectService;
    private final TaskService taskService;
    private final PostServiceImpl postService;
    @Autowired
    private MessageSource messageSource;

    @GetMapping("/allTasksPosts/{taskId}")
    public String allPosts(@PathVariable("taskId") Long taskId,
                           @RequestParam(name = "locale", required = false) String localeParam,
                           Model model) {

        Locale locale = Locale.getDefault();
        if (localeParam != null) {
            locale=Locale.forLanguageTag(localeParam);
        }
        LocaleContextHolder.setLocale(locale);
        String message = messageSource.getMessage("allComments.title", null, locale);
        model.addAttribute("message", message);
        Task task = taskService.findById(taskId);
        Long projectId = task.getProject().getId();
        List<Post> posts = task.getPosts();
        model.addAttribute("posts", posts);
        model.addAttribute("projectId", projectId);
        model.addAttribute("taskId", taskId);
        model.addAttribute("isAllTasksPostsPage", true);
        return "allTasksPosts";
    }
    @GetMapping("/allTasksPostsEmployee/{taskId}")
    public String allPostsEmployee(@PathVariable("taskId") Long taskId,
                                   @RequestParam("employeeId") Long employeeId,
                                   @RequestParam(name = "locale", required = false) String localeParam,
                                   Model model) {
        Locale locale = Locale.getDefault();
        if (localeParam != null) {
            locale=Locale.forLanguageTag(localeParam);
        }
        LocaleContextHolder.setLocale(locale);
        String message = messageSource.getMessage("allComments.title", null, locale);
        model.addAttribute("message", message);
        Task task = taskService.findById(taskId);
        List<Post> posts = task.getPosts();
        Long projectId = task.getProject().getId();
        model.addAttribute("posts", posts);
        model.addAttribute("taskId", taskId);
        model.addAttribute("projectId", projectId);
        model.addAttribute("employeeId", employeeId);
        model.addAttribute("isAllTasksPostsEmployeePage", true);
        return "allTasksPostsEmployee";
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

    @GetMapping("/viewPostTaskEmployee/{postId}")
    public String viewPostProjectEmployee(@PathVariable("postId") Long postId, Model model) {
        Optional<Post> optionalPost = Optional.ofNullable(postService.findById(postId));
        if (optionalPost.isPresent()) {
            Employee employee = (Employee) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Post post = optionalPost.get();
            model.addAttribute("taskId", post.getTask().getId());
            model.addAttribute("employeeId", employee.getId());
            model.addAttribute("post", post);
            return "viewPostTaskEmployee";
        } else {
            throw new IllegalArgumentException("Post not found with id: " + postId);
        }
    }
}
