package com.enterprise.employees.controller.web;

import com.enterprise.employees.model.*;
import com.enterprise.employees.repository.FileRepository;
import com.enterprise.employees.repository.TaskRepository;
import com.enterprise.employees.service.*;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Controller
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private MessageSource messageSource;
    private final FileStorageService fileStorageService;
    private final ProjectServiceImpl projectServiceImpl;
    private final SkillServiceImpl skillService;
    private final TaskServiceImpl taskServiceImpl;
    private final ResourceService resourceService;
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
    public String CreateTask(@PathVariable Long id,@RequestParam(name = "locale", required = false) String localeParam,
                             Model model) {
        logger.info("Creating new task");
        Locale locale = Locale.getDefault();
        if (localeParam != null) {
            locale = Locale.forLanguageTag(localeParam);
        }
        LocaleContextHolder.setLocale(locale);
        String message = messageSource.getMessage("createTask.title", null, locale);
        model.addAttribute("message", message);
        TaskDTO taskDTO = new TaskDTO();
        ProjectDTO projectDTO = projectServiceImpl.findByIdDTO(id);
        Long projectId = projectDTO.getId();
        taskDTO.setProjectId(projectId);
        model.addAttribute("task", taskDTO);
        model.addAttribute("skills", skillService.getAllSkillsDTO());
        model.addAttribute("isCreateTaskPage", true);
        model.addAttribute("projectId", projectId);
        return "createTask";
    }

    @PostMapping("/createdTask")
    public String createTask(@Validated @ModelAttribute("task") TaskDTO taskDTO,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        logger.info("Creating new task: {}", taskDTO);
        Long projectId = taskDTO.getProjectId();
        taskServiceImpl.create(taskDTO , bindingResult);
        if(bindingResult.hasErrors()){
            model.addAttribute("task", taskDTO);
            model.addAttribute("skills", skillService.getAllSkillsDTO());
            return "createTask";
        }
        redirectAttributes.addFlashAttribute("taskCreated", true);
        redirectAttributes.addFlashAttribute("taskName", taskDTO.getName());
        return "redirect:/tasks/allTasks/"+projectId; // A new template to show the task creation success and available employees
    }

    @GetMapping("/editTask/{id}")
    public String editTask(@PathVariable Long id,
                           @RequestParam(name="locale", required = false) String localeParam,
                           Model model) {
        logger.info("Adding employee to task");
        Locale locale = Locale.getDefault();
        if (localeParam != null) {
            locale = Locale.forLanguageTag(localeParam);
        }
        LocaleContextHolder.setLocale(locale);
        String message = messageSource.getMessage("editTask.title", null, locale);
        model.addAttribute("message", message);
        TaskDTO taskDTO = taskServiceImpl.findByIdDTO(id);
        Long projectId = taskDTO.getProjectId();
        Project project = projectServiceImpl.findById(projectId);
        String projectName = project.getName();

        List<Long> skillsId = taskDTO.getSkillsIds();
        List<Skill> requiredSkills = skillService.findSkillsByIds(skillsId);
        List<ResourceDTO> resources = resourceService.findAllDTO();
        List<Long> eligibleEmployeesIds = taskDTO.getEmployeeIds();
        List<Long> resourcesIds = taskDTO.getResourcesIds();

        // Retrieve employees already assigned to the task
        List<Employee> assignedEmployees = taskDTO.getEmployeeIds().stream()
                .map(employeeService::getEmployeeById)
                .toList();

        model.addAttribute("projectName",projectName);
        model.addAttribute("task",taskDTO);
        model.addAttribute("assignedResources", resources.stream().
                filter(resource -> taskDTO.getResourcesIds().contains(resource.getId()))
                .collect(Collectors.toList()));
        model.addAttribute("resourcesIds",resourcesIds);
        model.addAttribute("resources",resources);
        model.addAttribute("eligibleEmployeesIds",eligibleEmployeesIds);
        model.addAttribute("assignedEmployees", assignedEmployees);
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
                             @RequestParam(value = "resources", required = false) List<Long> resourcesIds,
                             @Validated @ModelAttribute("task") TaskDTO taskDTO, BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {
        Long projectId = taskDTO.getProjectId();

        if(eligibleEmployeesIds == null) {
            taskDTO.setEmployeeIds(Collections.emptyList());
        }
        taskDTO.setEmployeeIds(eligibleEmployeesIds);
        if(resourcesIds == null) {
            taskDTO.setResourcesIds(Collections.emptyList());
        }
        taskDTO.setResourcesIds(resourcesIds);

        taskServiceImpl.editTask(taskDTO, bindingResult);

        redirectAttributes.addFlashAttribute("taskUpdated", true);
        redirectAttributes.addFlashAttribute("taskName", taskDTO.getName());
        return "redirect:/tasks/allTasks/"+ projectId;
    }
    @GetMapping("/deleteTask/{id}")
    public String deleteTask(@PathVariable Long id,
                             RedirectAttributes redirectAttributes) {
        logger.info("Deleting task with ID: {}", id);
        Task existingTask = taskServiceImpl.findById(id);
        Long projectId = existingTask.getProject().getId();
        taskServiceImpl.deleteById(id);
        logger.info("Task with ID {} deleted successfully", id);
        redirectAttributes.addFlashAttribute("taskDeleted", true);
        redirectAttributes.addFlashAttribute("taskId", existingTask.getId());
        redirectAttributes.addFlashAttribute("taskName", existingTask.getName());
        return "redirect:/tasks/allTasks/"+ projectId;
    }
    /**
     * A description of the entire Java function.
     *
     * @param  model	the model to hold task data
     * @return         	the view name to display all tasks
     */
    @GetMapping("/allTasks/{id}")
    @Secured("ROLE_ADMIN")
    public String showAllTasks(@PathVariable("id") Long id,
                               @RequestParam(name = "locale", required = false) String localeParam,
                               Model model) {
        logger.info("Retrieving all tasks");
        Locale locale = Locale.getDefault();
        if (localeParam != null) {
            locale=Locale.forLanguageTag(localeParam);
        }
        LocaleContextHolder.setLocale(locale);
        String message = messageSource.getMessage("allTasks.title", null, locale);
        model.addAttribute("message", message);
        Project project = projectServiceImpl.findById(id);
        Long projectId = project.getId();
        Long taskId=project.getTasks().stream().findFirst().get().getId();
        List<Task> tasks = project.getTasks();
        tasks.forEach(task -> {
            task.setDurationInput(format(task.getDuration()));
        });
        logger.info("Number of tasks retrieved: {}", tasks.size());
        model.addAttribute("tasks", tasks);
        model.addAttribute("projectId", projectId);
        model.addAttribute("taskId", taskId);
        model.addAttribute("statuses", Status.values());
        model.addAttribute("isAllTasksPage", true);
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
    @PostMapping("/updateStatus")
    public String updateStatus(@RequestParam("taskId") Long taskId,
                               @RequestParam("status") Status status,
                               RedirectAttributes redirectAttributes) {
        Task task = taskServiceImpl.findById(taskId);
        task.setStatus(status);
        taskServiceImpl.save(task);
        redirectAttributes.addFlashAttribute("statusUpdated", true);
        redirectAttributes.addFlashAttribute("status", status);
        redirectAttributes.addFlashAttribute("taskName", task.getName());
        return "redirect:/tasks/allTasks/" + task.getProject().getId();
    }

    @GetMapping("/showFiles/{taskId}")
    public String showFiles(@PathVariable("taskId") Long taskId,
                            @RequestParam(name = "locale", required = false) String localeParam,
                            Model model) {
        Locale locale = Locale.getDefault();
        if (localeParam != null) {
            locale = Locale.forLanguageTag(localeParam);
        }
        LocaleContextHolder.setLocale(locale);
        String message=messageSource.getMessage("message.TasksFiles", null, locale);
        model.addAttribute("message", message);
        Task task = taskServiceImpl.findById(taskId);
        model.addAttribute("task", task);
        model.addAttribute("fileNames", task.getFiles().stream().map(File::getFileName).toList());
        return "showTaskFiles";
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
                                            @RequestParam("file") MultipartFile [] files,
                                            Model model,
                                            RedirectAttributes redirectAttributes) throws IOException {
        Long projectId = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid task ID: " + taskId))
                .getProject()
                .getId();
        //Check file sizes
        for(MultipartFile file : files) {
            if(file.isEmpty()) {
                Task task = taskServiceImpl.findById(taskId);
                return handleFileError(model, task, "File is empty. Please enter a valid file");
            }
            byte[] fileContent = file.getBytes();
            if(fileContent.length > 5 * 1024 * 1024) { // Check file size in bytes
                Task task = taskServiceImpl.findById(taskId);
                return handleFileError(model, task, "File size exceeds maximum allowed size of 5MB");
            }
        }
        fileStorageService.uploadTaskFile(taskId, files);
        logger.info("File uploaded 0successfully");
        redirectAttributes.addFlashAttribute("fileUploaded", true);
        redirectAttributes.addFlashAttribute("taskName", taskRepository.findById(taskId).get().getName());
        redirectAttributes.addFlashAttribute("fileName", files[0].getOriginalFilename());
        return "redirect:/tasks/allTasks/" + projectId;
    }
    private String handleFileError(Model model, Task task, String errorMessage) {
        model.addAttribute("error", errorMessage);
        model.addAttribute("task", task);
        model.addAttribute("fileNames", task.getFiles().stream().map(File::getFileName).collect(Collectors.toList()));
        return "showTaskFiles";
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
            File file = task.getFiles().stream()
                    .filter(f -> f.getFileName().equals(fileName))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid file name: " + fileName));

            ByteArrayResource resource = new ByteArrayResource(file.getFileContent());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(resource);
    }

    @PostMapping("/deleteFile/{taskId}/{fileId}")
    public String deleteFile(@PathVariable Long taskId,
                             @PathVariable Long fileId,
                             RedirectAttributes redirectAttributes) {
            Task task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid task ID: " + taskId));
            Long projectId = taskRepository.findById(taskId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid task ID: " + taskId))
                    .getProject()
                    .getId();
            File file = fileRepository.findById(fileId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid file ID: " + fileId));
            if(!task.getFiles().contains(file)) {
                throw new IllegalArgumentException("Invalid file ID: " + fileId);
            }
        Optional<Employee> employeeOpt = task.getEmployees().stream()
                                        .filter(emp -> emp.getFiles().contains(file))
                                        .findFirst();
            task.getFiles().remove(file);
            taskRepository.save(task);
            if(employeeOpt.isPresent()) {
                Employee employee = employeeOpt.get();
                employee.getFiles().remove(file);
                employeeService.save(employee);

                file.getEmployees().remove(employee);
            }
           file.getTasks().remove(task);
        // Delete the file if it is not associated with any other projects or employees
        if (file.getTasks().isEmpty() && file.getEmployees().isEmpty()) {
            fileRepository.deleteById(fileId);
        } else {
            fileRepository.save(file);
        }
        redirectAttributes.addFlashAttribute("fileDeleted", true);
        redirectAttributes.addFlashAttribute("fileName", file.getFileName());
        redirectAttributes.addFlashAttribute("taskName", task.getName());
            // Redirect back to the task details page or wherever appropriate
        return "redirect:/tasks/allTasks/" + projectId; // Adjust the redirect URL as per your application's navigation
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
