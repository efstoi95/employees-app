package com.enterprise.employees.repository;

import com.enterprise.employees.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.chrono.ChronoLocalDateTime;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    boolean existsByName(String name);


}
