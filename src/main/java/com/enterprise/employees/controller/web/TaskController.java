package com.enterprise.employees.controller.web;

import com.enterprise.employees.model.Task;
import com.enterprise.employees.repository.TaskRepository;
import com.enterprise.employees.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

@RequiredArgsConstructor
@Controller
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;
    private final FileStorageService fileStorageService;
    private static final Logger logger = Logger.getLogger(TaskController.class.getName());


    @PostMapping("/upload/{taskId}")
    public String uploadTaskDescriptionFile(@PathVariable Long taskId, @RequestParam("file") MultipartFile[] files) throws IOException {

        Long projectId = taskRepository.findById(taskId).orElseThrow(() -> new IllegalArgumentException("Invalid task ID: " + taskId)).getProject().getId();
        fileStorageService.uploadTaskFile(taskId, files);
        logger.info("File uploaded successfully");
        return "redirect:/tasks/successUploadToTask/" + projectId;
    }

    @GetMapping("/downloadFile/{taskId}/{fileName}")
    public ResponseEntity<ByteArrayResource> downloadTaskDescriptionFile(@PathVariable Long taskId, @PathVariable String fileName) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new IllegalArgumentException("Invalid task ID: " + taskId));

        byte[] fileContent = task.getFileContentByFileName(fileName);
        if(fileContent == null) {
            throw new IllegalArgumentException("Invalid file name: " + fileName);
        }
        ByteArrayResource resource = new ByteArrayResource(fileContent);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resource);

    }

    @PostMapping("/deleteFile/{taskId}/{index}")
    public String deleteFile(@PathVariable Long taskId, @PathVariable int index) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid task ID: " + taskId));
        Long projectId = taskRepository.findById(taskId).get().getProject().getId();

        // Remove file content and file name at the specified index
        List<byte[]> fileContents = task.getFileContent();
        List<String> fileNames = task.getFileNames();

        if (fileContents != null && index >= 0 && index < fileContents.size()) {
            fileContents.remove(index);
            fileNames.remove(index);
        }

        // Update task with updated lists
        task.setFileContent(fileContents);
        task.setFileNames(fileNames);
        taskRepository.save(task);

        // Redirect back to the task details page or wherever appropriate
        return "redirect:/tasks/successDeleteFile/" + projectId; // Adjust the redirect URL as per your application's navigation
    }

    @GetMapping("/successDeleteFile/{projectId}")
    public String successDeleteFile(@PathVariable Long projectId, Model model) {
        model.addAttribute("message", "File deleted successfully");
        model.addAttribute("projectId", projectId);
        return "successDeleteFile";
    }




    @GetMapping("/successUploadToTask/{projectId}")
    public String successUploadToTask(@PathVariable Long projectId, Model model) {
        model.addAttribute("message", "File uploaded successfully");
        model.addAttribute("projectId", projectId);

        return "successUploadToTask";
    }
}
