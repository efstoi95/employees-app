package com.enterprise.employees.controller.web;
import com.enterprise.employees.model.Department;
import com.enterprise.employees.model.Employee;
import com.enterprise.employees.service.EmployeeService;
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

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/web")
@Controller
public class WebController {

    private static final Logger logger = LoggerFactory.getLogger(WebController.class);
    private  final EmployeeService employeeService;


    /**
     * Creates a new Employee object and adds it to the model for display in the "emp" view.
     *
     * @param model the model to hold the employee data
     * @return the view name to display the employee form
     */
    @GetMapping("/employees")
    public String createEmployee(Model model) {
        logger.info("Creating employee");
        List<Department> allDepartments = employeeService.getAllDepartments();
        model.addAttribute("departments", allDepartments);
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
                                 BindingResult bindingResult, Model model) {

        logger.info("Entering (POST)createEmployee method");
        logger.debug("Employee data: {}", employee);
        employeeService.createEmployee(employee, bindingResult);

        if (bindingResult.hasErrors()) {
            logger.warn("Validation errors encountered while creating employee: {}", bindingResult.getAllErrors());
            // If there are errors, return to the employee creation form
            List<Department> allDepartments = employeeService.getAllDepartments();
            model.addAttribute("departments", allDepartments);
            model.addAttribute("employee", employee);
            logger.info("Returning to employee creation form due to validation errors");
            return "emp"; // Return to the employee creation form view
        }


        logger.info("Employee created successfully");
        return "redirect:/web/success";
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
        return "successEdit";
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
    public String editedInfoEmployee( Employee employee, BindingResult bindingResult,Model model){
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
        return "redirect:/web/allEmployees";
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
            List<Department> department = employeeService.getAllDepartments();
            model.addAttribute("departments", department);
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



