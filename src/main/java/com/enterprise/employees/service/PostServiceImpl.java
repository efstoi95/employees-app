package com.enterprise.employees.service;

import com.enterprise.employees.model.Post;
import com.enterprise.employees.repository.PostRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class PostServiceImpl implements CrudService<Post> {

    private final PostRepository postRepository;



    @Override
    public Post save(Post post) {
        return postRepository.save(post);
    }

    @Override
    public void create(Post post) {

        postRepository.save(post);


    }

    @Override
    public Post findById(Long id) {
        return postRepository.findById(id).orElse(null);
    }

    @Override
    public void deleteById(Long id) {
        postRepository.deleteById(id);

    }

    @Override
    public Iterable<Post> findAll() {
        return postRepository.findAll();
    }

    @Override
    public void update(Post post) {

    }

    public List<Post> findAllByProjectId(Long projectId) {

        return postRepository.findAllByProjectId(projectId);
    }
}
