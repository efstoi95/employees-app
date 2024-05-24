package com.enterprise.employees.controller.rest;

import com.enterprise.employees.model.Department;
import com.enterprise.employees.model.Employee;
import com.enterprise.employees.repository.DepartmentRepository;
import com.enterprise.employees.repository.EmployeesRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class HomeController {

    DepartmentRepository departmentRepository;
    EmployeesRepository employeesRepository;

    public HomeController(DepartmentRepository departmentRepository, EmployeesRepository employeesRepository) {
        this.departmentRepository = departmentRepository;
        this.employeesRepository = employeesRepository;
    }

    /**
     * Retrieves a list of employees from the server.
     *
     * @return         	A ResponseEntity containing the employee data
     */
    @GetMapping("/employees")
    public ResponseEntity<Employee> getEmployees()  {
        Department department = new Department();
        department.setId(1L);
        department.setName("IT");

        departmentRepository.save(department);

        Employee employee = new Employee();
        employee.setId(1L);
        employee.setFullName("John");
        employee.setEmail("a@a.com");
        employee.setDepartment(department);

        employeesRepository.save(employee);

        return ResponseEntity.ok(employee);
    }


}


