package com.enterprise.employees.repository;

import com.enterprise.employees.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    /**
     * Finds all posts by project ID.
     *
     * @param  projectId  the ID of the project to search for
     * @return            a list of posts belonging to the specified project
     */
    List<Post> findAllByProjectId(Long projectId);
}
