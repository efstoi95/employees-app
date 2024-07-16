package com.enterprise.employees.controller.web;

import com.enterprise.employees.model.*;
import com.enterprise.employees.repository.EmployeesRepository;
import com.enterprise.employees.repository.FileRepository;
import com.enterprise.employees.repository.TaskRepository;
import com.enterprise.employees.service.EmployeeService;
import com.enterprise.employees.service.FileStorageService;
import com.enterprise.employees.service.ProjectService;
import com.enterprise.employees.service.TaskService;
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

import static com.enterprise.employees.controller.web.TaskController.format;

@RequiredArgsConstructor
@RequestMapping("/web")
@Controller
public class EmployeeTaskController {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeTaskController.class);
    private final ProjectService projectService;
    private final EmployeeService employeeService;
    private final TaskService taskService;
    private final FileStorageService fileStorageService;
    @Autowired
    TaskRepository taskRepository;
    @Autowired
    FileRepository fileRepository;
    @Autowired
    EmployeesRepository employeesRepository;

    @Secured("ROLE_USER")
    @GetMapping("/EmployeeTasks/{projectId}")
    public String EmployeeTasks(@PathVariable("projectId") Long projectId,
                                @RequestParam("employeeId") Long employeeId,
                                Model model) {
        Project existingProject = projectService.findById(projectId);

        List<Task> tasks = existingProject.getTasks()
                .stream().filter(task -> task.getEmployees()
                        .stream().anyMatch(employee -> employee.getId()
                                .equals(employeeId))).toList();
        tasks.forEach(task -> {
            task.setDurationInput(format(task.getDuration()));
        });
        Employee employee = employeeService.getEmployeeById(employeeId);
        model.addAttribute("tasks", tasks);
        model.addAttribute("employeeId", employeeId);
        return "EmployeeTasks";
    }

    /**
     * A description of the entire Java function.
     *
     * @param  taskId	the ID of the task to show files for
     * @param  model	the model to hold task data
     * @return         	the view name to display the task files
     */
    @Secured("ROLE_USER")
    @GetMapping("/showEmployeeTaskFiles/{taskId}")
    public String showFiles(@PathVariable("taskId") Long taskId,
                            @RequestParam("employeeId") Long employeeId, Model model) {
        TaskDTO task = taskService.findByIdDTO(taskId);
        Employee employee = employeeService.getEmployeeById(employeeId);
        List<File> employeeTaskFiles = employee.getFiles();

        Map<Long, String> employeeFileMap = employeeTaskFiles.stream()
                .collect(Collectors.toMap(File::getId, File::getFileName));

        model.addAttribute("employee", employee);
        model.addAttribute("employeeId", employeeId);
        model.addAttribute("task", task);
        model.addAttribute("fileNames", task.getFiles().stream().map(File::getFileName).toList());
        model.addAttribute("employeeFileMap", employeeFileMap);
        return "showEmployeeTaskFiles";
    }

    /**
     * Uploads a task description file for a given task ID.
     *
     * @param  taskId  the ID of the task to upload the file for
     * @param  files   the array of files to upload
     * @return         a redirect URL after successful upload
     */
    @PostMapping("/upload/{taskId}")
    public String uploadTaskDescriptionFile(@PathVariable Long taskId,
                                            @RequestParam("file") MultipartFile[] files,
                                            RedirectAttributes redirectAttributes,
                                            Model model) throws IOException {
        Long projectId = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid task ID: " + taskId))
                .getProject()
                .getId();
        Long employeeId = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid task ID: " + taskId))
                .getEmployees()
                .getFirst()
                .getId();

        //Check file sizes
        // Check file sizes and emptiness
        for (MultipartFile file : files) {
            byte[] fileContent = file.getBytes();
            if (file.isEmpty() || fileContent.length > 5 * 1024 * 1024 ) { // Check file size in bytes
                TaskDTO task = taskService.findByIdDTO(taskId);
                Employee employee = employeeService.getEmployeeById(employeeId);
                List<File> employeeTaskFiles = employee.getFiles();

                Map<Long, String> employeeFileMap = employeeTaskFiles.stream()
                        .collect(Collectors.toMap(File::getId, File::getFileName));

                model.addAttribute("employee", employee);
                model.addAttribute("employeeId", employeeId);
                model.addAttribute("task", task);
                model.addAttribute("fileNames", task.getFiles().stream().map(File::getFileName).toList());
                model.addAttribute("employeeFileMap", employeeFileMap);

                String errorMessage = file.isEmpty() ? "File is empty" : "File size exceeds maximum allowed size of 5MB";
                model.addAttribute("error", errorMessage);

                return "showEmployeeTaskFiles";
            }
        }
        fileStorageService.uploadEmployeeTaskFile(taskId, files,employeeId);
        redirectAttributes.addFlashAttribute("fileUploaded", true);
        return "redirect:/web/EmployeeTasks/" + projectId + "?employeeId=" + employeeId;
    }
    /**
     * Retrieves the task description file by task ID and file name.
     *
     * @param  taskId     the ID of the task to download the file for
     * @param  fileName   the name of the file to download
     * @return            a ResponseEntity containing the downloaded file content
     */
    @GetMapping("/downloadFile/{taskId}/{fileName}")
    public ResponseEntity<ByteArrayResource> downloadTaskDescriptionFile(@PathVariable Long taskId, @PathVariable String fileName) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new IllegalArgumentException("Invalid task ID: " + taskId));

        // Find the File entity by fileName
        File file = task.getFiles().stream()
                    .filter(f -> f.getFileName().equals(fileName))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("File not found for fileName: " + fileName));

        ByteArrayResource resource = new ByteArrayResource(file.getFileContent());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }

    @PostMapping("/deleteFile/{taskId}/{fileId}")
    public String deleteFile(@PathVariable Long taskId,
                             @PathVariable Long fileId,
                             @RequestParam("employeeId") Long employeeId,
                             RedirectAttributes redirectAttributes) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid task ID: " + taskId));
        Long projectId = taskRepository.findById(taskId)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid task ID: " + taskId))
                        .getProject()
                        .getId();
        Employee employee = employeesRepository.findById(employeeId)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid employee ID: " + employeeId));

        File fileToRemove = fileRepository.findById(fileId)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid file ID: " + fileId));

        if (!task.getFiles().contains(fileToRemove)) {
            throw new IllegalArgumentException("File is not associated with the given task: " + fileId);
        }
        if (!employee.getFiles().contains(fileToRemove)) {
            throw new IllegalArgumentException("File is not associated with the given employee: " + employeeId);
        }
        task.getFiles().remove(fileToRemove);
        fileToRemove.getTasks().remove(task);

        employee.getFiles().remove(fileToRemove);
        fileToRemove.getEmployees().remove(employee);

        taskRepository.save(task);
        employeeService.save(employee);
        fileRepository.delete(fileToRemove);

        logger.info("File deleted successfully!");
        redirectAttributes.addFlashAttribute("fileUploaded", true);
        return "redirect:/web/EmployeeTasks/" + projectId + "?employeeId=" + employeeId;
    }

    @GetMapping("/viewFile/{taskId}/{fileName}")
    public ResponseEntity<byte[]> viewFile(@PathVariable Long taskId, @PathVariable String fileName, Model model) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new IllegalArgumentException("Invalid task ID: " + taskId));

        // Find the File entity by fileName
        File file = task.getFiles().stream()
                .filter(f -> f.getFileName().equals(fileName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("File not found for fileName: " + fileName));

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
