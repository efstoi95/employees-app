package com.enterprise.employees.service;

import com.enterprise.employees.model.Customer;
import com.enterprise.employees.model.CustomerDTO;
import com.enterprise.employees.model.Task;
import org.springframework.validation.BindingResult;

import java.util.List;

public interface CustomerService {
    Customer save(Customer customer);

    void create(Customer customer, BindingResult bindingResult);

    Customer findById(Long id);

    void deleteById(Long id);

    Iterable<Customer> findAll();

    List<CustomerDTO> findallDTO();

    void update(Customer customer, BindingResult bindingResult);

}
