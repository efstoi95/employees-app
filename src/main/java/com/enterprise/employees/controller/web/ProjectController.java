package com.enterprise.employees.controller.web;

import com.enterprise.employees.model.EmployeeDTO;
import com.enterprise.employees.model.Project;
import com.enterprise.employees.model.ProjectDTO;
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
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/projects")
@Controller
public class ProjectController {

    @Autowired
    private ProjectRepository projectRepository;
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
    public String createProject(Model model) {
        logger.info("Creating project");
        model.addAttribute("proj", new ProjectDTO());
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
    public String createProject(@Validated @ModelAttribute("proj") ProjectDTO projectDTO, BindingResult bindingResult, Model model) {
        logger.info("Entering (POST)createProject method");
        projectServiceImpl.create(projectDTO, bindingResult);
        if (bindingResult.hasErrors()) {
            logger.warn("Validation errors encountered while creating project: {}", bindingResult.getAllErrors());
            model.addAttribute("proj", projectDTO);
            return "createProject";
        }
        logger.info("Project created successfully");
        return "redirect:/projects/successCreateProject";

    }
    /**
     * Retrieves all projects and adds them to the model for display.
     *
     * @param  model  the model to hold project data
     * @return        the view name to display all projects
     */
    @GetMapping("/allProjects")
    @Secured("ROLE_ADMIN")
    public String showAllProjects(Model model) {
            logger.info("Retrieving all projects");
            List<ProjectDTO> projects = projectServiceImpl.findAllProjects();
            logger.info("Number of projects retrieved: {}", projects.size());
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
    public String editProject(@PathVariable("id") Long id, Model model) {
        logger.info("Editing project with ID: {}", id);
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
                                @Validated @ModelAttribute("proj") ProjectDTO projectDTO, BindingResult bindingResult, Model model) {
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

        return "redirect:/projects/successEditProject";
    }
    /**
     * Deletes a project based on the provided ID and redirects to a success page.
     *
     * @param  id    the ID of the project to delete
     * @param  model the model to hold project data
     * @return       a string representing the redirect URL after project deletion
     */
    @GetMapping("/deleteProject/{id}")
    public String deleteProject(@PathVariable Long id,Model model) {
        logger.info("Deleting project with ID: {}", id);
        projectServiceImpl.deleteById(id);
        logger.info("Project with ID {} deleted successfully", id);
        return "redirect:/projects/successDeleteProject";
    }
    /**
     * Uploads a file.
     *
     * @param  projectId	description of parameter
     * @param  files	    description of parameter
     * @return         	description of return value
     */
    @PostMapping("/upload/{projectId}")
    public String uploadProjectDescriptionFile(@PathVariable Long projectId, @RequestParam("file") MultipartFile[] files,Model model) throws IOException {
            logger.info("Uploading file to project with ID: {}", projectId);
            fileStorageService.uploadProjectFile(projectId, files);
            return "redirect:/projects/successUploadToProject";
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

            byte[] fileContent = project.getFileContentByFileName(fileName);
            if(fileContent == null) {
                throw new IllegalArgumentException("Invalid file name: " + fileName);
            }
            ByteArrayResource resource = new ByteArrayResource(fileContent);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(resource);

    }
    /**
     * Deletes a file.
     *
     * @param  projectId	description of parameter
     * @param  index	    description of parameter
     * @return         	description of return value
     */
    @PostMapping("/deleteFile/{projectId}/{index}")
    public String deleteFile(@PathVariable Long projectId, @PathVariable int index, Model model) {
            logger.info("Deleting file from project with ID: {}", projectId);
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

    @GetMapping("/successCreateProject")
    public String successCreateProject(Model model) {
        model.addAttribute("message", "The information of the project created");
        logger.info("The information of the project created.");
        return "successProject";
    }

    @GetMapping("/successEditProject")
    public String successEditProject(Model model) {
        model.addAttribute("message", "The information of the project edited");
        logger.info("The information of the project edited.");
        return "successProject";
    }

    @GetMapping("/successDeleteProject")
    public String successDeleteProject(Model model){
        model.addAttribute("message", "The information of the project deleted");
        return "successProject";
    }

    @GetMapping("/successDeleteProjectFile")
    public String successDeleteFile( Model model) {
        model.addAttribute("message", "File deleted successfully");
        return "successProject";
    }

    @GetMapping("/successUploadToProject")
    public String successUploadToProject(Model model) {
        model.addAttribute("message", "File uploaded successfully");
        return "successProject";
    }
}
