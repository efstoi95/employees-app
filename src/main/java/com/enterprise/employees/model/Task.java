package com.enterprise.employees.model;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
@Entity
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Size(max = 20, message = "name exceeds the maximum length of 20 characters")
    @NotBlank(message = "name is mandatory")
    private String name;

    @NotBlank(message = "description is mandatory")
    private String description;


    private Duration duration;

    @Transient
//    @NotBlank(message = "Duration is mandatory")
    private String durationInput;

    private boolean erledigt;

    private boolean finished;

    @Enumerated(EnumType.STRING)
    private Status status;



    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    private List<File> files = new ArrayList<>();

    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinTable(
            name = "employee_tasks",
            joinColumns = @JoinColumn(name = "tasks_id"),
            inverseJoinColumns = @JoinColumn(name = "employee_id"))
    private List<Employee> employees = new ArrayList<>();

    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinTable(
        name  = "resources_tasks",
        joinColumns = @JoinColumn(name = "tasks_id"),
        inverseJoinColumns = @JoinColumn(name = "resource_id"))
    private List<Resource> resources = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "project_id")
    @JsonBackReference
    private Project project;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Post> posts = new ArrayList<>();


    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    private List<Skill> skills = new ArrayList<>();


    public void addEmployee(Employee employee) {
        this.employees.add(employee);
        employee.getTasks().add(this);
    }

    public void removeEmployee(Employee employee) {
        this.employees.remove(employee);
        employee.getTasks().remove(this);
    }

    public void addResource(Resource resource) {
        this.resources.add(resource);
        resource.getTasks().add(this);
    }

    public void removeResource(Resource resource) {
        this.resources.remove(resource);
        resource.getTasks().remove(this);
    }

    public void removeProject(Project project) {
        this.project.getTasks().remove(this);
        this.project = null;
    }



}
