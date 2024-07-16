package com.enterprise.employees.service;

import com.enterprise.employees.model.Employee;
import com.enterprise.employees.model.File;
import com.enterprise.employees.model.Project;
import com.enterprise.employees.model.Task;
import com.enterprise.employees.repository.EmployeesRepository;
import com.enterprise.employees.repository.ProjectRepository;
import com.enterprise.employees.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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


}
