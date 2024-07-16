package com.enterprise.employees.controller.web;

import com.enterprise.employees.model.Customer;
import com.enterprise.employees.service.CustomerServiceImpl;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/customer")
@Controller
public class CostumerController {

    private static final Logger logger = LoggerFactory.getLogger(CostumerController.class);
    private final CustomerServiceImpl customerService;

    /**
     * Retrieves all customers and adds them to the model.
     *
     * @param  model  the model to add customers to
     * @return        the name of the view to render
     */
    @GetMapping("/allCustomers")
    public String allCustomers(Model model) {
            logger.info("Retrieving all customers");
            List<Customer> customers = (List<Customer>) customerService.findAll();
            model.addAttribute("customers", customers);
            logger.info("Number of projects retrieved: {}", customers.size());
            return "allCustomers";
    }
    /**
     * Creates a new customer and adds it to the model.
     *
     * @param  model  the model to add customers to
     * @return        the name of the view to render
     */
    @GetMapping("/createCustomer")
    public String createCustomer(Model model) {
        logger.info("Creating new customer");
        model.addAttribute("customer", new Customer());
        return "createCustomer";
    }
    /**
     * Creates a new customer and adds it to the model.
     *
     * @param  customer  description of parameter
     * @param  result    description of parameter
     * @return          description of return value
     */
    @PostMapping("/createdCustomer")
    public String createCustomer(@ModelAttribute("customer") Customer customer, BindingResult result, Model model) {
            logger.info("Entering (POST)createCustomer method");
            logger.debug("Customer data: {}", customer);
            customerService.create(customer, result);
            if (result.hasErrors()) {
                return "createCustomer";
            }
            logger.info("Customer created successfully");
            return "redirect:/customer/successCreateCustomer";
    }


    /**
     * Updating customer with the given ID.
     *
     * @param  id     the ID of the customer to update
     * @param  model  the model to add the updated customer to
     * @return        the name of the view to render
     */
    @GetMapping("/updateCustomer/{id}")
    public String updateCustomer(@PathVariable("id") Long id, Model model) {
        logger.info("Updating customer with id: {}", id);
        Customer existingCustomer = customerService.findById(id);
        model.addAttribute("customer",existingCustomer );
        return "updateCustomer";
    }
    /**
     * Updates the customer information based on the given data.
     *
     * @param  customer  description of parameter
     * @param  result    description of parameter
     * @return          the name of the view to render
     */
    @PostMapping("/updatedCustomer")
    public String editedCustomer(@ModelAttribute("customer") Customer customer, BindingResult result, Model model) {
            logger.info("Entering (POST)updateCustomer method");
            logger.debug("Customer data: {}", customer);
            customerService.update(customer, result);
            if (result.hasErrors()) {
                return "updateCustomer";
            }
            logger.info("Customer edited successfully");
            return "redirect:/customer/successUpdateCustomer";

    }


    /**
     * Deletes a customer based on the provided ID.
     *
     * @param  id  the ID of the customer to be deleted
     * @return     a redirection to the successDeleteCustomer page
     */
    @GetMapping("/deleteCustomer/{id}")
    public String deleteCustomer(@PathVariable("id") Long id,Model model) {
        logger.info("Deleting customer with id: {}", id);
        customerService.deleteById(id);
        logger.info("Customer deleted successfully");
        return "redirect:/customer/successDeleteCustomer";

    }

    @GetMapping("/successCreateCustomer")
    public String successCreateCustomer(Model model) {
        model.addAttribute("message", "Customer created successfully");

        return "successCustomer";
    }

    @GetMapping("/successUpdateCustomer")
    public String successUpdateCustomer(Model model) {
        model.addAttribute("message", "Customer edited successfully");
        return "successCustomer";
    }

    @GetMapping("/successDeleteCustomer")
    public String successDeleteCustomer(Model model) {
        model.addAttribute("message", "Customer deleted successfully");
        return "successCustomer";
    }
}