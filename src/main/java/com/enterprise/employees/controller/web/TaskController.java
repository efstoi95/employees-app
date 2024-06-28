package com.enterprise.employees.controller.web;

import com.enterprise.employees.model.*;
import com.enterprise.employees.repository.TaskRepository;
import com.enterprise.employees.service.*;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Controller
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;
    private final FileStorageService fileStorageService;
    private final ProjectServiceImpl projectServiceImpl;
    private final SkillServiceImpl skillService;
    private final TaskServiceImpl taskServiceImpl;
    private final EmployeeService employeeService;
    private final ModelMapper modelMapper;
    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);

    /**
     * Creates a new task.
     *
     * @param  id    description of parameter
     * @param  model description of parameter
     * @return       description of return value
     */
    @GetMapping("/createTask/{id}")
    @Secured("ROLE_ADMIN")
    public String CreateTask(@PathVariable Long id,Model model) {
        logger.info("Creating new task");
        TaskDTO taskDTO = new TaskDTO();
        ProjectDTO projectDTO = projectServiceImpl.findByIdDTO(id);
        Long projectId = projectDTO.getId();
        taskDTO.setProjectId(projectId);
        model.addAttribute("task", taskDTO);
        model.addAttribute("skills", skillService.getAllSkillsDTO());
        return "createTask";
    }


    @PostMapping("/createdTask")
    public String createTask(@Validated @ModelAttribute("task") TaskDTO taskDTO, BindingResult bindingResult, Model model) {
        logger.info("Creating new task: {}", taskDTO);
        Long projectId = taskDTO.getProjectId();
        taskServiceImpl.create(taskDTO , bindingResult);
        if(bindingResult.hasErrors()){
            model.addAttribute("task", taskDTO);
            model.addAttribute("skills", skillService.getAllSkillsDTO());
            return "createTask";
        }
        return "redirect:/tasks/successCreateTask/"+projectId; // A new template to show the task creation success and available employees
    }


    @GetMapping("/editTask/{id}")
    public String editTask(@PathVariable Long id,Model model) {
        logger.info("Adding employee to task");
        TaskDTO taskDTO = taskServiceImpl.findByIdDTO(id);
        Long projectId = taskDTO.getProjectId();
        Project project = projectServiceImpl.findById(projectId);
        String projectName = project.getName();

        List<Long> skillsId = taskDTO.getSkillsIds();
        List<Skill> requiredSkills = skillService.findSkillsByIds(skillsId);
        model.addAttribute("projectName",projectName);
        model.addAttribute("task",taskDTO);
        model.addAttribute(
                "employees",
                project.getEmployees().stream()
                        .filter(e -> e.getSkills().stream().anyMatch(requiredSkills::contains))
                        .collect(Collectors.toList())
        );
        return "editTask";
    }
    @PostMapping("/editedTask")
    public String editedTask(@RequestParam(value = "eligibleEmployees", required = false) List<Long> eligibleEmployeesIds,
                             @Validated @ModelAttribute("task") TaskDTO taskDTO, BindingResult bindingResult, Model model) {
            Long projectId = taskDTO.getProjectId();
            if(eligibleEmployeesIds != null) {
                taskDTO.setEmployeeIds(eligibleEmployeesIds);
            }
            taskServiceImpl.editTask(taskDTO, bindingResult);
            System.out.println(taskDTO);
            return "redirect:/tasks/successEditTask/"+ projectId;
    }
    @GetMapping("/deleteTask/{id}")
    public String deleteTask(@PathVariable Long id,Model model) {
        logger.info("Deleting task with ID: {}", id);
        Task existingTask = taskServiceImpl.findById(id);
        Long projectId = existingTask.getProject().getId();
        taskServiceImpl.deleteById(id);
        logger.info("Task with ID {} deleted successfully", id);
        return "redirect:/tasks/successDeleteTask/"+ projectId;
    }

    /**
     * A description of the entire Java function.
     *
     * @param  model	the model to hold task data
     * @return         	the view name to display all tasks
     */
    @GetMapping("/allTasks/{id}")
    @Secured("ROLE_ADMIN")
    public String showAllTasks(@PathVariable("id") Long id, Model model) {
            Project project = projectServiceImpl.findById(id);
            List<Task> tasks = project.getTasks();
            tasks.forEach(task -> {
                task.setDurationInput(format(task.getDuration()));
            });
            logger.info("Number of tasks retrieved: {}", tasks.size());
            model.addAttribute("tasks", tasks);
            model.addAttribute("taskId", id);
            return "allTasks";
    }

    public static String format(Duration duration){
        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;

        if (days > 0) {
            if (hours > 0 && minutes > 0) {
                return String.format("%dd %dh %dm", days, hours, minutes);
            } else if (hours > 0) {
                return String.format("%dd %dh", days, hours);
            } else if (minutes > 0) {
                return String.format("%dd %dm", days, minutes);
            } else {
                return String.format("%dd", days);
            }
        } else if (hours > 0) {
            if (minutes > 0) {
                return String.format("%dh %dm", hours, minutes);
            } else {
                return String.format("%dh", hours);
            }
        } else {
            return String.format("%dm", minutes);
        }
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
                                            @RequestParam("file") MultipartFile[] files,Model model) throws IOException {
            Long projectId = taskRepository.findById(taskId).orElseThrow(() -> new IllegalArgumentException("Invalid task ID: " + taskId)).getProject().getId();
            fileStorageService.uploadTaskFile(taskId, files);
            logger.info("File uploaded successfully");
            return "redirect:/tasks/successUploadToTask/" + projectId;
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
            Long projectId = taskRepository.findById(taskId).orElseThrow(() -> new IllegalArgumentException("Invalid task ID: " + taskId)).getProject().getId();
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

    @GetMapping("/successCreateTask/{id}")
    public String successCreateTask(@PathVariable Long id, Model model) {
        model.addAttribute("message", "The information of the task created");
        model.addAttribute("projectId", id);
        logger.info("The information of the task created.");
        return "successTask";

    }
    @GetMapping("/successEditTask/{id}")
    public String successAddEmployeeToTask(@PathVariable Long id, Model model){
        model.addAttribute("message", "The information of the task changed");
        model.addAttribute("projectId", id);
        return "successTask";
    }
    @GetMapping("/successDeleteTask/{id}")
    public String successDeleteTask(@PathVariable Long id, Model model){
        model.addAttribute("message", "The information of the task deleted");
        model.addAttribute("projectId", id);
        return "successTask";
    }

    @GetMapping("/successDeleteFile/{projectId}")
    public String successDeleteFile(@PathVariable Long projectId, Model model) {
        model.addAttribute("message", "File deleted successfully");
        model.addAttribute("projectId", projectId);
        return "successTask";
    }


    @GetMapping("/successUploadToTask/{projectId}")
    public String successUploadToTask(@PathVariable Long projectId, Model model) {
        model.addAttribute("message", "File uploaded successfully");
        model.addAttribute("projectId", projectId);

        return "successTask";
    }
}
