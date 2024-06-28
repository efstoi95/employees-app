package com.enterprise.employees.controller;

import com.enterprise.employees.controller.web.CostumerController;
import jakarta.mail.MessagingException;
import org.hibernate.boot.MappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.thymeleaf.exceptions.TemplateProcessingException;

import java.io.IOException;

@ControllerAdvice
public class GlobalExceptionHandler  {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle TemplateProcessingException and add error message to the model.
     *
     * @param  e     the TemplateProcessingException that occurred
     * @return       the view name for errorEmpLogin
     */
    @ExceptionHandler(TemplateProcessingException.class)
    public String handleTemplateProcessingException(TemplateProcessingException e) {
        logger.error("Template processing error occurred: {}", e.getMessage());
        ModelAndView modelAndView = new ModelAndView("error");
        modelAndView.addObject("error", "An error occurred while processing your request. Please try again later. " + e.getMessage());
        return "error";
    }
    /**
     * Handle IOException and MessagingException and add error message to the model.
     *
     * @param  e     the exception that occurred
     * @param  model the model to add the error message to
     * @return       the view name for error
     */
    @ExceptionHandler({IOException.class, MessagingException.class})
    public String handleIOExceptionAndMessagingException(Exception e, Model model) {
        if(e instanceof IOException) {
            logger.error("IO error occurred: {}", e.getMessage());
        } else if(e instanceof MessagingException) {
            logger.error("Messaging error occurred: {}", e.getMessage());
        }
       ModelAndView modelAndView = new ModelAndView("error");
       modelAndView.addObject("error", "An error occurred. Please try again later. " + e.getMessage());
       return "error";
    }
   // Handle General Exception
    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception e, Model model) {
        logger.error("General error occurred: {}", e.getMessage());
        ModelAndView modelAndView = new ModelAndView("error");
        modelAndView.addObject("error", "An error occurred. Please try again later. " + e.getMessage());
        return "error";
    }
    // Handle IllegalArgumentException
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(IllegalArgumentException e, Model model) {
        logger.error("Illegal argument error occurred: {}", e.getMessage());
        ModelAndView modelAndView = new ModelAndView("error");
        modelAndView.addObject("error", "Illegal argument error occurred: " + e.getMessage());
        return "error";
    }

    // Handle NoHandlerFoundException
    @ExceptionHandler(NoHandlerFoundException.class)
    public ModelAndView handleNoHandlerFoundException(NoHandlerFoundException ex) {
        logger.error("No handler found error occurred: {}", ex.getMessage());
        ModelAndView modelAndView = new ModelAndView("error");
        modelAndView.addObject("error", "No handler found error occurred: " + ex.getMessage());
        return modelAndView;
    }

    // Handle MappingException
    @ExceptionHandler(MappingException.class)
    public ModelAndView handleMappingException(MappingException ex) {
        logger.error("Mapping error occurred: {}", ex.getMessage());
        ModelAndView modelAndView = new ModelAndView("error");
        modelAndView.addObject("error", "Mapping error occurred: " + ex.getMessage());
        return modelAndView;
    }

    //Handle RuntimeException
    @ExceptionHandler(RuntimeException.class)
    public ModelAndView handleRuntimeException(RuntimeException ex) {
        logger.error("Runtime error occurred: {}", ex.getMessage());
        ModelAndView modelAndView = new ModelAndView("errorRuntime");
        modelAndView.addObject("error",  ex.getMessage());
        return modelAndView;
    }
}
