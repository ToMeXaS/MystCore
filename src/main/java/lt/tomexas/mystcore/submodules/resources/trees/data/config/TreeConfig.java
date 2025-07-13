package lt.tomexas.mystcore.submodules.resources.trees.data.config;

import lombok.Getter;
import lombok.Setter;
import lt.tomexas.mystcore.submodules.resources.trees.data.Axe;
import lt.tomexas.mystcore.submodules.resources.trees.data.ChopSound;
import lt.tomexas.mystcore.submodules.resources.trees.data.Drop;
import lt.tomexas.mystcore.submodules.resources.trees.data.Skill;

import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class TreeConfig {

    private String modelId = "oak_tree";
    private int uses = 1;
    private int respawnTime = 30;
    private int glowChance = 20;
    private String skillType = "woodcutting";
    private ChopSound chopSound = new ChopSound("block.wood.chop3", 1.0f, 1.0f);
    private List<Axe> axes = Arrays.asList(
            new Axe("minecraft:wooden_axe", 1D, 5D),
            new Axe("minecraft:stone_axe", 5D, 10D)
    );
    private List<Skill> skillLevelData = Arrays.asList(
            new Skill(1, 10D, 50D, 50D),
            new Skill(5, 50D, 30D, 30D)
    );
    private List<Drop> drops = Arrays.asList(
            new Drop("minecraft:oak_log", 1),
            new Drop("minecraft:apple", 2)
    );
}
