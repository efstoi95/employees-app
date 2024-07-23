package com.enterprise.employees.service;

import com.enterprise.employees.model.*;
import com.enterprise.employees.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class FileStorageService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private EmployeesRepository employeesRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    private final String uploadDir = "uploads/";

    public String saveFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("Failed to store empty file");
        }

        // Check if the file is an image
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IOException("The uploaded file is not an image");
        }

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String fileName = file.getOriginalFilename();
        assert fileName != null;
        Path filePath = uploadPath.resolve(fileName);

        // Check if the file already exists
        if (Files.exists(filePath)) {
            filePath = getUniqueFilePath(uploadPath, fileName);
        }

        Files.copy(file.getInputStream(), filePath);

        // Return the file URL (adjust this to match your deployment setup)
        return "/uploads/" + filePath.getFileName().toString();
    }

    private Path getUniqueFilePath(Path uploadPath, String fileName) {
        String name = fileName;
        String extension = "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            name = fileName.substring(0, dotIndex);
            extension = fileName.substring(dotIndex);
        }

        Path filePath = uploadPath.resolve(fileName);
        int counter = 1;
        while (Files.exists(filePath)) {
            fileName = name + "_" + counter + extension;
            filePath = uploadPath.resolve(fileName);
            counter++;
        }
        return filePath;
    }

    public void uploadTaskFile(Long taskId, MultipartFile[] files) throws IOException {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new IllegalArgumentException("Invalid task ID: " + taskId));
        // Get existing file contents and names
       List<File> existingFiles = task.getFiles();
        if (existingFiles == null) {
            existingFiles = new ArrayList<>();
        }

        for (MultipartFile file : files) {
            File newFile = new File();
            newFile.setFileName(file.getOriginalFilename());
            newFile.setFileContent(file.getBytes());
            existingFiles.add(newFile);
        }
        // Save updated task
        taskRepository.save(task);

    }

    public void uploadProjectFile(Long projectId, MultipartFile[] files) throws IOException {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new IllegalArgumentException("Invalid project ID: " + projectId));
       List<File> existingFiles = project.getFiles();

        if(existingFiles == null) {
            existingFiles = new ArrayList<>();
        }
        for (MultipartFile file : files) {
            File newFile = new File();
            newFile.setFileName(file.getOriginalFilename());
            newFile.setFileContent(file.getBytes());
            existingFiles.add(newFile);
        }
        project.setFiles(existingFiles);

        projectRepository.save(project);
    }
    public void uploadEmployeeProjectFile(Long projectId, MultipartFile[] files, Long employeeId) throws IOException {
        Project project = projectRepository.findById(projectId)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid project ID: " + projectId));

        Employee employee = employeesRepository.findById(employeeId)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid employee ID: " + employeeId));

        List<File> existingFiles = project.getFiles();
        if(existingFiles == null) {
            existingFiles = new ArrayList<>();
        }

        List<File> employeeFiles = employee.getFiles();
        if (employeeFiles == null) {
            employeeFiles = new ArrayList<>();
        }

        for (MultipartFile file : files) {
            File newFile = new File();
            newFile.setFileName(file.getOriginalFilename());
            newFile.setFileContent(file.getBytes());
            existingFiles.add(newFile);
            employeeFiles.add(newFile);
        }
        employee.setFiles(employeeFiles);
        project.setFiles(existingFiles);

        projectRepository.save(project);
        employeesRepository.save(employee);
    }


    public void uploadEmployeeTaskFile(Long taskId, MultipartFile[] files, Long employeeId) throws IOException {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new IllegalArgumentException("Invalid task ID: " + taskId));
        Employee employee = employeesRepository.findById(employeeId).orElseThrow(() -> new IllegalArgumentException("Invalid employee ID: " + employeeId));

        // Get existing files
        List<File> existingFiles = task.getFiles();

        if (existingFiles == null) {
            existingFiles = new ArrayList<>();
        }

        List<File> employeeFiles = employee.getFiles();
        if (employeeFiles == null) {
            employeeFiles = new ArrayList<>();
        }
        for (MultipartFile file : files) {
            File newFile = new File();
            newFile.setFileName(file.getOriginalFilename());
            newFile.setFileContent(file.getBytes());
            existingFiles.add(newFile);
            employeeFiles.add(newFile);
        }
        employee.setFiles(employeeFiles);
        task.setFiles(existingFiles);

        // Save updated task and employee
        taskRepository.save(task);
        employeesRepository.save(employee);
    }


    public void uploadResourceFile(Long resourceId, MultipartFile[] files) throws IOException {

        Resource resource = resourceRepository.findById(resourceId).orElseThrow(() -> new IllegalArgumentException("Invalid resource ID: " + resourceId));
        List<File> existingFiles = resource.getFiles();

        if(existingFiles == null) {
            existingFiles = new ArrayList<>();
        }
        for (MultipartFile file : files) {
            File newFile = new File();
            newFile.setFileName(file.getOriginalFilename());
            newFile.setFileContent(file.getBytes());
            existingFiles.add(newFile);
        }
        resource.setFiles(existingFiles);

        resourceRepository.save(resource);
    }
}
