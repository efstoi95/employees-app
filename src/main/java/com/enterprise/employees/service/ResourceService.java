package com.enterprise.employees.service;

import com.enterprise.employees.model.Resource;
import com.enterprise.employees.model.ResourceDTO;
import org.springframework.validation.BindingResult;

import java.util.List;

public interface ResourceService {
    List<Resource> findAll();

    List<ResourceDTO> findAllDTO();


    void create(ResourceDTO resourceDTO, BindingResult bindingResult);


    Resource findById(Long id);

    ResourceDTO findByIdDTO(Long id);

    void edit(ResourceDTO resourceDTO, BindingResult bindingResult);

    void delete(Long id);

    void save(Resource resource);
}
