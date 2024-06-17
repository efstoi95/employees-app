package com.enterprise.employees.service;

import com.enterprise.employees.model.Project;
import com.enterprise.employees.model.Task;
import com.enterprise.employees.repository.ProjectRepository;
import com.enterprise.employees.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileStorageService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    public void uploadTaskFile(Long taskId, MultipartFile[] files) throws IOException {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new IllegalArgumentException("Invalid task ID: " + taskId));
        // Get existing file contents and names
        List<byte[]> existingFileContents = task.getFileContent();
        List<String> existingFileNames = task.getFileNames();

        if (existingFileContents == null) {
            existingFileContents = new ArrayList<>();
        }
        if (existingFileNames == null) {
            existingFileNames = new ArrayList<>();
        }

        // Add new files to existing lists
        for (MultipartFile file : files) {
            existingFileContents.add(file.getBytes());
            existingFileNames.add(file.getOriginalFilename());
        }

        // Update task with combined lists
        task.setFileContent(existingFileContents);
        task.setFileNames(existingFileNames);

        // Save updated task
        taskRepository.save(task);

    }

    public void uploadProjectFile(Long projectId, MultipartFile[] files) throws IOException {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new IllegalArgumentException("Invalid project ID: " + projectId));
        List<byte[]> existingFileContents = project.getFileContent();
        List<String> existingFileNames = project.getFileNames();

        if (existingFileContents == null) {
            existingFileContents = new ArrayList<>();
        }
        if (existingFileNames == null) {
            existingFileNames = new ArrayList<>();
        }
        // Add new files to existing lists
        for (MultipartFile file : files) {
            existingFileContents.add(file.getBytes());
            existingFileNames.add(file.getOriginalFilename());
        }

        // Update task with combined lists
        project.setFileContent(existingFileContents);
        project.setFileNames(existingFileNames);

        projectRepository.save(project);
    }



}
