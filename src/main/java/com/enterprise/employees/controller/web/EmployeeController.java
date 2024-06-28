package com.enterprise.employees.controller.web;

import com.enterprise.employees.model.Department;
import com.enterprise.employees.model.Employee;
import com.enterprise.employees.model.EmployeeDTO;
import com.enterprise.employees.service.EmailService;
import com.enterprise.employees.service.EmployeeService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.exceptions.TemplateProcessingException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/web")
@Controller
public class EmployeeController {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);
    private final EmployeeService employeeService;
    private final EmailService emailService;
    private final ModelMapper modelMapper;

    /**
     * Creates a new Employee object and adds it to the model for display in the "emp" view.
     *
     * @param model the model to hold the employee data
     * @return the view name to display the employee form
     */
    @GetMapping("/employees")
    public String createEmployee(Model model) {
            logger.info("Creating employee");
            model.addAttribute("employee", new EmployeeDTO());
            model.addAttribute("skills", employeeService.getAllSkills());
            model.addAttribute("departments", employeeService.getAllDepartments());
            return "createEmployee";

    }
    /**
     * Creates a new Employee object and adds it to the model for display in the "emp" view.
     *
     * @param  employeeCreateDTO  the DTO containing employee creation/edit information
     * @param  bindingResult      the result of the binding process
     * @param  model              the model to hold employee data
     * @return                    the view name to display the employee form
     */
    @PostMapping("/employee")
    public String createEmployee(@Validated @ModelAttribute("employee") EmployeeDTO employeeCreateDTO,
                                 BindingResult bindingResult, Model model) throws MessagingException, IOException {

        logger.info("Entering (POST)createEmployee method");
        logger.debug("Employee data: {}", employeeCreateDTO);

        // Generate a random 4-digit number
        Employee employee = employeeService.createEmployee(employeeCreateDTO, bindingResult);

        if (bindingResult.hasErrors()) {
            logger.warn("Validation errors encountered while creating employee: {}", bindingResult.getAllErrors());
            // If there are errors, return to the employee creation form
            model.addAttribute("skills", employeeService.getAllSkills());
            model.addAttribute("departments", employeeService.getAllDepartments());
            model.addAttribute("employee", employeeCreateDTO);
            logger.info("Returning to employee creation form due to validation errors");
            return "createEmployee"; // Return to the employee creation form view
        }

        String employeeEmail = employee.getEmail();
        if (employeeEmail == null || employeeEmail.isEmpty()) {
            logger.error("Employee email is null or empty.");
            model.addAttribute("error", "Employee email is not provided. Please ensure the email is entered correctly.");
            return "error"; // Redirect to the error page
        }
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
     * Retrieves all employees and adds them to the model for display.
     *
     * @param model the model to hold employee data
     * @return the view name to display all employees
     */
    @GetMapping("/allEmployees")
    @Secured("ROLE_ADMIN")
    public String showAllEmployees(Model model) {
            List<Employee> employees = employeeService.getAllEmployees();
            model.addAttribute("employees", employees);
            return "allEmployees";
    }

    /**
     * Deletes an employee with the given ID from the database and redirects to the
     * page displaying all employees.
     */
    @Secured("ROLE_ADMIN")
    @GetMapping("/admin/delete/{id}")
    public String deleteEmployee(@PathVariable("id") Long id,Model model) {
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
            EmployeeDTO employeeEditDTO = modelMapper.map(existingEmployee, EmployeeDTO.class);
            model.addAttribute("skills", employeeService.getAllSkills());
            model.addAttribute("departments", employeeService.getAllDepartments());
            model.addAttribute("employee", employeeEditDTO);
            return "edit";
        }else{
            logger.warn("Employee with ID {} not found for editing", id);
            return "redirect:/web/allEmployees";
        }
    }

    /**
     * Edits an employee by validating the provided employee object and redirecting to the success edit page.
     *
     * @param  employeeEditDTO  the employee object to be edited
     * @return           the string representing the redirect URL to the success edit page
     */
    @Secured("ROLE_ADMIN")
    @PostMapping("/admin/editedEmployee")
    public String editEmployee(@Validated @ModelAttribute("employee") EmployeeDTO employeeEditDTO, BindingResult bindingResult, Model model) {
        logger.info("Editing employee: {}", employeeEditDTO);
        employeeService.editEmployee(employeeEditDTO, bindingResult);
        if (bindingResult.hasErrors()) {
            logger.warn("Validation error encountered while editing employee: {}", bindingResult.getAllErrors());
            model.addAttribute("departments", employeeService.getAllDepartments());
            logger.info("Returning to the edit form");
            return "edit";
        }
        logger.info("Employee edited successfully");
        return "redirect:/web/successEdit";
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
     * Retrieves the success message and adds it to the model.
     *
     * @param  model  the model to hold the success message
     * @return        the view name to display the success message
     */
    @GetMapping("/successEdit")
    public String successEdit(Model model) {
        model.addAttribute("message", "The information of the user edited");
        logger.info("The information of the employee edited.");
        return "successEmployee";
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
        return "successEmployee";
    }


    @GetMapping("/error")
    public String error(Model model) {
        model.addAttribute("error", "The information of the user deleted");
        logger.info("The information of the employee deleted.");
        return "error";
    }
}

