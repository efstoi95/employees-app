package com.enterprise.employees.controller.web;

import com.enterprise.employees.model.Customer;
import com.enterprise.employees.model.CustomerProjectDTO;
import com.enterprise.employees.model.Project;
import com.enterprise.employees.repository.CustomerRepository;
import com.enterprise.employees.service.CustomerServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/map")
@Controller
public class MapController {


    private final CustomerServiceImpl customerService;
    private final CustomerRepository customerRepository;


    /**
     * Retrieves all customers and their project details to display on the map.
     *
     * @param model the model to hold the customer locations
     * @return the view name to display the map
     */
    @GetMapping("/allCustomers")
    public String showMap(Model model) {
        List<CustomerProjectDTO> customerProjectDTOs = customerService.findCustomersWithProjectDTO();
        model.addAttribute("customerLocations", customerProjectDTOs);
        return "map";

    }
}

