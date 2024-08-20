package com.enterprise.employees.service;

import com.enterprise.employees.model.Project;
import com.enterprise.employees.model.ProjectDTO;
import org.springframework.validation.BindingResult;

public interface ProjectService {
    Project save(Project project);

    void create(ProjectDTO projectDTO, BindingResult bindingResult);

    Project findById(Long id);

    void deleteById(Long id);

    Iterable<Project> findAll();

    Iterable<ProjectDTO> findAllDto();

    void update(ProjectDTO projectDTO, BindingResult bindingResult);

    ProjectDTO findByIdDTO(Long id);
}
