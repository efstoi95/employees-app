package com.enterprise.employees.controller.web;
import com.enterprise.employees.model.Skill;
import com.enterprise.employees.repository.SkillRepository;
import com.enterprise.employees.service.SkillServiceImpl;
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
public class SkillController {
    private static final Logger logger = LoggerFactory.getLogger(SkillController.class);
    private final SkillServiceImpl skillService;
    @Autowired
    private SkillRepository skillRepository;
    @Autowired
    private MessageSource messageSource;

    @GetMapping("/allSkills")
    @Secured("ROLE_ADMIN")
    public String showAllSkills(@RequestParam(name = "locale", required = false) String localeParam,
                                     Model model) {
        logger.info("View all Skills");
        Locale locale = Locale.getDefault();
        if (localeParam != null) {
            locale=Locale.forLanguageTag(localeParam);
        }
        LocaleContextHolder.setLocale(locale);
        String message = messageSource.getMessage("allSkills.title", null, locale);
        model.addAttribute("message", message);
        List<Skill> skills = (List<Skill>) skillService.getAllSkills();
        model.addAttribute("isAllSkillsPage", true);
        model.addAttribute("skills", skills);
        return "allSkills";
    }

    @GetMapping("/createSkill")
    @Secured("ROLE_ADMIN")
    public String createSkill(@RequestParam(name = "locale", required = false) String localeParam,
                                   Model model) {
        logger.info("Creating new Skill");
        Locale locale = Locale.getDefault();
        if (localeParam != null) {
            locale=Locale.forLanguageTag(localeParam);
        }
        LocaleContextHolder.setLocale(locale);
        String message = messageSource.getMessage("allSkills.title", null, locale);
        model.addAttribute("message", message);
        model.addAttribute("skill", new Skill());
        model.addAttribute("isCreateSkillPage", true);
        return "createSkill";
    }

    @PostMapping("/createdSkill")
    public String createdSkill(@Validated Skill skill,
                                    BindingResult bindingResult,
                                    RedirectAttributes redirectAttributes,
                                    Model model) {
        logger.info("Created new Skill");
        if(skillRepository.existsByName(skill.getName())) {
            bindingResult.rejectValue("name", "error.skill", "Skill with this name already exists");
            model.addAttribute("skill", skill);
            model.addAttribute("isCreateSkillPage", true);
            return "createSkill";
        }
        skillService.save(skill);
        redirectAttributes.addFlashAttribute("skillCreated", true);
        redirectAttributes.addFlashAttribute("skillName", skill.getName());
        return "redirect:/web/allSkills";
    }
    @GetMapping("/editSkill/{id}")
    @Secured("ROLE_ADMIN")
    public String editSkill(@PathVariable("id") Long id, Model model) {
        logger.info("Editing Skill");
        Skill skill = skillService.findById(id);
        model.addAttribute("skill", skill);
        return "editSkill";
    }
    /**
     * A method to handle the editing of a skill.
     *
     * @param  skill               the Skill object to be edited
     * @param  bindingResult       the BindingResult object to hold validation errors
     * @param  redirectAttributes  the RedirectAttributes object to add flash attributes for redirection
     * @param  model               the Model object to hold the skill and page information
     * @return                     the view to redirect after editing the skill
     */
    @PostMapping("/editedSkill")
    public String editedSkill(@Validated Skill skill,
                                    BindingResult bindingResult,
                                    RedirectAttributes redirectAttributes,
                                    Model model) {
        logger.info("Edited Skill");
        if(skillRepository.existsByName(skill.getName()) && !skill.getName().equals(skillService.findById(skill.getId()).getName())) {
            bindingResult.rejectValue("name", "error.skill", "Skill with this name already exists");
            model.addAttribute("skill", skill);
            model.addAttribute("isEditSkillPage", true);
            return "editSkill";
        }
        skillService.save(skill);
        redirectAttributes.addFlashAttribute("skillEdited", true);
        redirectAttributes.addFlashAttribute("skillId", skill.getId());
        redirectAttributes.addFlashAttribute("skillName", skill.getName());
        return "redirect:/web/allSkills";
    }


}
