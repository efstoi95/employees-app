package com.enterprise.employees.service;


import com.enterprise.employees.model.Department;
import com.enterprise.employees.model.Employee;
import com.enterprise.employees.repository.DepartmentRepository;
import com.enterprise.employees.repository.EmployeesRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;

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
            if(employeesRepository.existsByUsername(employee.getUsername()) && !Objects.equals(existingEmployee.getUsername(), employee.getUsername())) {
                bindingResult.rejectValue("username", "error.employee", "Username already exists");
            }
            if(employeesRepository.existsByEmail(employee.getEmail()) && !Objects.equals(existingEmployee.getEmail(), employee.getEmail())) {
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

            if (employeesRepository.existsByUsername(employee.getUsername()) && !Objects.equals(existingEmployee.getUsername(), employee.getUsername())) {
                bindingResult.rejectValue("username", "error.employee", "Username already exists");
            }
            if(bindingResult.hasErrors()) {
                return;
            }
            existingEmployee.setUsername(employee.getUsername());
            employeesRepository.save(existingEmployee);

            // Update the username if it has been changed

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

    @Override
    public void updateEmployee(Employee employee) {
        Employee existingEmployee = employeesRepository.findById(employee.getId()).orElse(null);
        assert existingEmployee != null;
        employeesRepository.save(existingEmployee);

    }




}
