package com.enterprise.employees.service;

import com.enterprise.employees.model.Skill;
import com.enterprise.employees.model.SkillDTO;

import java.util.List;

public interface SkillService  {

    List<Skill> getAllSkills();

    List<SkillDTO> getAllSkillsDTO();

    List<Skill> getSkillsByIds(List<Long> ids);

    void save(Skill skill);

    List<Skill> findSkillsByIds(List<Long> skillsId);
}
