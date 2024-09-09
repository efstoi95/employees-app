package com.enterprise.employees.controller.web;

import com.enterprise.employees.model.*;
import com.enterprise.employees.repository.FileRepository;
import com.enterprise.employees.service.FileStorageService;
import com.enterprise.employees.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RequestMapping("/resources")
@Controller
public class ResourceController {

    private final ResourceService resourceService;
    private final FileStorageService fileStorageService;
    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private MessageSource messageSource;



    @GetMapping("/allResources")
    public String getAllResources(@RequestParam(name = "locale", required = false) String localeParam,
                                  Model model) {
        logger.info("Getting all resources");
        Locale locale = Locale.getDefault();
        if (localeParam != null) {
            locale=Locale.forLanguageTag(localeParam);
        }
        LocaleContextHolder.setLocale(locale);
        String message = messageSource.getMessage("allResources.title", null, locale);
        model.addAttribute("resourceCreatedMessage", messageSource.getMessage("resourceCreatedMessage", null, locale));
        model.addAttribute("resourceUpdatedMessage", messageSource.getMessage("resourceUpdatedMessage", null, locale));
        model.addAttribute("resourceDeletedMessage", messageSource.getMessage("resourceDeletedMessage", null, locale));

        model.addAttribute("message", message);
        List<ResourceDTO> resources = resourceService.findAllDTO();
        model.addAttribute("isAllResourcesPage", true);
        model.addAttribute("resources", resources);
        return "allResources";
    }

    @GetMapping("/createResource")
    public String createResource(@RequestParam(name = "locale", required = false) String localeParam,
                                 Model model) {
        logger.info("Creating new resource");
        Locale locale = Locale.getDefault();
        if (localeParam != null) {
            locale = Locale.forLanguageTag(localeParam);
        }
        LocaleContextHolder.setLocale(locale);
        String message = messageSource.getMessage("createResource.title", null, locale);
        model.addAttribute("message", message);
        model.addAttribute("resource", new ResourceDTO());
        model.addAttribute("isCreateResourcePage", true);
        return "createResource";
    }

    @PostMapping("/createdResource")
    public String createdResource(ResourceDTO resourceDTO,
                                  Model model,
                                  BindingResult bindingResult,
                                  RedirectAttributes redirectAttributes) {
        logger.info("Created new resource");
        resourceService.create(resourceDTO,bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("resource", new ResourceDTO());
            return "createResource";
        }
        logger.info("Resource created successfully");
        redirectAttributes.addFlashAttribute("resourceCreated", true);
        redirectAttributes.addFlashAttribute("resourceId", resourceDTO.getId());
        redirectAttributes.addFlashAttribute("resourceName", resourceDTO.getName());
        return "redirect:/resources/allResources";
    }

    @GetMapping("/editResource/{id}")
    public String editResource(@PathVariable("id") Long id,
                               @RequestParam(name = "locale", required = false) String localeParam,
                               Model model) {
        logger.info("Editing resource");
        Locale locale = Locale.getDefault();
        if (localeParam != null) {
            locale = Locale.forLanguageTag(localeParam);
        }
        LocaleContextHolder.setLocale(locale);
        String message = messageSource.getMessage("editResource.title", null, locale);
        model.addAttribute("message", message);
        ResourceDTO resource = resourceService.findByIdDTO(id);
        model.addAttribute("resource", resource);
        return "editResource";
    }

    @PostMapping("/editedResource")
    public String editedResource(@ModelAttribute("resource") ResourceDTO resourceDTO,
                                 Model model,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        logger.info("Edited resource");
        resourceService.edit(resourceDTO,bindingResult);
        if (bindingResult.hasErrors()) {
            logger.warn("Validation error encountered while editing employee: {}", bindingResult.getAllErrors());
            model.addAttribute("resource", resourceDTO);
            return "createResource";
        }
        logger.info("Resource edited successfully");
        redirectAttributes.addFlashAttribute("resourceUpdated", true);
        redirectAttributes.addFlashAttribute("resourceId", resourceDTO.getId());
        redirectAttributes.addFlashAttribute("resourceName", resourceDTO.getName());
        return "redirect:/resources/allResources";
    }

    @GetMapping("/deleteResource/{id}")
    public String deleteResource(@PathVariable("id") Long id,
                                 RedirectAttributes redirectAttributes) {
        logger.info("Deleting resource");
        String resourceName = resourceService.findById(id).getName();
        resourceService.delete(id);
        logger.info("Resource deleted successfully");
        redirectAttributes.addFlashAttribute("resourceDeleted", true);
        redirectAttributes.addFlashAttribute("resourceId", id);
        redirectAttributes.addFlashAttribute("resourceName", resourceName);
        return "redirect:/resources/allResources";
    }

    @GetMapping("/showResourceFiles/{resourceId}")
    public String showFiles(@PathVariable("resourceId") Long resourceId,
                            @RequestParam(name = "locale", required = false) String localeParam,
                            Model model) {
        Locale locale = Locale.getDefault();
        if (localeParam != null) {
            locale = Locale.forLanguageTag(localeParam);
        }
        LocaleContextHolder.setLocale(locale);
        String message=messageSource.getMessage("message.ResourceFiles", null, locale);
        model.addAttribute("message", message);
        Resource resource = resourceService.findById(resourceId);
        model.addAttribute("resource", resource);
        model.addAttribute("fileNames", resource.getFiles().stream().map(File::getFileName).collect(Collectors.toList()));
        return "showResourceFiles";
    }
    @GetMapping("/viewFile/{resourceId}/{fileName}")
    public ResponseEntity<byte[]> viewFile(@PathVariable Long resourceId, @PathVariable String fileName, Model model) {
        Resource resource = resourceService.findById(resourceId);
        File file = resource.getFiles().stream()
                .filter(f -> f.getFileName().equals(fileName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid file name: " + fileName));


        HttpHeaders headers = new HttpHeaders();
        if (fileName.endsWith(".pdf")) {
            headers.add(HttpHeaders.CONTENT_TYPE, "application/pdf");
        } else if (fileName.endsWith(".txt")) {
            headers.add(HttpHeaders.CONTENT_TYPE, "text/plain; charset=UTF-8");
        } else if (fileName.endsWith(".png")) {
            headers.add(HttpHeaders.CONTENT_TYPE, "image/png");
        }  else if (fileName.endsWith(".jpg")) {
            headers.add(HttpHeaders.CONTENT_TYPE, "image/jpg");
        }else {
            throw new IllegalArgumentException("Unsupported file type: " + fileName);
        }
        return ResponseEntity.ok()
                .headers(headers)
                .body(file.getFileContent());

    }

    @PostMapping("/upload/{resourceId}")
    public String uploadResourceFile(@PathVariable Long resourceId,
                                               @RequestParam("file") MultipartFile[] files,
                                               Model model,
                                               RedirectAttributes redirectAttributes) throws IOException {

        //Check the size
        for(MultipartFile file : files) {
            if (file.isEmpty()) {
                Resource resource = resourceService.findById(resourceId);
                return handleFileError(model, resource, "File is empty. Please enter a valid file");
            }
            byte[] fileContent = file.getBytes();
            if (fileContent.length > 5 * 1024 * 1024) {
                Resource resource = resourceService.findById(resourceId);
                return handleFileError(model, resource, "File size exceeds maximum allowed size of 5MB");
            }
        }
        logger.info("Uploading file to resource with ID: {}", resourceId);
        fileStorageService.uploadResourceFile(resourceId, files);
        redirectAttributes.addFlashAttribute("fileUploaded", true);
        redirectAttributes.addFlashAttribute("resourceId", resourceId);
        redirectAttributes.addFlashAttribute("resourceName", resourceService.findById(resourceId).getName());
        redirectAttributes.addFlashAttribute("fileName", files[0].getOriginalFilename());
        return "redirect:/resources/allResources";
    }

    private String handleFileError(Model model, Resource project, String errorMessage) {
        model.addAttribute("error", errorMessage);
        model.addAttribute("project", project);
        model.addAttribute("fileNames", project.getFiles().stream().map(File::getFileName).collect(Collectors.toList()));
        return "showResourceFiles";
    }

    @GetMapping("/downloadFile/{resourceId}/{fileName}")
    public ResponseEntity<ByteArrayResource> downloadTaskDescriptionFile(@PathVariable Long resourceId, @PathVariable String fileName, Model model) {
        logger.info("Downloading file from resource with ID: {}", resourceId);
       Resource resource = resourceService.findById(resourceId);
        File file = resource.getFiles().stream()
                .filter(f -> f.getFileName().equals(fileName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid file name: " + fileName));
        byte[] fileContent = file.getFileContent();
        ByteArrayResource resourced = new ByteArrayResource(fileContent);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resourced);

    }

    @PostMapping("/deleteFile/{resourceId}/{fileId}")
    public String deleteFile(@PathVariable Long resourceId,
                             @PathVariable Long fileId,
                             RedirectAttributes redirectAttributes) {
        logger.info("Deleting file from resource with ID: {}", resourceId);
        Resource resource = resourceService.findById(resourceId);

        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid file ID: " + fileId));
        String fileName = file.getFileName();


        if (!resource.getFiles().contains(file)) {
            throw new IllegalArgumentException("Invalid file ID: " + fileId);
        }

        resource.getFiles().remove(file);
        resourceService.save(resource);

        file.getResources().remove(resource);

        if (file.getResources().isEmpty()) {
            fileRepository.deleteById(fileId);
        } else {
            fileRepository.save(file);
        }

        redirectAttributes.addFlashAttribute("fileDeleted", true);
        redirectAttributes.addFlashAttribute("resourceId", resourceId);
        redirectAttributes.addFlashAttribute("resourceName", resource.getName());
        redirectAttributes.addFlashAttribute("fileName", fileName);

        return "redirect:/resources/allResources";
    }

}
