package com.enterprise.employees.controller.web;

import com.enterprise.employees.model.Employee;
import com.enterprise.employees.model.File;
import com.enterprise.employees.model.Project;
import com.enterprise.employees.model.ProjectDTO;
import com.enterprise.employees.repository.EmployeesRepository;
import com.enterprise.employees.repository.FileRepository;
import com.enterprise.employees.repository.ProjectRepository;
import com.enterprise.employees.service.EmployeeService;
import com.enterprise.employees.service.FileStorageService;
import com.enterprise.employees.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RequestMapping("/web")
@Controller
public class EmployeeProjectController {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeProjectController.class);
    private final EmployeeService employeeService;
    private final ProjectService projectService;
    private final FileStorageService fileStorageService;
    @Autowired
    ProjectRepository projectRepository;
    @Autowired
    EmployeesRepository employeesRepository;
    @Autowired
    FileRepository fileRepository;

    @Secured("ROLE_USER")
    @GetMapping("/EmployeeProjects/{id}")
    public String EmployeeProjects(@PathVariable("id") Long employeeId, Model model) {
        logger.info("Employee ID: {}", employeeId);
        Employee existingEmployee = employeeService.getEmployeeById(employeeId);
        List<Project> projects = existingEmployee.getProjects().stream().toList();
        model.addAttribute("projects", projects);
        model.addAttribute("employeeId", employeeId);
        return "EmployeeProjects";
    }

    /**
     * Uploads a file.
     *
     * @param  projectId	description of parameter
     * @param  files	    description of parameter
     * @return         	description of return value
     */
    @PostMapping("/uploadProjectFile/{projectId}")
    public String uploadProjectDescriptionFile(@PathVariable Long projectId,
                                               @RequestParam("file") MultipartFile[] files,
                                               RedirectAttributes redirectAttributes,
                                               Model model) throws IOException {

        Project project = projectRepository.findById(projectId)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid task ID: " + projectId));
        Long employeeId = projectRepository.findById(projectId)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid task ID: " + projectId))
                        .getEmployees()
                        .getFirst()
                        .getId();
        // Check file sizes and emptiness
        for (MultipartFile file : files) {
            byte[] fileContent = file.getBytes();
            if (file.isEmpty() || fileContent.length > 5 * 1024 * 1024) { // Check file size in bytes
                Employee employee = employeeService.getEmployeeById(employeeId);
                List<File> employeeProjectFiles = employee.getFiles();

                Map<Long, String> employeeFileMap = employeeProjectFiles.stream()
                        .collect(Collectors.toMap(File::getId, File::getFileName));

                model.addAttribute("project", project);
                model.addAttribute("employeeFileMap", employeeFileMap);
                model.addAttribute("employeeId", employeeId);
                model.addAttribute("employee", employee);
                model.addAttribute("fileNames", project.getFiles().stream().map(File::getFileName).collect(Collectors.toList()));

                String errorMessage = file.isEmpty() ? "File is empty" : "File size exceeds maximum allowed size of 5MB";
                model.addAttribute("error", errorMessage);

                return "showEmployeeProjectFiles";
            }
        }

        logger.info("Uploading file to project with ID: {}", projectId);
        redirectAttributes.addFlashAttribute("fileUploaded", true);
        fileStorageService.uploadEmployeeProjectFile(projectId, files, employeeId);

        return "redirect:/web/EmployeeProjects/" + employeeId;
    }
    /**
     * Downloads a file.
     * @param  projectId	description of parameter
     * @param  fileName	    description of parameter
     * @return         	description of return value
     */
    @GetMapping("/downloadProjectFile/{projectId}/{fileName}")
    public ResponseEntity<ByteArrayResource> downloadTaskDescriptionFile(@PathVariable Long projectId, @PathVariable String fileName, Model model) {
        logger.info("Downloading file from project with ID: {}", projectId);
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new IllegalArgumentException("Invalid task ID: " + projectId));
        File file = project.getFiles().stream()
                .filter(f -> f.getFileName().equals(fileName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid file name: " + fileName));

        ByteArrayResource resource = new ByteArrayResource(file.getFileContent());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resource);

    }

    @PostMapping("/deleteProjectFile/{projectId}/{fileId}")
    public String deleteFile(@PathVariable Long projectId,
                             @PathVariable Long fileId,
                             @RequestParam("employeeId") Long employeeId,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        logger.info("Deleting file from project with ID: {}", projectId);
        Project project = projectRepository.findById(projectId)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid task ID: " + projectId));
        Employee employee = employeesRepository.findById(employeeId)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid employee ID: " + employeeId));
        File fileToRemove = fileRepository.findById(fileId)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid file ID: " + fileId));
        if(!project.getFiles().contains(fileToRemove)) {
            throw new IllegalArgumentException("Invalid file ID: " + fileId);
        }
        if(!employee.getFiles().contains(fileToRemove)) {
            throw new IllegalArgumentException("Invalid file ID: " + fileId);
        }
        project.getFiles().remove(fileToRemove);
        fileToRemove.getProjects().remove(project);

        employee.getFiles().remove(fileToRemove);
        fileToRemove.getEmployees().remove(employee);

        projectRepository.save(project);
        employeesRepository.save(employee);
        fileRepository.delete(fileToRemove);

        redirectAttributes.addFlashAttribute("fileDeleted", true);
        return "redirect:/web/EmployeeProjects/" + employeeId;

    }

    @GetMapping("/showEmployeeProjectFiles/{projectId}")
    public String showFiles(@PathVariable("projectId") Long projectId,
                            @RequestParam("employeeId") Long employeeId,
                            Model model) {
        Employee employee = employeeService.getEmployeeById(employeeId);
        Project project = projectService.findById(projectId);
        List<File> employeeProjectFiles = employee.getFiles();

        Map<Long, String> employeeFileMap = employeeProjectFiles.stream()
                .collect(Collectors.toMap(File::getId, File::getFileName));

        model.addAttribute("project", project);
        model.addAttribute("employeeFileMap", employeeFileMap);
        model.addAttribute("employeeId", employeeId);
        model.addAttribute("employee", employee);
        model.addAttribute("fileNames", project.getFiles().stream().map(File::getFileName).collect(Collectors.toList()));
        return "showEmployeeProjectFiles";
    }

    @GetMapping("/viewProjectFile/{projectId}/{fileName}")
    public ResponseEntity<byte[]> viewFile(@PathVariable Long projectId, @PathVariable String fileName, Model model) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new IllegalArgumentException("Invalid task ID: " + projectId));
        File file = project.getFiles().stream()
                .filter(f -> f.getFileName().equals(fileName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid file name: " + fileName));


        HttpHeaders headers = new HttpHeaders();
        if (fileName.endsWith(".pdf")) {
            headers.add(HttpHeaders.CONTENT_TYPE, "application/pdf");
        } else if (fileName.endsWith(".txt")) {
            headers.add(HttpHeaders.CONTENT_TYPE, "text/plain; charset=UTF-8");
        } else if (fileName.endsWith(".png")) {
            headers.add(HttpHeaders.CONTENT_TYPE, "image/png");
        }  else if (fileName.endsWith(".jpg")) {
            headers.add(HttpHeaders.CONTENT_TYPE, "image/jpg");
        }else {
            throw new IllegalArgumentException("Unsupported file type: " + fileName);
        }
        return ResponseEntity.ok()
                .headers(headers)
                .body(file.getFileContent());

    }
}
