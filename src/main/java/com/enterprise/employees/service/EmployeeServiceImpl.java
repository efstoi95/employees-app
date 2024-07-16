package com.enterprise.employees.service;


import com.enterprise.employees.model.*;
import com.enterprise.employees.repository.DepartmentRepository;
import com.enterprise.employees.repository.EmployeesRepository;
import com.enterprise.employees.repository.SkillRepository;
import jakarta.mail.MessagingException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    EmployeesRepository employeesRepository;
    DepartmentRepository departmentRepository;
    SkillRepository skillRepository;
    PasswordEncoder passwordEncoder;
    @Autowired
    EmailService emailService;
    @Autowired
    ModelMapper modelMapper;


    public EmployeeServiceImpl(EmployeesRepository employeesRepository, DepartmentRepository departmentRepository,SkillRepository skillRepository, PasswordEncoder passwordEncoder) {
        this.employeesRepository = employeesRepository;
        this.departmentRepository = departmentRepository;
        this.skillRepository = skillRepository;
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
    public List<Skill> getAllSkills() {
        return skillRepository.findAll();
    }

    @Override
    public void generateAndSendPassword(Employee employee) throws IOException, MessagingException {
        SecureRandom random = new SecureRandom();
        int randomNumber = 1000 + random.nextInt(9000); // ensures a 4-digit number
        employee.setVerifiedCode(String.valueOf(randomNumber));
        employeesRepository.save(employee);

        String employeeEmail = employee.getEmail();
        if(employeeEmail == null) {
            throw new IllegalArgumentException("Employee email cannot be null");
        }
        String htmlTemplate = new String(Files.readAllBytes(Paths.get("src/main/resources/templates/verify.html")));
        String htmlBody = htmlTemplate.replace("[VerificationNumber]", employee.getVerifiedCode());
        emailService.sendEmail(employeeEmail, "Your new verification code", htmlBody);

    }



    @Override
    public Employee createEmployee(EmployeeDTO employeeDTO, BindingResult bindingResult) {
        SecureRandom random = new SecureRandom();
        int randomNumber = 1000 + random.nextInt(9000); // ensures a 4-digit number
        employeeDTO.setVerifiedCode(String.valueOf(randomNumber));

        Employee employee = modelMapper.map(employeeDTO, Employee.class);

        String hashedPassword = passwordEncoder.encode(employee.getPassword());
        employee.setPassword(hashedPassword);

        if(employeesRepository.existsByUsername(employee.getUsername())) {
            bindingResult.rejectValue("username", "error.employee", "Username already exists");
        }
        if(employeesRepository.existsByEmail(employee.getEmail())) {
            bindingResult.rejectValue("email", "error.employee", "Email already exists");
        }

        if(bindingResult.hasErrors()) {
            return null;
        }
        List<Skill> selectedSkills = new ArrayList<>();
        if(employeeDTO.getSkillIds() == null || employeeDTO.getSkillIds().isEmpty()) {
            bindingResult.rejectValue("skillIds", "error.employee", "Please select at least one skill");
            return null;
        }
        for (Long skillId : employeeDTO.getSkillIds()) {
            skillRepository.findById(skillId).ifPresent(selectedSkills::add);
        }
        employee.setSkills(selectedSkills);
        Department department = departmentRepository.findById(employeeDTO.getDepartmentId()).orElseThrow(() -> new RuntimeException("Department not found"));
        employee.setDepartment(department);

        Employee savedEmployee = employeesRepository.save(employee);

        if (department != null) {
            department.getEmployees().add(employee);
            departmentRepository.save(department);
        }
        return savedEmployee;
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
            // Remove the employee from each skill's list of employees
            List<Skill> skills = employee.getSkills();
            for (Skill skill : skills) {
                skill.getEmployees().remove(employee);
            }
            // Clear the employee's list of skills
            employee.getSkills().clear();
            // Now delete the employee
            employeesRepository.deleteById(id);
        }
    }





    @Override
    public void editEmployee(EmployeeDTO employeeEditDTO, BindingResult bindingResult) {

        Employee employee = modelMapper.map(employeeEditDTO, Employee.class);
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
    public void updateEmployee(Employee employee,BindingResult bindingResult) {
        Employee existingEmployee = employeesRepository.findById(employee.getId()).orElse(null);
        assert existingEmployee != null;
        if(!employee.getVerifiedCode().equals(existingEmployee.getVerifiedCode())) {
            bindingResult.rejectValue("verifiedCode", "error.employee", "Enter a valid code");
        }
        if(bindingResult.hasErrors()) {
            return;
        }
        existingEmployee.setVerified(true);
        employeesRepository.save(existingEmployee);

    }

    @Override
    public List<Employee> findEmployeesByIds(List<Long> selectedEmployeesIds) {
        return employeesRepository.findAllById(selectedEmployeesIds);
    }

    @Override
    public List<EmployeeDTO> findEmployeesWithUserRoleDTO() {
        List<Employee> employees = findEmployeesWithUserRole();
        return employees.stream()
                        .map(employee -> modelMapper.map(employee, EmployeeDTO.class))
                        .collect(Collectors.toList());

    }

    @Override
    public List<Employee> findEmployeesWithUserRole() {
        return employeesRepository.findByRole(EmployeeRoles.USER);
    }

    public List<Employee> getEmployeesByProjectsAndSkills(Long projectId, List<Skill> skills) {
        return employeesRepository.getEmployeesByProjectIdAndSkills(projectId, skills);
    }

    @Override
    public void save(Employee employee) {
        employeesRepository.save(employee);
    }



}
