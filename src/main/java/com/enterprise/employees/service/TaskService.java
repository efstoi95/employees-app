package com.enterprise.employees.service;

import com.enterprise.employees.model.Task;
import com.enterprise.employees.model.TaskDTO;
import org.springframework.validation.BindingResult;

public interface TaskService {
    Task save(Task task);


    void create(TaskDTO taskDTO, BindingResult bindingResult);

    Task findById(Long id);

    void deleteById(Long id);

    Iterable<Task> findAll();

    void editTask(TaskDTO taskDTO, BindingResult bindingResult);

    TaskDTO findByIdDTO(Long id);

    Long findProjectId();
}
