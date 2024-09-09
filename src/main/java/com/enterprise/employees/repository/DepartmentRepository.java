package com.enterprise.employees.repository;

import com.enterprise.employees.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface DepartmentRepository extends JpaRepository<Department, Long> {


    Department findByName(String name);

    Department findFirstByName(String name);

    boolean existsByName(String name);
}
