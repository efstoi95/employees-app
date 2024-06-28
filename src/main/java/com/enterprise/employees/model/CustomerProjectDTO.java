package com.enterprise.employees.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Data
//@ToString
//@Entity
public class CustomerProjectDTO {

    private Long id;
    private String name;
    private String address;
    private String city;
    private String postalCode;
    private String projectName;

}
