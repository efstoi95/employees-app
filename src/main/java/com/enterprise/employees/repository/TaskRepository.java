package com.enterprise.employees.repository;

import com.enterprise.employees.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {

    boolean existsByName(String name);
}
