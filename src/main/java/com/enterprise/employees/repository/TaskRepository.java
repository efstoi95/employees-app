package com.enterprise.employees.repository;

import com.enterprise.employees.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, Long> {

    boolean existsByName(String name);
    @Query("SELECT CASE WHEN COUNT(t) = 0 " +
            "THEN true ELSE false END" +
            " FROM Task t " +
            "WHERE t.project.id = :projectId AND t.status <> 'closed'")
    boolean areAllTasksClosed(@Param("projectId") Long projectId);
}
