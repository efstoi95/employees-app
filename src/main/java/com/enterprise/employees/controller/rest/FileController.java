//package com.enterprise.employees.controller.rest;
//
//import com.enterprise.employees.model.Task;
//import com.enterprise.employees.service.ProjectService;
//import com.enterprise.employees.service.TaskService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.annotation.Secured;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.util.Objects;
//import java.util.logging.Logger;
//
//@Controller
//@RequestMapping("/file")
//public class FileController {
//    Logger logger = Logger.getLogger(FileController.class.getName());
//
//    @Autowired
//    private ProjectService projectService;
//
//    @Autowired
//    private TaskService taskService;
//    @Secured("ROLE_ADMIN")
//    @GetMapping("/uploadFileToTask/{id}")
//    public String uploadFileToTask(@PathVariable Long id, Model model) {
//        logger.info("Uploading file to task");
//        Task task = taskService.findById(id);
//        model.addAttribute("task", task);
//        return "uploadFileToTask";
//    }
//    @Secured("ROLE_ADMIN")
//    @PostMapping("/uploadedFileToTask")
//    public String uploadFileToTask(@RequestParam("file") MultipartFile file) {
//        Task task = taskService.findById(Long.parseLong(Objects.requireNonNull(file.getOriginalFilename())));
//        Long taskId = task.getId();
//        try {
//            logger.info("Entering uploadFileToTask");
//            Long projectId = taskService.findById(taskId).getProject().getId();
//            projectService.uploadFileToTask(taskId, file);
//
//            logger.info("File uploaded successfully");
//            return "redirect:/web/uploadSuccess/" + projectId;
//        } catch (IOException e) {
//            logger.info("Error uploading file to task");
//            return "redirect:/web/uploadFileToTask/" + taskId;
//        }
//    }
//
//    @GetMapping("uploadSuccess/{id}")
//    public String uploadSuccess(@PathVariable Long projectId, Model model) {
//        model.addAttribute("projectId", projectId);
//        return "uploadSuccess";
//    }
//
//}
