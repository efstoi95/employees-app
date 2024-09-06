package com.enterprise.employees.controller.web;

import com.enterprise.employees.model.Customer;
import com.enterprise.employees.model.CustomerProjectDTO;
import com.enterprise.employees.service.CustomerServiceImpl;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
@RequestMapping("/customer")
@Controller
public class CostumerController {

    private static final Logger logger = LoggerFactory.getLogger(CostumerController.class);
    private final CustomerServiceImpl customerService;
    @Autowired
    private MessageSource messageSource;

    /**
     * Retrieves all customers and adds them to the model.
     *
     * @param  model  the model to add customers to
     * @return        the name of the view to render
     */
    @GetMapping("/allCustomers")
    public String allCustomers(@RequestParam(name = "locale", required = false) String localeParam,
                               Model model, RedirectAttributes redirectAttributes) {
            logger.info("Retrieving all customers");
        Locale locale = Locale.getDefault();
        if (localeParam != null) {
            locale=Locale.forLanguageTag(localeParam);
        }
        LocaleContextHolder.setLocale(locale);
        String message = messageSource.getMessage("allResources.title", null, locale);
        model.addAttribute("message", message);
        model.addAttribute("customerCreatedSuccess", messageSource.getMessage("customerCreatedSuccess", null, locale));
        model.addAttribute("customerUpdatedSuccess", messageSource.getMessage("customerUpdatedSuccess", null, locale));
        model.addAttribute("customerDeletedSuccess", messageSource.getMessage("customerDeletedSuccess", null, locale));
        List<Customer> customers = (List<Customer>) customerService.findAll();
        model.addAttribute("isAllCustomerPage", true);
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
    public String createCustomer(@RequestParam(name = "locale", required = false) String localeParam,
                                 Model model) {
        logger.info("Creating new customer");
        Locale locale = Locale.getDefault();
        if (localeParam != null) {
            locale=Locale.forLanguageTag(localeParam);
        }
        LocaleContextHolder.setLocale(locale);
        String message = messageSource.getMessage("createCustomer.title", null, locale);
        model.addAttribute("message", message);
        model.addAttribute("customer", new Customer());
        model.addAttribute("isCreateCustomerPage", true);
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
    public String createCustomer(@ModelAttribute("customer") Customer customer,
                                 BindingResult result,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
            logger.info("Entering (POST)createCustomer method");
            logger.debug("Customer data: {}", customer);
            customerService.create(customer, result);
            if (result.hasErrors()) {
                return "createCustomer";
            }
            logger.info("Customer created successfully");
            redirectAttributes.addFlashAttribute("customerCreated", true);
            redirectAttributes.addFlashAttribute("customerName", customer.getFullName());
            return "redirect:/customer/allCustomers";
    }


    /**
     * Updating customer with the given ID.
     *
     * @param  id     the ID of the customer to update
     * @param  model  the model to add the updated customer to
     * @return        the name of the view to render
     */
    @GetMapping("/updateCustomer/{id}")
    public String updateCustomer(@PathVariable("id") Long id,
                                 @RequestParam(name = "locale", required = false) String localeParam,
                                 Model model) {
        logger.info("Updating customer with id: {}", id);
        Locale locale = Locale.getDefault();
        if (localeParam != null) {
            locale=Locale.forLanguageTag(localeParam);
        }
        LocaleContextHolder.setLocale(locale);
        String message = messageSource.getMessage("message.customerForm", null, locale);
        model.addAttribute("message", message);
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
    public String editedCustomer(@ModelAttribute("customer") Customer customer,
                                 BindingResult result,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
            logger.info("Entering (POST)updateCustomer method");
            logger.debug("Customer data: {}", customer);
            customerService.update(customer, result);
            if (result.hasErrors()) {
                return "updateCustomer";
            }
            logger.info("Customer edited successfully");
            redirectAttributes.addFlashAttribute("customerUpdated", true);
            redirectAttributes.addFlashAttribute("customerName", customer.getFullName());
            redirectAttributes.addFlashAttribute("customerId", customer.getId());
            return "redirect:/customer/allCustomers";

    }


    /**
     * Deletes a customer based on the provided ID.
     *
     * @param  id  the ID of the customer to be deleted
     * @return     a redirection to the successDeleteCustomer page
     */
    @GetMapping("/deleteCustomer/{id}")
    public String deleteCustomer(@PathVariable("id") Long id,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        logger.info("Deleting customer with id: {}", id);
        String fullName = customerService.findById(id).getFullName();
        customerService.deleteById(id);
        logger.info("Customer deleted successfully");
        redirectAttributes.addFlashAttribute("customerDeleted", true);
        redirectAttributes.addFlashAttribute("customerName", fullName);
        redirectAttributes.addFlashAttribute("customerId", id);
        return "redirect:/customer/allCustomers";

    }

    @GetMapping("/showCustomer/{id}")
    public String showCustomer(@PathVariable("id") Long id, Model model) {
        CustomerProjectDTO customerProjectDTO = customerService.findCustomerWithProjectDTOById(id);
        model.addAttribute("customerLocation", customerProjectDTO);
        return "showCostumer";
    }
}
