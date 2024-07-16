package com.enterprise.employees.model;

import jakarta.persistence.*;
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
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 20, message = "name exceeds the maximum length of 20 characters")
    @NotBlank(message = "name is mandatory")
    private String name;

    private String description;

    @NotBlank(message = "qrCode is mandatory")
    private String qrCode;

    @ManyToMany(mappedBy ="resources", cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    private List<Task> tasks = new ArrayList<>();

    @ElementCollection
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private List<byte[]> fileContent = new ArrayList<>();

    @ElementCollection
    private List<String> fileNames = new ArrayList<>();
}
