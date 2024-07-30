package com.enterprise.employees.controller.web;

import com.enterprise.employees.model.Employee;
import com.enterprise.employees.service.EmployeeService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Locale;

@RequiredArgsConstructor
@RequestMapping("/web")
@Controller
public class VerificationController {

    private static final Logger logger = LoggerFactory.getLogger(VerificationController.class);
    private final EmployeeService employeeService;
    @Autowired
    private MessageSource messageSource;
    /**
     * Show the verification page.
     *
     * @param  id    description of parameter
     * @param  model description of parameter
     * @return       description of return value
     */
    @GetMapping("/verify/{id}")
    public String showVerificationPage(@PathVariable("id") Long id, Model model) {
        logger.info("Entering (GET)showVerificationPage method");
        Employee employee = employeeService.getEmployeeById(id);
        model.addAttribute("employee", employee);
        return "addVerifyCode";
    }

    /**
     * Check if the entered verification code matches the stored one.
     *
     * @param  verifiedCode  the verification code entered by the user
     * @param  employee       the employee object for verification
     * @param  model          the model object for the view
     * @return               the view name after verification process
     */
    @PostMapping("/verified")
    public String verifyCode(@Validated @RequestParam("verifiedCode") String verifiedCode,
                             @ModelAttribute("employee") Employee employee, Model model, BindingResult bindingResult) {
        logger.info("Entering (POST) verifyCode method with code: {}", verifiedCode);
        logger.info("Employee ID: {}", employee.getId());
        employeeService.updateEmployee(employee, bindingResult);
        if (bindingResult.hasErrors()) {
            logger.warn("Incorrect verification code entered for employee ID: {}", employee.getId());
            return "addVerifyCode"; // Return to the verification page with error message
        }
        logger.info("Verification successful for employee ID: {}", employee.getId());
        // Redirect to success page
        return "redirect:/web/success";



    }
    /**
     * Sends a verification code to the employee with the specified ID.
     *
     * @param  id    the ID of the employee to send the verification code to
     * @param  model the model containing attributes for the view
     * @return       the redirection URL for verifying the sent code
     */
    @PostMapping("/sendCode/{id}")
    public String sendCode(@PathVariable("id") Long id, Model model) throws MessagingException, IOException {
            Employee employee = employeeService.getEmployeeById(id);
            employeeService.generateAndSendPassword(employee);
            model.addAttribute("employee", employee);
            model.addAttribute("message", "A new verification code has been sent to your email.");
            return "redirect:/web/verify/" + id;
    }
    /**
     * Retrieves the locked account information and adds it to the model.
     *
     * @param  id    the ID of the locked account
     * @param  model the model to hold the locked account information
     * @return       the view name to display the locked account information
     */
    @GetMapping("/lockedAccount/{id}")
    public String lockedAccount(@PathVariable("id") Long id, Model model) {
            model.addAttribute("employeeId", id);
            logger.info("Your account is locked.");
            return "lockedAccount";
    }

    /**
     * Retrieves the success message and adds it to the model for the admin user.
     *
     * @param  model  the model to hold the success message
     * @return        the view name to display the success message for the admin user
     */
    @Secured("ROLE_ADMIN")
    @GetMapping("/successLogin")
    public String successLogin(@RequestParam(name = "locale", required = false) String localeParam,
                               Model model) {

        Locale locale = Locale.getDefault();
        if (localeParam != null) {
            locale=Locale.forLanguageTag(localeParam);
        }
        LocaleContextHolder.setLocale(locale);
        String message = messageSource.getMessage("message.menuEmployee", null, locale);
        model.addAttribute("message", message);
        model.addAttribute("message", "You logged in");
        model.addAttribute("isSuccessLoginPage", true);
        logger.info("The employee logged in.");
        return "successLogin";
    }
}
