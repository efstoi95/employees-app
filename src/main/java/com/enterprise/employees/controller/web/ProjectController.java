package com.enterprise.employees.controller.web;

import com.enterprise.employees.model.*;
import com.enterprise.employees.repository.FileRepository;
import com.enterprise.employees.repository.ProjectRepository;
import com.enterprise.employees.service.CustomerServiceImpl;
import com.enterprise.employees.service.EmployeeService;
import com.enterprise.employees.service.FileStorageService;
import com.enterprise.employees.service.ProjectServiceImpl;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
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
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RequestMapping("/projects")
@Controller
public class ProjectController {

    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private MessageSource messageSource;
    private final FileStorageService fileStorageService;
    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);
    private final ProjectServiceImpl projectServiceImpl;
    private final EmployeeService employeeService;
    private final CustomerServiceImpl customerService;
    private final ModelMapper modelMapper;

    /**
     * Creates a new project and handles validation errors.
     *
     * @param  model   the model to hold project data
     * @return         the view name for the project creation form
     */
    @Secured("ROLE_ADMIN")
    @GetMapping("/createProject")
    public String createProject(@RequestParam(name = "locale", required = false) String localeParam,
                                Model model) {
        logger.info("Creating project");
        Locale locale = Locale.getDefault();
        if (localeParam != null) {
            locale = Locale.forLanguageTag(localeParam);
        }
        LocaleContextHolder.setLocale(locale);
        String message = messageSource.getMessage("createProject.title", null, locale);
        model.addAttribute("message", message);
        model.addAttribute("proj", new ProjectDTO());
        model.addAttribute("isCreateProjectPage", true);
        return "createProject";

    }

    /**
     * Creates a new project and handles validation errors.
     *
     * @param  projectDTO         the Project object to create
     * @param  bindingResult   the result of the binding
     * @param  model           the model to hold project data
     * @return                 the view name after project creation
     */
    @PostMapping("/createdProject")
    public String createProject(@Validated @ModelAttribute("proj") ProjectDTO projectDTO,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        logger.info("Entering (POST)createProject method");
        projectServiceImpl.create(projectDTO, bindingResult);
        if (bindingResult.hasErrors()) {
            logger.warn("Validation errors encountered while creating project: {}", bindingResult.getAllErrors());
            model.addAttribute("proj", projectDTO);
            return "createProject";
        }
        logger.info("Project created successfully");
        redirectAttributes.addFlashAttribute("projectCreated", true);
        redirectAttributes.addFlashAttribute("projectName", projectDTO.getName());
        return "redirect:/projects/allProjects";

    }
    /**
     * Retrieves all projects and adds them to the model for display.
     *
     * @param  model  the model to hold project data
     * @return        the view name to display all projects
     */
    @GetMapping("/allProjects")
    @Secured("ROLE_ADMIN")
    public String showAllProjects(@RequestParam(name = "locale", required = false) String localeParam,
                                  Model model) {
        Locale locale = Locale.getDefault();
        if (localeParam != null) {
            locale = Locale.forLanguageTag(localeParam);
        }
        LocaleContextHolder.setLocale(locale);
        String message = messageSource.getMessage("allProjects.title", null, locale);
        model.addAttribute("message", message);


        logger.info("Retrieving all projects");
        List<ProjectDTO> projects = projectServiceImpl.findAllProjects();
        logger.info("Number of projects retrieved: {}", projects.size());
        model.addAttribute("isAllProjectsPage", true);
        model.addAttribute("projects", projects);
        return "allProjects";
    }

    /**
     * Retrieves a project for editing based on the provided ID and adds it to the model.
     * Also adds a list of employees with the user role to the model.
     *
     * @param  id         the ID of the project to edit
     * @param  model      the model to hold project and employee data
     * @return            the view name to edit the project
     */
    @Secured("ROLE_ADMIN")
    @GetMapping("/editProject/{id}")
    public String editProject(@PathVariable("id") Long id,
                              @RequestParam(name = "locale", required = false) String localeParam,
                              Model model) {
        logger.info("Editing project with ID: {}", id);
        Locale locale = Locale.getDefault();
        if (localeParam != null) {
            locale = Locale.forLanguageTag(localeParam);
        }
        LocaleContextHolder.setLocale(locale);
        String message = messageSource.getMessage("editProject.title", null, locale);
        model.addAttribute("message", message);
        Project project = projectServiceImpl.findById(id);
        ProjectDTO projectDTO = modelMapper.map(project, ProjectDTO.class);
        List<EmployeeDTO> employees = employeeService.findEmployeesWithUserRoleDTO();
        model.addAttribute("proj", projectDTO);
        model.addAttribute("employees", employees);
        model.addAttribute("customers", customerService.findallDTO());
        return "editProject";
    }

    /**
     * Edits a project based on the selected employees and updates it in the system.
     *
     * @param  selectedEmployeesIds  the list of IDs of selected employees
     * @param  projectDTO               the projectDTO object to be edited
     * @param  bindingResult         the result of the binding process
     * @param  model                 the model to hold project and employee data
     * @return                       a string representing the redirect URL after project editing
     */
    @PostMapping("/editedProject")
    public String editedProject(@RequestParam(value = "employeeIds", required = false) List<Long> selectedEmployeesIds,
                                @Validated @ModelAttribute("proj") ProjectDTO projectDTO,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        logger.info("Editing project: {}", projectDTO);
        projectDTO.setEmployeeIds(selectedEmployeesIds);
        // Proceed with your project update logic
        projectServiceImpl.update(projectDTO, bindingResult);
        if (bindingResult.hasErrors()) {
            List<EmployeeDTO> employees = employeeService.findEmployeesWithUserRoleDTO();
            model.addAttribute("proj", projectDTO);
            model.addAttribute("employees", employees);
            model.addAttribute("customers", customerService.findallDTO());
            return "editProject";
        }

        redirectAttributes.addFlashAttribute("projectUpdated", true);
        redirectAttributes.addFlashAttribute("projectId", projectDTO.getId());
        redirectAttributes.addFlashAttribute("projectName", projectDTO.getName());
        return "redirect:/projects/allProjects";
    }
    /**
     * Deletes a project based on the provided ID and redirects to a success page.
     *
     * @param  id    the ID of the project to delete
     * @param  model the model to hold project data
     * @return       a string representing the redirect URL after project deletion
     */
    @GetMapping("/deleteProject/{id}")
    public String deleteProject(@PathVariable Long id,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        logger.info("Deleting project with ID: {}", id);
        String projectName = projectServiceImpl.findById(id).getName();
        projectServiceImpl.deleteById(id);
        logger.info("Project with ID {} deleted successfully", id);
        redirectAttributes.addFlashAttribute("projectDeleted", true);
        redirectAttributes.addFlashAttribute("projectId", id);
        redirectAttributes.addFlashAttribute("projectName", projectName);
        return "redirect:/projects/allProjects";
    }


    /**
     * Uploads a file.
     *
     * @param  projectId	description of parameter
     * @param  files	    description of parameter
     * @return         	description of return value
     */
    @PostMapping("/upload/{projectId}")
    public String uploadProjectDescriptionFile(@PathVariable Long projectId,
                                               @RequestParam("file") MultipartFile[] files,
                                               Model model,
                                               RedirectAttributes redirectAttributes) throws IOException {

        //Check the size
        for(MultipartFile file : files) {
            if (file.isEmpty()) {
                Project project = projectServiceImpl.findById(projectId);
                return handleFileError(model, project, "File is empty. Please enter a valid file");
            }
            byte[] fileContent = file.getBytes();
            if (fileContent.length > 5 * 1024 * 1024) {
                Project project = projectServiceImpl.findById(projectId);
                return handleFileError(model, project, "File size exceeds maximum allowed size of 5MB");
            }
        }
        logger.info("Uploading file to project with ID: {}", projectId);
        fileStorageService.uploadProjectFile(projectId, files);
        redirectAttributes.addFlashAttribute("fileUploaded", true);
        redirectAttributes.addFlashAttribute("projectId", projectId);
        redirectAttributes.addFlashAttribute("projectName", projectServiceImpl.findById(projectId).getName());
        redirectAttributes.addFlashAttribute("fileName", files[0].getOriginalFilename());
        return "redirect:/projects/allProjects";
    }

    private String handleFileError(Model model, Project project, String errorMessage) {
        model.addAttribute("error", errorMessage);
        model.addAttribute("project", project);
        model.addAttribute("fileNames", project.getFiles().stream().map(File::getFileName).collect(Collectors.toList()));
        return "showProjectFiles";
    }
    /**
     * Downloads a file.
     * @param  projectId	description of parameter
     * @param  fileName	    description of parameter
     * @return         	description of return value
     */
    @GetMapping("/downloadFile/{projectId}/{fileName}")
    public ResponseEntity<ByteArrayResource> downloadTaskDescriptionFile(@PathVariable Long projectId, @PathVariable String fileName,Model model) {
            logger.info("Downloading file from project with ID: {}", projectId);
            Project project = projectRepository.findById(projectId).orElseThrow(() -> new IllegalArgumentException("Invalid task ID: " + projectId));
            File file = project.getFiles().stream()
                    .filter(f -> f.getFileName().equals(fileName))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid file name: " + fileName));
            byte[] fileContent = file.getFileContent();
            ByteArrayResource resource = new ByteArrayResource(fileContent);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(resource);

    }
    @PostMapping("/deleteFile/{projectId}/{fileId}")
    public String deleteFile(@PathVariable Long projectId,
                             @PathVariable Long fileId,
                             RedirectAttributes redirectAttributes) {
        logger.info("Deleting file from project with ID: {}", projectId);

        // Retrieve the project by projectId
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid project ID: " + projectId));

        // Retrieve the file by fileId
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid file ID: " + fileId));
        String fileName = file.getFileName();

        // Check if the project contains the file
        if (!project.getFiles().contains(file)) {
            throw new IllegalArgumentException("Invalid file ID: " + fileId);
        }

        // Find the employee who has the file
        Optional<Employee> employeeOpt = project.getEmployees().stream()
                .filter(emp -> emp.getFiles().contains(file))
                .findFirst();

        // Remove the file from the project
        project.getFiles().remove(file);
        projectRepository.save(project);

        // If an employee was found, remove the file from the employee and save
        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            employee.getFiles().remove(file);
            employeeService.save(employee);

            // Remove the employee association from the file
            file.getEmployees().remove(employee);
        }
        // Remove the project association from the file
        file.getProjects().remove(project);

        // Delete the file if it is not associated with any other projects or employees
        if (file.getProjects().isEmpty() && file.getEmployees().isEmpty()) {
            fileRepository.deleteById(fileId);
        } else {
            fileRepository.save(file);
        }

        redirectAttributes.addFlashAttribute("fileDeleted", true);
        redirectAttributes.addFlashAttribute("projectId", projectId);
        redirectAttributes.addFlashAttribute("projectName", project.getName());
        redirectAttributes.addFlashAttribute("fileName", fileName);
        // Redirect to a success page or wherever appropriate
        return "redirect:/projects/allProjects"; // Adjust the redirect URL as per your application's navigation
    }



    @GetMapping("/showFiles/{projectId}")
    public String showFiles(@PathVariable("projectId") Long projectId,
                            @RequestParam(name = "locale", required = false) String localeParam,
                            Model model) {
        Locale locale = Locale.getDefault();
        if (localeParam != null) {
            locale = Locale.forLanguageTag(localeParam);
        }
        LocaleContextHolder.setLocale(locale);
        String message=messageSource.getMessage("message.ProjectFiles", null, locale);
        model.addAttribute("message", message);
        Project project = projectServiceImpl.findById(projectId);
        model.addAttribute("project", project);
        model.addAttribute("fileNames", project.getFiles().stream().map(File::getFileName).collect(Collectors.toList()));
        return "showProjectFiles";
    }

    @GetMapping("/viewFile/{projectId}/{fileName}")
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
