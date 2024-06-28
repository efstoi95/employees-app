package com.enterprise.employees.service;

import com.enterprise.employees.model.*;
import com.enterprise.employees.repository.CustomerRepository;
import com.enterprise.employees.repository.EmployeesRepository;
import com.enterprise.employees.repository.ProjectRepository;
import com.enterprise.employees.repository.TaskRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final EmployeesRepository employeesRepository;
    private final EmployeeService employeeService;
    private final TaskRepository taskRepository;
    @Lazy
    private final TaskServiceImpl taskServiceImpl;
    private final FileStorageService fileStorageService;
    private final CustomerRepository customerRepository;
    @Autowired
    ModelMapper modelMapper;

    @Override
    public Project save(Project project) {
        return projectRepository.save(project);
    }

    @Override
    public void create(ProjectDTO projectDTO, BindingResult bindingResult) {
        projectDTO.setStatus(Status.OPEN);
        Project project = modelMapper.map(projectDTO, Project.class);
        if(projectRepository.existsByName(project.getName())) {
            bindingResult.rejectValue("name", "error.project", "Name already exists");
        }
        if(bindingResult.hasErrors()) {
            return  ;
        }
        projectRepository.save(project);
    }

    public List<ProjectDTO> findAllProjects() {
        List<Project> projects = projectRepository.findAll();
        return projects.stream()
                .map(project -> modelMapper.map(project, ProjectDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public Project findById(Long id) {
        return projectRepository.findById(id).orElse(null);
    }
    public ProjectDTO findByIdDTO(Long id) {
        return modelMapper.map(findById(id), ProjectDTO.class);
    }
    @Override
    public void deleteById(Long id) {
        Project existingProject = findById(id);
        if (existingProject != null) {
            Set<Employee> employeesToRemoveFromProject = new HashSet<>(existingProject.getEmployees());
            Set<Task> tasksToRemove = new HashSet<>(existingProject.getTasks());

            // Remove project from employees' project lists
            for (Employee employee : employeesToRemoveFromProject) {
                employee.getProjects().remove(existingProject);
                employeeService.save(employee);
            }
            for(Task task : tasksToRemove){
                taskServiceImpl.deleteById(task.getId());
            }

            // The cascade and orphanRemoval settings on the Project.tasks field handle task deletion.
            projectRepository.deleteById(id);
        }
    }


    @Override
    public Iterable<Project> findAll() {
        return projectRepository.findAll();
    }

    @Override
    public void update(ProjectDTO projectDTO, BindingResult bindingResult) {

        Project existingProject = findById(projectDTO.getId());
        Customer existingCustomer = customerRepository.findById (projectDTO.getCustomerId()).orElse(null);
        if(existingProject != null){
            existingProject.setName(projectDTO.getName());
            existingProject.setDescription(projectDTO.getDescription());
            existingProject.setStart(projectDTO.getStart());
            existingProject.setEnd(projectDTO.getEnd());

            // Update employees
            existingProject.getEmployees().clear();
            if (projectDTO.getEmployeeIds() != null) {
                for (Long employeeId : projectDTO.getEmployeeIds()) {
                    employeesRepository.findById(employeeId).ifPresent(existingProject::addEmployee);
                }
            }


            existingProject.setCustomer(existingCustomer);
            projectRepository.save(existingProject);

            // Ensure not to add the project if it already exists
            if (existingCustomer != null && !existingCustomer.getProjects().contains(existingProject)) {
                existingCustomer.addProject(existingProject);
                customerRepository.save(existingCustomer);
            }
        }
    }
}
