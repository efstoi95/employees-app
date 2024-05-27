package com.enterprise.employees.service;

import com.enterprise.employees.exception.FieldTooLongException;
import com.enterprise.employees.model.Department;
import com.enterprise.employees.model.Employee;
import com.enterprise.employees.repository.DepartmentRepository;
import com.enterprise.employees.repository.EmployeesRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    EmployeesRepository employeesRepository;
    DepartmentRepository departmentRepository;
    PasswordEncoder passwordEncoder;

    public EmployeeServiceImpl(EmployeesRepository employeesRepository, DepartmentRepository departmentRepository, PasswordEncoder passwordEncoder) {
        this.employeesRepository = employeesRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<Employee> getAllEmployees() {
        return employeesRepository.findAll();
    }


    @Override
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    @Override
    public void createEmployee(Employee employee, BindingResult bindingResult) {

        int MAX_LENGTH = 30;

        // Validate the length of the input fields
        if (employee.getFullName().length() > MAX_LENGTH) {
            throw new FieldTooLongException("Full name is too long.");
        }
        if (employee.getUsername().length() > MAX_LENGTH) {
            throw new FieldTooLongException("Username is too long.");
        }
        if (employee.getEmail().length() > MAX_LENGTH) {
            throw new FieldTooLongException("Email is too long.");
        }

//
        String hashedPassword = passwordEncoder.encode(employee.getPassword());
        employee.setPassword(hashedPassword);
        if(employeesRepository.existsByUsername(employee.getUsername())) {
            bindingResult.rejectValue("username", "error.employee", "Username already exists");
        }
        if(employeesRepository.existsByEmail(employee.getEmail())) {
            bindingResult.rejectValue("email", "error.employee", "Email already exists");
        }

        if(bindingResult.hasErrors()) {
            return ;
        }

        employeesRepository.save(employee);
        Department department = departmentRepository.findById(employee.getDepartment().getId()).orElse(null);
        employee.setDepartment(department);
        assert department != null;
        department.getEmployees().add(employee);
        departmentRepository.save(department);

    }

    @Override
    public void deleteEmployee(Long id) {
        Employee employee = employeesRepository.findById(id).orElse(null);
        if (employee != null) {
            // Clear all references to the employee
            Department department = employee.getDepartment();
            if (department != null) {
                // Check if the employee is already associated with this department
                if (department.getEmployees().stream().anyMatch(e -> e.getId().equals(employee.getId()))) {
                    // Remove the existing relationship from the old department
                    employee.setDepartment(null);
                    //employeesRepository.save(employee);
                    department.setEmployees(department.getEmployees()
                            .stream()
                            .filter(e -> !e.getId().equals(employee.getId()))
                            .collect(Collectors.toList()));
                    departmentRepository.save(department);
                }
            }
            // Now delete the employee
            employeesRepository.deleteById(id);
        }
    }





    @Override
    public void editEmployee(Employee employee, BindingResult bindingResult) {
        // Fetch the existing employee by ID
        Employee existingEmployee = employeesRepository.findById(employee.getId()).orElse(null);

        if (existingEmployee != null) {
            // Update the existing employee details
            String encodedPassword = passwordEncoder.encode(employee.getPassword());
            employee.setPassword(encodedPassword);
            if(employeesRepository.existsByUsername(employee.getUsername())) {
                bindingResult.rejectValue("username", "error.employee", "Username already exists");
            }
            if(employeesRepository.existsByEmail(employee.getEmail())) {
                bindingResult.rejectValue("email", "error.employee", "Email already exists");
            }
            if(bindingResult.hasErrors()) {
                return;
            }

            // Fetch the existing department by ID
            Department newDepartment = departmentRepository.findById(employee.getDepartment().getId()).orElse(null);
            if (newDepartment != null) {
                Department oldDepartment = departmentRepository.findById(existingEmployee.getDepartment().getId()).orElse(null);
                if (oldDepartment != null) {
                    // Check if the employee is already associated with this department
                    if (oldDepartment.getEmployees().stream().anyMatch(e -> e.getId().equals(employee.getId()))) {
                        // Remove the existing relationship from the old department
                        employee.setDepartment(null);
                        // TODO: Check if line needed
                        employeesRepository.save(employee);
                        oldDepartment.setEmployees(oldDepartment.getEmployees()
                                .stream()
                                .filter(e -> !e.getId().equals(employee.getId()))
                                .collect(Collectors.toList()));
                        departmentRepository.save(oldDepartment);
                    }
                }
                employee.setDepartment(newDepartment);
                // Add the employee to the new department
                newDepartment.getEmployees().add(employee);
                departmentRepository.save(newDepartment);
            }
            employeesRepository.save(employee);
        }
    }

    @Override
    public void editInfoEmployee(Employee employee, BindingResult bindingResult) {
        employeesRepository.findById(employee.getId()).ifPresent(existingEmployee -> {
            // Update the existing employee details
            Optional.ofNullable(employee.getPassword())
                    .filter(password -> !password.isEmpty())
                    .ifPresent(password -> {
                        String encodedPassword = passwordEncoder.encode(password);
                        existingEmployee.setPassword(encodedPassword);
                    });

            // Update the username if it has been changed
            if(employeesRepository.existsByUsername(employee.getUsername())) {
                bindingResult.rejectValue("username", "error.employee", "Username already exists");
            }
            if(bindingResult.hasErrors()) {
                return;
            }
            existingEmployee.setUsername(employee.getUsername());
            employeesRepository.save(existingEmployee);
        });
    }


    @Override
    public List<Employee> searchEmployeeByName(String name) {
        if(name != null && !name.isEmpty()){
            return employeesRepository.findByFullNameContainingIgnoreCase(name);
        }else{
            return employeesRepository.findAll();
        }
    }

    @Override
    public List<Employee> searchEmployeeByDepartment(String department) {
        if (department != null && !department.isEmpty()) {
            return employeesRepository.findByDepartmentNameContainingIgnoreCase(department);
        } else {
            return employeesRepository.findAll();
        }
    }

    @Override
    public Employee getEmployeeById(Long id) {
        return employeesRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + id));
    }


    private String validateName() throws Exception{
        throw new Exception("Name cannot be null");
    }
}
