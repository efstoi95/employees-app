package com.enterprise.employees.controller.web;

import com.enterprise.employees.model.Department;
import com.enterprise.employees.repository.DepartmentRepository;
import com.enterprise.employees.service.DepartmentServiceImpl;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
@RequestMapping("/web")
@Controller
public class DepartmentController {
    private static final Logger logger = LoggerFactory.getLogger(DepartmentController.class);
    private final DepartmentServiceImpl departmentService;
    @Autowired
    private MessageSource messageSource;
    @Autowired
    private DepartmentRepository departmentRepository;

    /**
     * Retrieves all departments and displays them.
     *
     * @param  localeParam the locale parameter for language setting
     * @param  model       the model for adding attributes
     * @return             the view name for displaying all departments
     */
    @GetMapping("/allDepartments")
    @Secured("ROLE_ADMIN")
    public String showAllDepartments(@RequestParam(name = "locale", required = false) String localeParam,
                                     Model model) {
        logger.info("View all Departments");
        Locale locale = Locale.getDefault();
        if (localeParam != null) {
            locale=Locale.forLanguageTag(localeParam);
        }
        LocaleContextHolder.setLocale(locale);
        String message = messageSource.getMessage("allDepartments.title", null, locale);
        model.addAttribute("message", message);
        List<Department> departments = (List<Department>) departmentService.findAll();
        model.addAttribute("isAllDepartmentsPage", true);
        model.addAttribute("departments", departments);
        return "allDepartments";
    }

    @GetMapping("/createDepartment")
    @Secured("ROLE_ADMIN")
    public String createDepartment(@RequestParam(name = "locale", required = false) String localeParam,
                                   Model model) {
        logger.info("Creating new Department");
        Locale locale = Locale.getDefault();
        if (localeParam != null) {
            locale=Locale.forLanguageTag(localeParam);
        }
        LocaleContextHolder.setLocale(locale);
        String message = messageSource.getMessage("allDepartments.title", null, locale);
        model.addAttribute("message", message);
        model.addAttribute("department", new Department());
        model.addAttribute("isCreateDepartmentPage", true);
        return "createDepartment";
    }

    /**
     * Created new Department
     *
     * @param  department           description of parameter
     * @param  bindingResult        description of parameter
     * @param  redirectAttributes   description of parameter
     * @param  model                description of parameter
     * @return                     description of return value
     */
    @PostMapping("/createdDepartment")
    public String createdDepartment(@Validated Department department,
                                    BindingResult bindingResult,
                                    RedirectAttributes redirectAttributes,
                                    Model model) {
        logger.info("Created new Department");
        if(departmentRepository.existsByName(department.getName())) {
            bindingResult.rejectValue("name", "error.department", "Department with this name already exists");
            model.addAttribute("department", department);
            model.addAttribute("isCreateDepartmentPage", true);
            return "createDepartment";
        }
        departmentService.save(department);
        redirectAttributes.addFlashAttribute("departmentCreated", true);
        redirectAttributes.addFlashAttribute("departmentName", department.getName());
        return "redirect:/web/allDepartments";
    }
    /**
     * Edit a department based on the provided ID.
     *
     * @param  id    the ID of the department to edit
     * @param  model the model to which attributes are added
     * @return       the view name for editing the department
     */
    @GetMapping("/editDepartment/{id}")
    @Secured("ROLE_ADMIN")
    public String editDepartment(@PathVariable Long id, Model model) {
        logger.info("Editing Department with ID: {}", id);
        model.addAttribute("department", departmentService.findById(id));
        model.addAttribute("isEditDepartmentPage", true);
        return "editDepartment";
    }
    /**
     * Updated Department with ID.
     *
     * @param  department           description of parameter
     * @param  bindingResult        description of parameter
     * @param  redirectAttributes   description of parameter
     * @param  model                description of parameter
     * @return                     description of return value
     */
    @PostMapping("/editedDepartment")
    public String updatedDepartment(@Validated Department department,
                                    BindingResult bindingResult,
                                    RedirectAttributes redirectAttributes,
                                    Model model) {
        logger.info("Updated Department with ID: {}", department.getId());
        if(departmentRepository.existsByName(department.getName()) && !department.getName().equals(departmentService.findById(department.getId()).getName())) {
            bindingResult.rejectValue("name", "error.department", "Department with this name already exists");
            model.addAttribute("department", department);
            model.addAttribute("isEditedDepartmentPage", true);
            return "editDepartment";
        }
        departmentService.save(department);
        redirectAttributes.addFlashAttribute("departmentUpdated", true);
        redirectAttributes.addFlashAttribute("departmentName", department.getName());
        return "redirect:/web/allDepartments";
    }
}
