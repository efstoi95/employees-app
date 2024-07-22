package com.enterprise.employees.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
@Entity
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "FullName is mandatory")
    private String fullName;

    @NotBlank(message = "description is mandatory")
    private String description;

    @Size(max = 50, message = "Email exceeds the maximum length of 50 characters")
    @Email
    @NotBlank(message = "Email is mandatory")
    private String email;

    @Size(max = 50, message = "postalCode must be exactly 5 characters")
    @NotBlank(message = "description is mandatory")
    private String address;

    @Size(max = 30, message = "postalCode must be exactly 5 characters")
    @NotBlank(message = "description is mandatory")
    private String city;

    @Size(min = 5, max = 5, message = "postalCode must be exactly 5 characters")
    @NotBlank(message = "description is mandatory")
    private String postalCode;

    @OneToMany(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)

    private List<Project> projects= new ArrayList<>();

    public void addProject(Project  project) {
        this.projects.add(project);

    }
}
