package lt.tomexas.mystcore.submodules.resources.data.interfaces;

import lt.tomexas.mystcore.submodules.resources.data.trees.Skill;

import java.util.List;

public interface SkillRequirementHolder {
    List<Skill> getSkillData();
    String getSkillType();
}
