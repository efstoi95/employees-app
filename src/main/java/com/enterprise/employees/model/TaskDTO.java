package com.enterprise.employees.model;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TaskDTO {

    private Long id;

    @Size(max = 20, message = "name exceeds the maximum length of 20 characters")
    @NotBlank(message = "name is mandatory")
    private String name;

    @NotBlank(message = "description is mandatory")
    private String description;

    private Long projectId;

    private List<Long> employeeIds;

    private List<Long> skillsIds;

    @Enumerated(EnumType.STRING)
    private Status status;

    private String durationInput;

    private List<byte[]> fileContent = new ArrayList<>();

    @ElementCollection
    private List<String> fileNames = new ArrayList<>();

    private boolean finished;
}
