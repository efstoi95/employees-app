package com.enterprise.employees.repository;

import com.enterprise.employees.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeesRepository extends JpaRepository<Employee, Long> {
    /**
     * Finds all employees whose full name contains the given name, ignoring case.
     *
     * @param  name  the name to search for (case-insensitive)
     * @return       a list of employees matching the search criteria
     */
    List<Employee> findByFullNameContainingIgnoreCase(String name);

    /**
     * Finds all employees whose department name contains the given department name, ignoring case.
     *
     * @param  department  the department name to search for (case-insensitive)
     * @return             a list of employees matching the search criteria
     */
    List<Employee> findByDepartmentNameContainingIgnoreCase(String department);

    /**
     * Finds an employee by their username.
     *
     * @param  userName  the username of the employee to find
     * @return           the employee with the given username, or null if not found
     */
    Employee findByUsername(String userName);
    /**
     * Checks if an employee with the given username exists in the repository.
     *
     * @param  username  the username of the employee to check
     * @return           true if an employee with the given username exists, false otherwise
     */
    boolean existsByUsername(String username);
    /**
     * Checks if an employee with the given email exists in the repository.
     *
     * @param  email  the email of the employee to check
     * @return        true if an employee with the given email exists, false otherwise
     */
    boolean existsByEmail(String email);
}
