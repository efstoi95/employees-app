package com.enterprise.employees.service;

import com.enterprise.employees.model.*;
import com.enterprise.employees.repository.CustomerRepository;
import com.enterprise.employees.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    CustomerRepository customerRepository;
    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public Customer save(Customer customer) {
        return customerRepository.save(customer);
    }

    @Override
    public void create(Customer customer, BindingResult bindingResult) {
        if(customerRepository.existsByEmail(customer.getEmail())) {
            bindingResult.rejectValue("email", "error.customer", "Email already exists");
        }
        if(bindingResult.hasErrors()) {
            return;
        }
        Customer savedCustomer = customerRepository.save(customer);

    }

    @Override
    public Customer findById(Long id) {
        return customerRepository.findById(id).orElse(null);
    }

    @Override
    public void deleteById(Long id) {
        Customer existingCustomer = customerRepository.findById(id).orElse(null);
        if (existingCustomer != null) {
            for (Project project : existingCustomer.getProjects()) {
                project.setCustomer(null);
                projectRepository.save(project); // Assuming you have a projectRepository
            }
            customerRepository.deleteById(id);

        }else {
            throw new IllegalArgumentException("Customer not found with ID: " + id);
        }
    }

    @Override
    public Iterable<Customer> findAll() {
        return customerRepository.findAll();
    }
    @Override
    public List<CustomerDTO> findallDTO(){
        List<Customer> customers = (List<Customer>) findAll();
        return customers.stream()
                        .map(customer -> modelMapper.map(customer, CustomerDTO.class))
                        .collect(Collectors.toList());
    }

    public List<CustomerProjectDTO> findCustomersWithProjectDTO(){
        List<Customer> customersWithProjects = findCustomersWithProject();
        List<CustomerProjectDTO> customerProjectDTOs = new ArrayList<>();

        for(Customer customer : customersWithProjects){
            for(Project project : customer.getProjects()){
                CustomerProjectDTO dto = modelMapper.map(customer, CustomerProjectDTO.class);
                dto.setProjectName(project.getName());
                customerProjectDTOs.add(dto);
            }
        }
        return customerProjectDTOs;
    }

    public  List<Customer> findCustomersWithProject(){
        List<Customer> customersWithProject = new ArrayList<>();
       List<Customer> customers = customerRepository.findAll();
       for(Customer customer : customers){
           if(!customer.getProjects().isEmpty()){
               assert false;
               customersWithProject.add(customer);
           }
       }
       return customersWithProject.isEmpty() ? null : customersWithProject;

    }

    @Override
    public void update(Customer customer, BindingResult bindingResult) {
        Customer existingCustomer = customerRepository.findById(customer.getId()).orElse(null);
        if(existingCustomer != null) {
            if(customerRepository.existsByEmail(customer.getEmail()) && !Objects.equals(existingCustomer.getEmail(), customer.getEmail())) {
                bindingResult.rejectValue("email", "error.customer", "Email already exists");
            }
            if(bindingResult.hasErrors()) {
                return;
            }
            existingCustomer.setFullName(customer.getFullName());
            existingCustomer.setDescription(customer.getDescription());
            existingCustomer.setEmail(customer.getEmail());
            existingCustomer.setAddress(customer.getAddress());
            existingCustomer.setCity(customer.getCity());
            existingCustomer.setPostalCode(customer.getPostalCode());
            customerRepository.save(existingCustomer);
        }else {
            throw new IllegalArgumentException("Customer not found with ID: " + customer.getId());
        }
    }

}
