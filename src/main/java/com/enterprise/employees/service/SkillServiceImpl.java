package com.enterprise.employees.service;

import com.enterprise.employees.model.Skill;
import com.enterprise.employees.repository.SkillRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@AllArgsConstructor
public class SkillServiceImpl implements SkillService {

    SkillRepository skillRepository;

    @Override
    public List<Skill> getAllSkills() {
        return skillRepository.findAll();
    }

    @Override
    public List<Skill> getSkillsByIds(List<Long> ids) {
        return skillRepository.findAllById(ids);
    }

    @Override
    public void save(Skill skill) {
        skillRepository.save(skill);
    }


}
