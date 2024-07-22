package com.enterprise.employees.repository;

import com.enterprise.employees.model.Employee;
import com.enterprise.employees.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    boolean existsByName(String name);


    List<Project> findByEmployeesContaining(Employee employee);

    Project findByCustomerId(Long id);
}
