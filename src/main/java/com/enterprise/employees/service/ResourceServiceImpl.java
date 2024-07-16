package com.enterprise.employees.service;

import com.enterprise.employees.model.Resource;
import com.enterprise.employees.model.ResourceDTO;
import com.enterprise.employees.model.Skill;
import com.enterprise.employees.model.Task;
import com.enterprise.employees.repository.ResourceRepository;
import com.enterprise.employees.repository.TaskRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ResourceServiceImpl implements ResourceService {

    @Autowired
    ResourceRepository resourceRepository;
    @Autowired
    ModelMapper modelMapper;

    @Autowired
    TaskRepository taskRepository;


    @Override
    public List<Resource> findAll() {
        return resourceRepository.findAll();
    }

    @Override
    public List<ResourceDTO> findAllDTO() {
        List<Resource> resources =  findAll();
        return resources.stream().map(resource -> modelMapper.map(resource, ResourceDTO.class)).collect(Collectors.toList());
    }

    @Override
    public void create(ResourceDTO resourceDTO, BindingResult bindingResult) {

        Resource resource = modelMapper.map(resourceDTO, Resource.class);

        if(resourceRepository.existsByName(resource.getName())) {
            bindingResult.rejectValue("name", "error.resource", "Resource already exists");
        }
        if (resourceRepository.existsByQrCode(resource.getQrCode())) {
            bindingResult.rejectValue("qrCode", "error.resource", "QR code already exists");
        }

        if (bindingResult.hasErrors()) {
            return;
        }
        resourceRepository.save(resource);

    }

    @Override
    public Resource findById(Long id) {
        return resourceRepository.findById(id).orElse(null);
    }

    @Override
    public ResourceDTO findByIdDTO(Long id) {

        Resource resource = resourceRepository.findById(id).orElse(null);
        if(resource != null) {
            return modelMapper.map(resource, ResourceDTO.class);
        }
        return null;
    }

    @Override
    public void edit(ResourceDTO resourceDTO, BindingResult bindingResult) {
        Resource existingResource = resourceRepository.findById(resourceDTO.getId()).orElse(null);
        if(existingResource != null) {

            if(resourceRepository.existsByName(resourceDTO.getName()) && !Objects.equals(existingResource.getName(), resourceDTO.getName())) {
                bindingResult.rejectValue("name", "error.resource", "Resource already exists");
            }
            if (resourceRepository.existsByQrCode(resourceDTO.getQrCode()) && !Objects.equals(existingResource.getQrCode(), resourceDTO.getQrCode())) {
                bindingResult.rejectValue("qrCode", "error.resource", "QR code already exists");
            }
            if (bindingResult.hasErrors()) {
                return;
            }
            existingResource.setName(resourceDTO.getName());
            existingResource.setDescription(resourceDTO.getDescription());
            existingResource.setQrCode(resourceDTO.getQrCode());
            resourceRepository.save(existingResource);
        }

    }

    @Override
    public void delete(Long id) {
        Resource resource = resourceRepository.findById(id).orElse(null);
        // Remove the employee from each skill's list of employees

        if(resource != null) {
            List<Task> tasks = resource.getTasks();
            if(tasks != null) {
                for (Task task : tasks) {
                    task.getResources().remove(resource);
                }
            }
            assert resource.getTasks() != null;
            resource.getTasks().clear();
            resourceRepository.deleteById(id);
        }
    }

    @Override
    public void save(Resource resource) {
        resourceRepository.save(resource);
    }
}
