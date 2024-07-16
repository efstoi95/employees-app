package com.enterprise.employees.controller.web;

import com.enterprise.employees.model.ResourceDTO;
import com.enterprise.employees.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/resources")
@Controller
public class ResourceController {

    private final ResourceService resourceService;
    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);

    @GetMapping("/allResources")
    public String getAllResources(Model model) {
        logger.info("Getting all resources");
        List<ResourceDTO> resources = resourceService.findAllDTO();
        model.addAttribute("resources", resources);
        return "allResources";
    }

    @GetMapping("/createResource")
    public String createResource(Model model) {
        logger.info("Creating new resource");
        model.addAttribute("resource", new ResourceDTO());
        return "createResource";
    }

    @PostMapping("/createdResource")
    public String createdResource(ResourceDTO resourceDTO, Model model, BindingResult bindingResult) {
        logger.info("Created new resource");
        resourceService.create(resourceDTO,bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("resource", new ResourceDTO());
            return "createResource";
        }
        logger.info("Resource created successfully");
        return "redirect:/resources/successCreateResource";
    }

    @GetMapping("/editResource/{id}")
    public String editResource(@PathVariable("id") Long id, Model model) {
        logger.info("Editing resource");
        ResourceDTO resource = resourceService.findByIdDTO(id);
        model.addAttribute("resource", resource);
        return "editResource";
    }

    @PostMapping("/editedResource")
    public String editedResource(@ModelAttribute("resource") ResourceDTO resourceDTO, Model model, BindingResult bindingResult) {
        logger.info("Edited resource");
        resourceService.edit(resourceDTO,bindingResult);
        if (bindingResult.hasErrors()) {
            logger.warn("Validation error encountered while editing employee: {}", bindingResult.getAllErrors());
            model.addAttribute("resource", resourceDTO);
            return "createResource";
        }
        logger.info("Resource edited successfully");
        return "redirect:/resources/successEditResource";
    }

    @GetMapping("/deleteResource/{id}")
    public String deleteResource(@PathVariable("id") Long id, Model model) {
        logger.info("Deleting resource");
        resourceService.delete(id);
        logger.info("Resource deleted successfully");
        return "redirect:/resources/successDeleteResource";
    }

    @GetMapping("/successCreateResource")
    public String successCreateResource(Model model) {
        model.addAttribute("message", "New resource created");
        logger.info("The information of the resource created");
        return "successResource";
    }

    @GetMapping("/successEditResource")
    public String successEditResource(Model model) {
        model.addAttribute("message", "Resource edited");
        logger.info("The information of the resource edited");
        return "successResource";
    }

    @GetMapping("/successDeleteResource")
    public String successDeleteResource(Model model) {
        model.addAttribute("message", "Resource deleted");
        logger.info("The information of the resource deleted");
        return "successResource";
    }
}
