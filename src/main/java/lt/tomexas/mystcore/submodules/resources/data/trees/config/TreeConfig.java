package lt.tomexas.mystcore.submodules.resources.data.trees.config;

import lombok.Getter;
import lombok.Setter;
import lt.tomexas.mystcore.submodules.resources.data.interfaces.ModelIdentifiable;
import lt.tomexas.mystcore.submodules.resources.data.trees.Axe;
import lt.tomexas.mystcore.submodules.resources.data.trees.Drop;
import lt.tomexas.mystcore.submodules.resources.data.trees.Skill;

import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class TreeConfig implements ModelIdentifiable {

    private String modelId = "oak_tree";
    private int uses = 1;
    private int respawnTime = 30;
    private int glowChance = 20;
    private String skillType = "woodcutting";
    private List<Axe> axes = Arrays.asList(
            new Axe("minecraft:wooden_axe", 1, 5),
            new Axe("minecraft:stone_axe", 5, 10)
    );
    private List<Skill> skillLevelData = Arrays.asList(
            new Skill(1, 10, 50, 50),
            new Skill(5, 50, 30, 30)
    );
    private List<Drop> drops = Arrays.asList(
            new Drop("minecraft:oak_log", 1),
            new Drop("minecraft:apple", 2)
    );

    @Override
    public String getModelId() {
        return modelId;
    }
}
