package com.enterprise.employees.service;

import com.enterprise.employees.model.Project;
import com.enterprise.employees.model.Task;
import org.springframework.validation.BindingResult;

public interface CrudService <T>{

    T save(T t);

    void create(T t);



    T findById(Long id);

    void deleteById(Long id);

    Iterable<T> findAll();

    void update(T t);



}
