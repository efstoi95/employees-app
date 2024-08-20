package com.enterprise.employees.service;

import com.enterprise.employees.model.*;
import com.enterprise.employees.repository.*;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final EmployeesRepository employeesRepository;
    private final FileRepository fileRepository;
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
            Set<File> filesToRemove = new HashSet<>(existingProject.getFiles());

            Customer customer = existingProject.getCustomer();
            if (customer != null) {
                Optional<Customer> customerOpt = customerRepository.findById(customer.getId());

                // Remove project from customer's project lists
                if (customerOpt.isPresent()) {
                    customerOpt.get().getProjects().remove(existingProject);
                    existingProject.setCustomer(null);
                    customerRepository.save(customerOpt.get());
                }
            }

            // Remove project from employees' project lists
            for (Employee employee : employeesToRemoveFromProject) {
                employee.getProjects().remove(existingProject);
                existingProject.removeEmployee(employee);
                employeeService.save(employee);
            }
            for(Task task : tasksToRemove){
                task.removeProject(existingProject);
                existingProject.removeTask(task);
                taskServiceImpl.deleteById(task.getId());

            }

            // Remove project from files' project lists
            for (File file : filesToRemove) {
                file.getProjects().remove(existingProject);
                existingProject.getFiles().remove(file);
                fileRepository.delete(file);
            }
            projectRepository.deleteById(id);
        }
    }


    @Override
    public Iterable<Project> findAll() {
        return projectRepository.findAll();
    }

    @Override
    public Iterable<ProjectDTO> findAllDto() {
        return projectRepository.findAll().stream()
                .map(project -> modelMapper.map(project, ProjectDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public void update(ProjectDTO projectDTO, BindingResult bindingResult) {

        Project existingProject = findById(projectDTO.getId());
        Customer newCustomer = customerRepository.findById(projectDTO.getCustomerId()).orElse(null);

        if (existingProject != null) {
            Customer oldCustomer = existingProject.getCustomer();

            existingProject.setName(projectDTO.getName());
            existingProject.setDescription(projectDTO.getDescription());
            existingProject.setStart(projectDTO.getStart());
            existingProject.setEnd(projectDTO.getEnd());


            // Update employees
            if (projectDTO.getEmployeeIds() != null) {
                Set<Long> newEmployeeIds = new HashSet<>(projectDTO.getEmployeeIds());
                Set<Long> currentEmployeeIds = existingProject.getEmployees().stream()
                        .map(Employee::getId)
                        .collect(Collectors.toSet());

                // Remove employees that are no longer in the new list
                for (Long currentEmployeeId : currentEmployeeIds) {
                    if (!newEmployeeIds.contains(currentEmployeeId)) {
                        employeesRepository.findById(currentEmployeeId).ifPresent(existingProject::removeEmployee);
                    }
                }

                // Add new employees that are not in the current list
                for (Long newEmployeeId : newEmployeeIds) {
                    if (!currentEmployeeIds.contains(newEmployeeId)) {
                        employeesRepository.findById(newEmployeeId).ifPresent(existingProject::addEmployee);
                    }
                }
            }

                if (newCustomer != null) {
                    if (oldCustomer != null) {
                        if (oldCustomer.getProjects().stream().anyMatch(p -> p.getId().equals(existingProject.getId()))) {
                            existingProject.setCustomer(null);
                            projectRepository.save(existingProject);
                            oldCustomer.setProjects(oldCustomer.getProjects()
                                    .stream()
                                    .filter(p -> !p.getId().equals(existingProject.getId()))
                                    .collect(Collectors.toList()));
                            customerRepository.save(oldCustomer);
                        }
                    }
                    existingProject.setCustomer(newCustomer);
                    newCustomer.getProjects().add(existingProject);
                    customerRepository.save(newCustomer);

                }

                projectRepository.save(existingProject);
            }
        }




    }

