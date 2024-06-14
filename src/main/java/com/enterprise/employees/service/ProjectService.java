package com.enterprise.employees.service;

import com.enterprise.employees.model.Employee;
import com.enterprise.employees.model.Project;
import com.enterprise.employees.model.Skill;
import com.enterprise.employees.model.Task;
import com.enterprise.employees.repository.ProjectRepository;
import com.enterprise.employees.repository.TaskRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class ProjectService implements CrudService<Project> {

    private final ProjectRepository projectRepository;
    private final EmployeeService employeeService;
    private final SkillService skillService;
    @Lazy
    private final TaskService taskService;

    @Override
    public Project save(Project project) {
        return projectRepository.save(project);
    }

    @Override
    public void create(Project project, BindingResult bindingResult) {
        if(projectRepository.existsByName(project.getName())) {
            bindingResult.rejectValue("name", "error.project", "Name already exists");
        }
        if(bindingResult.hasErrors()) {
            return ;
        }
        projectRepository.save(project);
    }

    @Override
    public Project findById(Long id) {
        return projectRepository.findById(id).orElse(null);
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
                taskService.deleteById(task.getId());
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
    public void update(Project project, BindingResult bindingResult) {
        Project existingProject = findById(project.getId());
        if(existingProject != null){
            existingProject.setName(project.getName());
            existingProject.setDescription(project.getDescription());
            existingProject.setStart(project.getStart());
            existingProject.setEnd(project.getEnd());
            // Update employees
            existingProject.getEmployees().clear();
            for (Employee employee : project.getEmployees()) {
                existingProject.addEmployee(employee);
            }
            projectRepository.save(existingProject);
        }
    }

    @Override
    public void addEmployeeToTask(Task task, BindingResult bindingResult) {

    }


}
