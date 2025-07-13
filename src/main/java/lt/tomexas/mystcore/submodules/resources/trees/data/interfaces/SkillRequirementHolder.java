package lt.tomexas.mystcore.submodules.resources.trees.data.interfaces;

import lt.tomexas.mystcore.submodules.resources.trees.data.Skill;

import java.util.List;

public interface SkillRequirementHolder {
    List<Skill> getSkillData();
    String getSkillType();
}
