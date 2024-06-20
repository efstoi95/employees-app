package com.enterprise.employees.service;

import com.enterprise.employees.model.Department;
import com.enterprise.employees.model.Employee;
import com.enterprise.employees.model.Skill;
import jakarta.mail.MessagingException;
import org.springframework.validation.BindingResult;

import java.io.IOException;
import java.util.List;

public interface EmployeeService {

    /**
     * Retrieves all employees from the employees repository.
     *
     * @return a list of all employees
     */
    List<Employee> getAllEmployees();
    /**
     * Retrieves all departments from the department repository.
     *
     * @return a list of all departments
     */
    List<Department>getAllDepartments();
    /**
     * Creates a new employee and saves it to the database. The employee's password is hashed before saving.
     * The employee is associated with a department, which is retrieved from the department repository.
     *
     * @param employee the employee to be created
     */
    void createEmployee(Employee employee, BindingResult bindingResult);
    /**
     * Deletes an employee with the given ID from the database.
     *
     * @param  id  the ID of the employee to be deleted
     */
    void deleteEmployee(Long id);
    /**
     * Updates an existing employee in the database with the provided details.
     *
     * @param  employee  the employee object containing the updated details
     */
    void editEmployee(Employee employee, BindingResult bindingResult);
    /**
     * Updates the information of an existing employee in the database.
     *
     * @param  employee  the employee object containing the updated details
     */
    void editInfoEmployee(Employee employee, BindingResult bindingResult);
    /**
     * Searches for employees by their name.
     *
     * @param  name  the name to search for (case-insensitive)
     * @return       a list of employees matching the given name, or all employees if the name is empty or null
     */
    List<Employee> searchEmployeeByName(String name);
    /**
     * Searches for employees by department.
     *
     * @param  department  the name of the department to search for
     * @return             a list of employees matching the department name, or all employees if the department is null or empty
     */
    List<Employee> searchEmployeeByDepartment(String department);
    /**
     * Retrieves an employee from the employees repository based on the provided ID.
     *
     * @param  id  the ID of the employee to retrieve
     * @return     the employee with the given ID, or throws an IllegalArgumentException if not found
     */
    Employee getEmployeeById(Long id);

    void updateEmployee(Employee employee,BindingResult bindingResult);

    List<Employee> findEmployeesByIds(List<Long> selectedEmployeesIds);

    List<Employee> findEmployeesWithUserRole();

    List<Skill> getAllSkills();
    void generateAndSendPassword(Employee employee) throws IOException, MessagingException;

    List<Employee> getEmployeesByProjectsAndSkills(Long projectId, List<Skill> skills);


    void save(Employee employee);
}
