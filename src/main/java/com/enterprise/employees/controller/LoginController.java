package com.enterprise.employees.controller;

import com.enterprise.employees.model.Employee;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class LoginController {

    /**
     * Retrieves the login page.
     *
     * @return the name of the login page
     */
    @GetMapping("/login")
    String login() {
        return "login";
    }

    /**
     * Handles the default behavior after a successful login.
     *
     * @return the URL to redirect the user to based on their role
     */
    @RequestMapping("/")
    public String defaultAfterLogin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth != null){
            Employee employee = (Employee) auth.getPrincipal();
            if(!employee.isVerified()) {
                return "redirect:/web/lockedAccount/" + employee.getId();
            }else if(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                return "redirect:/web/successLogin";
            }
            return "redirect:/web/successUserLogin/" + employee.getId();
        }
        return "redirect:/web/login";
    }

}
