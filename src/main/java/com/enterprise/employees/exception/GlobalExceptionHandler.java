package com.enterprise.employees.exception;

import com.enterprise.employees.model.Department;
import com.enterprise.employees.model.Employee;
import com.enterprise.employees.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@RequiredArgsConstructor
@ControllerAdvice
public class GlobalExceptionHandler {

    private final EmployeeService employeeService;
    /**
     * Handles the FieldTooLongException by displaying a ModelAndView with an error message.
     *
     * @param  e  the FieldTooLongException that was thrown
     * @return    the ModelAndView with the error message
     */
    @ExceptionHandler(FieldTooLongException.class)
    public ModelAndView handleFieldTooLongException(FieldTooLongException e) {
        ModelAndView mav = new ModelAndView("great-error");
        mav.addObject("title", "Too long Field Exception");
        mav.addObject("message", e.getLocalizedMessage());
        return mav;
    }
}
