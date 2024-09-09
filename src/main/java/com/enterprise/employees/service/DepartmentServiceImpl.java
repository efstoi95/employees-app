package com.enterprise.employees.service;

import com.enterprise.employees.model.Department;
import com.enterprise.employees.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements CrudService<Department> {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Override
    public Department save(Department department) {
        return departmentRepository.save(department);
    }

    @Override
    public void create(Department department) {

    }

    @Override
    public Department findById(Long id) {
        return departmentRepository.findById(id).orElseThrow(() -> new RuntimeException("Department not found"));
    }

    @Override
    public void deleteById(Long id) {
        departmentRepository.deleteById(id);
    }

    @Override
    public Iterable<Department> findAll() {
        return departmentRepository.findAll();
    }

    @Override
    public void update(Department department) {

    }

}
