package com.enterprise.employees.model;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class EmployeeDTO {

    private Long id; // For edit purposes

    @Size(max = 20, message = "Full name exceeds the maximum length of 20 characters")
    @NotBlank(message = "Full name is mandatory")
    private String fullName;

    @Size(max = 100, message = "Password exceeds the maximum length of 50 characters")
    private String password; // Password can be empty during edit

    @Size(max = 50, message = "Email exceeds the maximum length of 50 characters")
    @Email
    @NotBlank(message = "Email is mandatory")
    private String email;

    @Size(max = 20, message = "Username exceeds the maximum length of 20 characters")
    @NotBlank(message = "Username is mandatory")
    private String username;

    @Enumerated(EnumType.STRING)
    private EmployeeRoles role;

    private boolean verified = false;

    private String verifiedCode;

    private List<Long> skillIds; // IDs of selected skills

    private Long departmentId; // ID of the selected department

}
