package com.enterprise.employees.service;

import com.enterprise.employees.model.*;
import com.enterprise.employees.repository.ProjectRepository;
import com.enterprise.employees.repository.SkillRepository;
import com.enterprise.employees.repository.TaskRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

@Service
@AllArgsConstructor
public class TaskServiceImpl implements TaskService  {

    ProjectRepository projectRepository;
    SkillRepository skillRepository;

    TaskRepository taskRepository;
    @Autowired
    EmployeeService employeeService;
    @Autowired
    SkillService skillService;
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public Task save(Task task) {
        return taskRepository.save(task);
    }

    @Override
    public void create(TaskDTO taskDTO, BindingResult bindingResult) {

        Task task = modelMapper.map(taskDTO, Task.class);
        // Fetch and set the selected skills
        Optional<Project> maybeProject = projectRepository.findById(task.getProject().getId());
        if (maybeProject.isEmpty()) {
            throw new IllegalArgumentException("Project not found");
        }
        Project project = maybeProject.get();
        List<Skill> selectedSkills = new ArrayList<>();

        for (Long skillId : taskDTO.getSkillsIds()) {
            skillRepository.findById(skillId).ifPresent(selectedSkills::add);
        }
        //TODO: fixing the issue with the SkillsDTO
        task.setSkills(selectedSkills);
        String durationStr = task.getDurationInput();
        Duration duration = parseDuration(durationStr);
        if(duration==null){
            bindingResult.rejectValue("durationInput", "error.task", "Task duration must be in the format '1d2h3m'");
        }
        task.setDuration(duration);
        LocalDateTime taskEndDate = project.getStart().plus(task.getDuration());

        // Check if task end date is within project's end date
        if (taskEndDate.isAfter(project.getEnd())) {
            bindingResult.rejectValue("durationInput", "error.task", "Task duration must be within project's start and end dates");
        }

        if(bindingResult.hasErrors()) {
            return;
        }

        //Save the task if there is no validation error
        Task savedTask = taskRepository.save(task);

        // Add the task to the project's list of tasks
        project.addTask(savedTask);
        boolean taskIsOpen = project.getTasks().stream()
                .allMatch(t -> t.getStatus() == Status.CLOSED);
        project.setFinished(taskIsOpen);
        projectRepository.save(project);
    }

    public static Duration parseDuration(String durationStr) {
        // Define the pattern to match the duration string
        Pattern pattern = Pattern.compile("(\\d+)([wdhm])");
        Matcher matcher = pattern.matcher(durationStr);

        // Initialize a duration of zero
        Duration duration = Duration.ZERO;

        // Process each match found in the string
        while (matcher.find()) {
            // Extract the number and the unit
            int value = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2);

            // Add to the duration based on the unit
            switch (unit) {
                case "w":
                    duration = duration.plusDays(value * 7); // 1 week = 7 days
                    break;
                case "d":
                    duration = duration.plusDays(value);
                    break;
                case "h":
                    duration = duration.plusHours(value);
                    break;
                case "m":
                    duration = duration.plusMinutes(value);
                    break;
            }
        }
        return duration;
    }



    @Override
    public Task findById(Long id) {
        return taskRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Task not found"));
    }

    @Override
    public void deleteById(Long id) {
        Task existingTask = findById(id);
        if(existingTask != null){
            // Remove task from project's task list
            Optional<Project> project = projectRepository.findById(existingTask.getProject().getId());
            if(project.isPresent()){
//                boolean allTasksClosed = project.get().getTasks().stream()
//                        .allMatch(t -> t.getStatus() == Status.CLOSED);
//                project.get().setFinished(allTasksClosed);
                project.get().removeTask(existingTask);
                projectRepository.save(project.get());
            }
            // Remove task from employees' task lists
            Set<Employee> employeesToRemoveFromTask = new HashSet<>(existingTask.getEmployees());
            for (Employee employee : employeesToRemoveFromTask) {
                employee.getTasks().remove(existingTask);
                employeeService.save(employee);
            }
            // Remove task from skills' task lists
            Set<Skill> skillsToRemoveFromTask = new HashSet<>(existingTask.getSkills());
            for (Skill skill : skillsToRemoveFromTask) {
                skill.getTasks().remove(existingTask);
                skillService.save(skill);
            }
            //Delete the task
            taskRepository.deleteById(id);

        }
    }

    @Override
    public Iterable<Task> findAll() {
        return taskRepository.findAll();
    }


    @Override
    public void editTask(TaskDTO taskDTO, BindingResult bindingResult) {
        Task task = modelMapper.map(taskDTO, Task.class);
        Task existingTask = findById(task.getId());

        if(existingTask != null){
            // Update task properties
            existingTask.setName(task.getName());
            existingTask.setDescription(task.getDescription());

            existingTask.setStatus(task.getStatus());
            existingTask.setStatus(task.getStatus());
            // Parse and set duration
            String durationStr = task.getDurationInput();
            Duration duration = parseDuration(durationStr);
            task.setDuration(duration);
            existingTask.setDuration(task.getDuration());
            // Clear existing employees and add new ones
            existingTask.getEmployees().clear();
           for(Long employeeId : taskDTO.getEmployeeIds()){
                Employee employee = employeeService.getEmployeeById(employeeId);
                existingTask.addEmployee(employee);
            }
            // Save updated task
            taskRepository.save(existingTask);
            // Update project finished status based on task statuses
           Project project = existingTask.getProject();
            boolean allTasksClosed = project.getTasks().stream()
                    .allMatch(t -> t.getStatus() == Status.CLOSED);
            project.setFinished(allTasksClosed);
            // Save updated project
           projectRepository.save(project);
        }else {
            // Handle case where task with given ID is not found
            throw new IllegalArgumentException("Task not found with ID: " + task.getId());
        }
    }

    @Override
    public TaskDTO findByIdDTO(Long id) {
        Task task = findById(id);
        List<Long> skillIds = task.getSkills().stream().map(Skill::getId).toList();
        TaskDTO taskDTO = modelMapper.map(task, TaskDTO.class);
        taskDTO.setSkillsIds(skillIds);
        return taskDTO;
    }


    @Override
    public Long findProjectId() {
        return null;
    }

}
