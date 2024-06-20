package com.enterprise.employees.controller.web;

import com.enterprise.employees.model.Project;
import com.enterprise.employees.model.Task;
import com.enterprise.employees.repository.ProjectRepository;
import com.enterprise.employees.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

@RequiredArgsConstructor
@RequestMapping("/projects")
@Controller
public class ProjectController {

    @Autowired
    private ProjectRepository projectRepository;
    private final FileStorageService fileStorageService;
    private static final Logger logger = Logger.getLogger(ProjectController.class.getName());

    @PostMapping("/upload/{projectId}")
    public String uploadProjectDescriptionFile(@PathVariable Long projectId, @RequestParam("file") MultipartFile[] files) throws IOException {
        fileStorageService.uploadProjectFile(projectId, files);
        return "redirect:/projects/successUploadToProject";
    }

    @GetMapping("/downloadFile/{projectId}/{fileName}")
    public ResponseEntity<ByteArrayResource> downloadTaskDescriptionFile(@PathVariable Long projectId, @PathVariable String fileName) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new IllegalArgumentException("Invalid task ID: " + projectId));

        byte[] fileContent = project.getFileContentByFileName(fileName);
        if(fileContent == null) {
            throw new IllegalArgumentException("Invalid file name: " + fileName);
        }
        ByteArrayResource resource = new ByteArrayResource(fileContent);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resource);

    }

    @PostMapping("/deleteFile/{projectId}/{index}")
    public String deleteFile(@PathVariable Long projectId, @PathVariable int index) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid task ID: " + projectId));

        // Remove file content and file name at the specified index
        List<byte[]> fileContents = project.getFileContent();
        List<String> fileNames = project.getFileNames();

        if (fileContents != null && index >= 0 && index < fileContents.size()) {
            fileContents.remove(index);
            fileNames.remove(index);
        }

        // Update task with updated lists
        project.setFileContent(fileContents);
        project.setFileNames(fileNames);
        projectRepository.save(project);

        // Redirect back to the task details page or wherever appropriate
        return "redirect:/projects/successDeleteProjectFile"; // Adjust the redirect URL as per your application's navigation
    }

    @GetMapping("/successDeleteProjectFile")
    public String successDeleteFile( Model model) {
        model.addAttribute("message", "File deleted successfully");
        return "successDeleteProjectFile";
    }





    @GetMapping("/successUploadToProject")
    public String successUploadToProject(Model model) {
        model.addAttribute("message", "File uploaded successfully");
        return "successUploadToProject";
    }
}
