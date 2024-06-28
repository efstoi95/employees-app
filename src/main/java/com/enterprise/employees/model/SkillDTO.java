package com.enterprise.employees.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SkillDTO {

    private Long id;

    @NotBlank(message = "Name is mandatory")
    private String name;

    private List<Long> employeesIds = new ArrayList<>();

    private List<Long> tasksIds = new ArrayList<>();
}
