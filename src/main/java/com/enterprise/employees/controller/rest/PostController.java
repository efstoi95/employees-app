package com.enterprise.employees.controller.rest;

import com.enterprise.employees.model.Poste;
import com.enterprise.employees.model.Project;
import com.enterprise.employees.model.ProjectDTO;
import com.enterprise.employees.service.PostRestClientService;
import com.enterprise.employees.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class PostController {

    private final PostRestClientService postRestClientService;
    private final ProjectService projectService;


    @PostMapping("/send")
    public List<ProjectDTO> sendProjectData() {
        List<ProjectDTO> projects= (List<ProjectDTO>) projectService.findAllDto();
        return postRestClientService.postProjectData(projects);
    }

    @GetMapping("/projects")
    @Secured("ROLE_ADMIN")
    public List<ProjectDTO> getAllProjects() {
        return (List<ProjectDTO>) projectService.findAllDto();
    }

    @GetMapping("/project/{id}")
    @Secured("ROLE_ADMIN")
    public ProjectDTO getProjectById(@PathVariable Long id) {
        if(projectService.findById(id) == null) {
            return null;
        }
        return projectService.findByIdDTO(id);
    }

    @GetMapping("/allPosts")
    @Secured("ROLE_ADMIN")
    public List<Poste> getAllPosts(){
        return postRestClientService.findAllPosts();
    }

}

