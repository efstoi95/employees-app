package com.enterprise.employees.service;

import com.enterprise.employees.model.Skill;
import com.enterprise.employees.model.SkillDTO;
import com.enterprise.employees.repository.SkillRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SkillServiceImpl implements SkillService {

    SkillRepository skillRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<Skill> getAllSkills() {
        return skillRepository.findAll();
    }

    @Override
    public Skill findById(Long id) {
        return skillRepository.findById(id).orElseThrow(() -> new RuntimeException("Skill not found"));
    }

    @Override
    public List<SkillDTO> getAllSkillsDTO() {
        List<Skill> skills = skillRepository.findAll();
        return skills.stream()
                .map(skill -> modelMapper.map(skill, SkillDTO.class))
                .collect(Collectors.toList());
    }


    @Override
    public List<Skill> getSkillsByIds(List<Long> ids) {
        return skillRepository.findAllById(ids);
    }

    @Override
    public void save(Skill skill) {
        skillRepository.save(skill);
    }

    @Override
    public List<Skill> findSkillsByIds(List<Long> skillsId) {
        return skillRepository.findAllById(skillsId);
    }

    @Override
    public void deleteById(Long id) {
        skillRepository.deleteById(id);
    }


}
