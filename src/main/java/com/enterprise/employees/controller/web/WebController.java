package com.enterprise.employees.controller.web;

import com.enterprise.employees.model.*;
import com.enterprise.employees.service.*;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RequestMapping("/web")
@Controller
public class WebController {

    @Autowired
    EmailService emailService;

    private static final Logger logger = LoggerFactory.getLogger(WebController.class);
    private  final EmployeeService employeeService;
    private final ProjectService projectService;
    private final TaskService taskService;
    private final SkillService skillService;


    /**
     * Creates a new Employee object and adds it to the model for display in the "emp" view.
     *
     * @param model the model to hold the employee data
     * @return the view name to display the employee form
     */
    @GetMapping("/employees")
    public String createEmployee(Model model) {
        logger.info("Creating employee");
        model.addAttribute("skills", employeeService.getAllSkills());
        model.addAttribute("departments", employeeService.getAllDepartments());
        model.addAttribute("employee", new Employee());
        return "emp";
    }
    /**
     * Creates a new employee by validating the provided employee object and redirecting to the success page.
     *
     * @param  employee  the employee object to be created
     * @return           the string representing the redirect URL to the success page
     *
     */
    @PostMapping("/employee")
    public String createEmployee(@Validated @ModelAttribute("employee") Employee employee,
                                 BindingResult bindingResult, Model model) throws IOException, MessagingException {

        logger.info("Entering (POST)createEmployee method");
        logger.debug("Employee data: {}", employee);
        // Generate a random 4-digit number

        employeeService.createEmployee(employee, bindingResult);

        if (bindingResult.hasErrors()) {
            logger.warn("Validation errors encountered while creating employee: {}", bindingResult.getAllErrors());
            // If there are errors, return to the employee creation form
            model.addAttribute("skills", employeeService.getAllSkills());
            model.addAttribute("departments", employeeService.getAllDepartments());
            model.addAttribute("employee", employee);
            logger.info("Returning to employee creation form due to validation errors");
            return "emp"; // Return to the employee creation form view
        }
        String employeeEmail = employee.getEmail();
        String htmlTemplate = new String(Files.readAllBytes(Paths.get("src/main/resources/templates/verify.html")));
        // Replace placeholders with actual values
        String htmlBody = htmlTemplate.replace("[VerificationNumber]", employee.getVerifiedCode());
        // Send the email
        emailService.sendEmail(employeeEmail, "Your verification code", htmlBody);
        logger.info("Verification code sent successfully to employee: {}", employeeEmail);
        logger.info("Employee created successfully");
        return "redirect:/web/verify/" + employee.getId();
    }

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
        model.addAttribute("proj", new Project());
        return "createProject";
    }

    /**
     * Creates a new project and handles validation errors.
     *
     * @param  project         the Project object to create
     * @param  bindingResult   the result of the binding
     * @param  model           the model to hold project data
     * @return                 the view name after project creation
     */
    @PostMapping("/createdProject")
    public String createProject(@Validated @ModelAttribute("proj") Project project, BindingResult bindingResult, Model model) {
        logger.info("Entering (POST)createProject method");
        projectService.create(project, bindingResult);
        if (bindingResult.hasErrors()) {
            logger.warn("Validation errors encountered while creating project: {}", bindingResult.getAllErrors());
            model.addAttribute("proj", project);
            return "createProject";
        }
        logger.info("Project created successfully");
        return "redirect:/web/successCreateProject";
    }


    @GetMapping("/verify/{id}")
    public String showVerificationPage(@PathVariable("id") Long id, Model model) {
        logger.info("Entering (GET)showVerificationPage method");
        Employee employee = employeeService.getEmployeeById(id);
        model.addAttribute("employee", employee);
        return "addVerifyCode";
    }

    /**
     * Check if the entered verification code matches the stored one.
     *
     * @param  verifiedCode  the verification code entered by the user
     * @param  employee       the employee object for verification
     * @param  model          the model object for the view
     * @return               the view name after verification process
     */
    @PostMapping("/verified")
    public String verifyCode(@Validated @RequestParam("verifiedCode") String verifiedCode, @ModelAttribute("employee") Employee employee, Model model,BindingResult bindingResult) {
        logger.info("Entering (POST) verifyCode method with code: {}", verifiedCode);
        logger.info("Employee ID: {}", employee.getId());
        employeeService.updateEmployee(employee, bindingResult); // Assuming you have an update method in your service
        if (bindingResult.hasErrors()) {
            logger.warn("Incorrect verification code entered for employee ID: {}", employee.getId());
            return "addVerifyCode"; // Return to the verification page with error message
        }

        logger.info("Verification successful for employee ID: {}", employee.getId());
        // Redirect to success page
        return "redirect:/web/success";

    }

    @PostMapping("/sendCode/{id}")
    public String sendCode(@PathVariable("id") Long id, Model model) throws MessagingException, IOException {
        Employee employee = employeeService.getEmployeeById(id);
        employeeService.generateAndSendPassword(employee);
        model.addAttribute("employee", employee);
        model.addAttribute("message", "A new verification code has been sent to your email.");
        return "redirect:/web/verify/" + id;
    }

    @GetMapping("/lockedAccount/{id}")
    public String lockedAccount(@PathVariable("id") Long id, Model model) {
        model.addAttribute("employeeId", id);
        logger.info("Your account is locked.");
        return "lockedAccount";
    }

    /**
     * Retrieves the success message and adds it to the model.
     *
     * @param model the model to hold the success message
     * @return the view name to display the success message
     */
    @GetMapping("/success")
    public String success(Model model) {
        model.addAttribute("message", "The information of the user added");
        logger.info("The information of the employee added.");
        return "success";
    }

    /**
     * Retrieves the success message and adds it to the model for the admin user.
     *
     * @param  model  the model to hold the success message
     * @return        the view name to display the success message for the admin user
     */
    @Secured("ROLE_ADMIN")
    @GetMapping("/successLogin")
    public String successLogin(Model model) {
        model.addAttribute("message", "You logged in");
        logger.info("The employee logged in.");
        return "successLogin";
    }

    /**
     * Retrieves the success message and adds it to the model.
     *
     * @param  model  the model to hold the success message
     * @return        the view name to display the success message
     */
    @GetMapping("/successEdit")
    public String successEdit(Model model) {
        model.addAttribute("message", "The information of the user edited");
        logger.info("The information of the employee edited.");
        return "successEdit";
    }
    /**
     * Retrieves the success message for deleting an user's information and adds it to the model.
     *
     * @param  model  the model to hold the success message
     * @return        the view name to display the success message
     */
    @GetMapping("/successDelete")
    public String successDelete(Model model) {
        model.addAttribute("message", "The information of the user deleted");
        logger.info("The information of the employee deleted.");
        return "successDelete";
    }
    @GetMapping("/successCreateProject")
    public String successCreateProject(Model model) {
        model.addAttribute("message", "The information of the project created");
        logger.info("The information of the project created.");
        return "successCreateProject";
    }

    /**
     * A description of the entire Java function.
     *
     * @param  model  description of parameter
     * @return        description of return value
     */
    @GetMapping("/successEditProject")
    public String successEditProject(Model model) {
        model.addAttribute("message", "The information of the project edited");
        logger.info("The information of the project edited.");
        return "successEditProject";
    }

    @GetMapping("/successCreateTask/{id}")
    public String successCreateTask(@PathVariable Long id, Model model) {
        model.addAttribute("message", "The information of the task created");
        model.addAttribute("projectId", id);
        logger.info("The information of the task created.");
        return "successCreateTask";

    }
    @GetMapping("/successAddEmployeeToTask/{id}")
    public String successAddEmployeeToTask(@PathVariable Long id, Model model){
        model.addAttribute("message", "The information of the task changed");
        model.addAttribute("projectId", id);
        return "successAddEmployeeToTask";
    }
    @GetMapping("/successDeleteTask/{id}")
    public String successDeleteTask(@PathVariable Long id, Model model){
        model.addAttribute("message", "The information of the task deleted");
        model.addAttribute("projectId", id);
        return "successDeleteTask";
    }

    @GetMapping("/successDeleteProject")
    public String successDeleteProject(Model model){
        model.addAttribute("message", "The information of the project deleted");
        return "successDeleteProject";
    }

    /**
     * Retrieves all employees and adds them to the model for display.
     *
     * @param model the model to hold employee data
     * @return the view name to display all employees
     */
    @GetMapping("/allEmployees")
    @Secured("ROLE_ADMIN")
    public String showAllEmployees(Model model) {
        List<Employee> employees = employeeService.getAllEmployees();
        logger.info("Number of employees retrieved: {}", employees.size());
        model.addAttribute("employees", employees);
        return "allEmployees";
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
        List<Project> projects = (List<Project>) projectService.findAll();
        logger.info("Number of projects retrieved: {}", projects.size());
        model.addAttribute("projects", projects);
        return "allProjects";
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
        Project project = projectService.findById(id);
        List<Task> tasks = project.getTasks();
        logger.info("Number of tasks retrieved: {}", tasks.size());
        model.addAttribute("tasks", tasks);
        model.addAttribute("taskId", id);
        return "allTasks";
    }

    /**
     * Retrieves employee information by ID and adds it to the model.
     *
     * @param  id      the ID of the employee to retrieve
     * @param  model   the model to hold the employee data
     * @return         the view name to display the employee information
     */
    @GetMapping("/infoEmployee/{id}")
    @Secured("ROLE_USER")
    public String infoEmployee(@PathVariable("id") Long id,Model model) {
        logger.info("Entering infoEmployee method with ID: {}", id);
        Employee existingEmployee = employeeService.getEmployeeById(id);
        if(existingEmployee!= null){
            List<Department> departments = employeeService.getAllDepartments();
            model.addAttribute("skills",employeeService.getAllSkills());
            model.addAttribute("departments", departments);
            model.addAttribute("employee", existingEmployee);
            logger.info("Employee retrieved: {}", existingEmployee);
        }else{
            logger.warn("Employee with ID {} not found",id);
        }

        return "infoEmployee";
    }


    /**
     * Updates the information of an employee in the database and redirects to the success page.
     *
     * @param  employee  the employee object containing the updated information
     * @return           the redirect URL to the success page
     */
    @Secured("ROLE_USER")
    @PostMapping("/editedInfoEmployee")
    public String editedInfoEmployee(@Validated @ModelAttribute("employee") Employee employee, BindingResult bindingResult,Model model){
        logger.info("Editing employee information: {}", employee);
        employeeService.editInfoEmployee(employee, bindingResult);

        if(bindingResult.hasErrors()){
            logger.info("Validation error encountered while editing your information: {}", bindingResult.getAllErrors());
            model.addAttribute("employee", employee);
            logger.info("Returning to employee creation form due to validation errors");
            return "infoEmployee";
        }
        logger.info("Employee information edited successfully");
        return "redirect:/web/success";
    }


    /**
     * Deletes an employee with the given ID from the database and redirects to the
     * page displaying all employees.
     */
    @Secured("ROLE_ADMIN")
    @GetMapping("/admin/delete/{id}")
    public String deleteEmployee(@PathVariable("id") Long id) {
        logger.info("Deleting employee with ID: {}", id);
        employeeService.deleteEmployee(id);
        logger.info("Employee with ID {} deleted successfully", id);
        return "redirect:/web/successDelete";
    }
    /**
     * Retrieves an employee for editing based on the provided ID.
     *
     * @param id    the ID of the employee to edit
     * @param model the model to hold employee data
     * @return the view name to edit the employee
     */
    @Secured("ROLE_ADMIN")
    @GetMapping("/admin/edit/{id}")
    public String editEmployee(@PathVariable("id") Long id, Model model) {
        logger.info("Editing employee with ID: {}", id);
        Employee existingEmployee = employeeService.getEmployeeById(id);

        if(existingEmployee != null){
            model.addAttribute("skills",employeeService.getAllSkills());
            model.addAttribute("departments", employeeService.getAllDepartments());
            model.addAttribute("employee", existingEmployee);
            return "edit";
        }else{
            logger.warn("Employee with ID {} not found for editing", id);
            return "redirect:/web/allEmployees";
        }
    }

    /**
     * Edits an employee by validating the provided employee object and redirecting to the success edit page.
     *
     * @param  employee  the employee object to be edited
     * @return           the string representing the redirect URL to the success edit page
     */
    @Secured("ROLE_ADMIN")
    @PostMapping("/admin/editedEmployee")
    public String editEmployee(@Validated @ModelAttribute("employee") Employee employee, BindingResult bindingResult, Model model) {
        logger.info("Editing employee: {}", employee);
        employeeService.editEmployee(employee, bindingResult);
        if (bindingResult.hasErrors()) {
            logger.warn("Validation error encountered while editing employee: {}", bindingResult.getAllErrors());
            List<Department> allDepartments = employeeService.getAllDepartments();
            model.addAttribute("departments", employeeService.getAllDepartments());
            logger.info("Returning to the edit form");
            return "edit";
        }
        logger.info("Employee edited successfully");
        return "redirect:/web/successEdit";
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
        Project project = projectService.findById(id);
        model.addAttribute("proj", project);
        model.addAttribute("employees", employeeService.findEmployeesWithUserRole());
        return "editProject";
    }

    /**
     * Edits a project based on the selected employees and updates it in the system.
     *
     * @param  selectedEmployeesIds  the list of IDs of selected employees
     * @param  project               the project object to be edited
     * @param  bindingResult         the result of the binding process
     * @param  model                 the model to hold project and employee data
     * @return                       a string representing the redirect URL after project editing
     */
    @PostMapping("/editedProject")
    public String editedProject(@RequestParam(value = "selectedEmployees", required = false) List<Long> selectedEmployeesIds, @Validated @ModelAttribute("proj") Project project, BindingResult bindingResult, Model model) {
        if (selectedEmployeesIds != null) {
            // Retrieve selected employees based on their IDs and add them to the project
            List<Employee> selectedEmployees = employeeService.findEmployeesByIds(selectedEmployeesIds);
            project.setEmployees(selectedEmployees);
        }
        // Proceed with your project update logic
        projectService.update(project, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("proj", project);
            model.addAttribute("employees", employeeService.findEmployeesWithUserRole());
            return "editproject";
        }

        return "redirect:/web/successEditProject";
    }


    @GetMapping("/createTask/{id}")
    @Secured("ROLE_ADMIN")
    public String CreateTask(@PathVariable Long id,Model model) {
        logger.info("Creating new task");

        Task task = new Task();
        Project project = projectService.findById(id);
        task.setProject(project);
        model.addAttribute("task",task);
        model.addAttribute("skills", skillService.getAllSkills());
        return "createTask";
    }


   @PostMapping("/createdTask")
   public String createTask(@Validated @ModelAttribute("task") Task task, BindingResult bindingResult, Model model) {
       logger.info("Creating new task: {}", task);
       Long projectId = task.getProject().getId();
       Project project = projectService.findById(projectId);

       task.setProject(project);
       taskService.create(task , bindingResult);
       if(bindingResult.hasErrors()){
           model.addAttribute("task", task);
           model.addAttribute("skills", skillService.getAllSkills());
           return "createTask";
       }
       return "redirect:/web/successCreateTask/"+projectId; // A new template to show the task creation success and available employees
   }

   @GetMapping("/addEmployeeToTask/{id}")
   public String addEmployeeToTask(@PathVariable Long id,Model model) {
        logger.info("Adding employee to task");
        Task task = taskService.findById(id);
       if(task == null) {
           logger.error("Task with id {} not found",id);
           return "taskNotFound";

       }
        Project project = task.getProject();
        List<Skill> requiredSkills = task.getSkills();

        model.addAttribute("task",task);
        model.addAttribute(
                "employees",
                project.getEmployees().stream()
                .filter(e -> e.getSkills().stream().anyMatch(requiredSkills::contains))
                .collect(Collectors.toList())
        );
        return "addEmployeeToTask";
   }
   @PostMapping("/addedEmployeeToTask")
   public String addedEmployeeToTask(@RequestParam(value = "eligibleEmployees", required = false) List<Long> eligibleEmployeesIds, @Validated @ModelAttribute("task") Task task, BindingResult bindingResult, Model model) {
        Task existingTask = taskService.findById(task.getId());
        Long projectId = existingTask.getProject().getId();
       if(eligibleEmployeesIds != null) {
           List<Employee> eligibleEmployees = employeeService.findEmployeesByIds(eligibleEmployeesIds);
           task.setEmployees(eligibleEmployees);
       }
       taskService.addEmployeeToTask(task, bindingResult);
       return "redirect:/web/successAddEmployeeToTask/"+ projectId;
   }

   @GetMapping("/deleteTask/{id}")
   public String deleteTask(@PathVariable Long id,Model model) {
        logger.info("Deleting task with ID: {}", id);
        Task existingTask = taskService.findById(id);
        Long projectId = existingTask.getProject().getId();
        taskService.deleteById(id);
        logger.info("Task with ID {} deleted successfully", id);
        return "redirect:/web/successDeleteTask/"+ projectId;
   }

   @GetMapping("/deleteProject/{id}")
   public String deleteProject(@PathVariable Long id,Model model) {
        logger.info("Deleting project with ID: {}", id);
        projectService.deleteById(id);
        logger.info("Project with ID {} deleted successfully", id);
        return "redirect:/web/successDeleteProject";
   }



    /**
     * Searches for employees by name and adds the results to the model.
     *
     * @param  name   the name to search for (optional)
     * @param  model  the model to add the search results to
     * @return        the name of the view to display the search results
     */
    @Secured("ROLE_ADMIN")
    @GetMapping("/search/name")
    public String searchEmployee(@RequestParam(name = "name", required = false) String name,
                                 Model model) {
        logger.info("Searching for employee with name: {}", name);
        List<Employee> employees= employeeService.searchEmployeeByName(name);
        logger.info("Number of employees found: {}", employees.size());
        model.addAttribute("employees", employees);
        return "allEmployees";
    }

    /**
     * Searches for employees by department and adds the results to the model.
     *
     * @param  department   the department to search for (optional)
     * @param  model        the model to add the search results to
     * @return              the name of the view to display the search results
     */
    @Secured("ROLE_ADMIN")
    @GetMapping("/search/department")
    public String searchEmployeeByDepartment(@RequestParam(name = "department", required = false) String department,
                                             Model model) {
        logger.info("Searching for employees in department: {}", department);
        List<Employee> employees= employeeService.searchEmployeeByDepartment(department);
        logger.info("Number of employees found in department {}: {}",department, employees.size());
        model.addAttribute("employees", employees);
        return "allEmployees";

    }


}



