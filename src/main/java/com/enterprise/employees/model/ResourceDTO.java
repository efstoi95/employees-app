package com.enterprise.employees.model;

import jakarta.persistence.ElementCollection;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ResourceDTO {

    private Long id;

    @NotBlank(message = "name is mandatory")
    private String name;

    private String description;

    private String qrCode;

    private final List<byte[]> fileContent = new ArrayList<>();

    @ElementCollection
    private List<String> fileNames = new ArrayList<>();
}
