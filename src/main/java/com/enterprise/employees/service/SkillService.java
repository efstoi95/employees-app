package com.enterprise.employees.service;

import com.enterprise.employees.model.Skill;

import java.util.List;

public interface SkillService  {

    List<Skill> getAllSkills();
     List<Skill> getSkillsByIds(List<Long> ids);

    void save(Skill skill);
}
