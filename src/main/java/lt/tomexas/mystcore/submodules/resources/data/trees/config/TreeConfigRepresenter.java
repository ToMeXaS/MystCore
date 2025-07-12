package lt.tomexas.mystcore.submodules.resources.data.trees.config;

import lt.tomexas.mystcore.submodules.resources.data.trees.Axe;
import lt.tomexas.mystcore.submodules.resources.data.trees.Drop;
import lt.tomexas.mystcore.submodules.resources.data.trees.Skill;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.util.LinkedHashMap;
import java.util.Map;

public class TreeConfigRepresenter extends Representer {

    public TreeConfigRepresenter(DumperOptions options) {
        super(options);
        this.addClassTag(TreeConfig.class, Tag.MAP);
        this.representers.put(TreeConfig.class, data -> {
            TreeConfig t = (TreeConfig) data;
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("modelId", t.getModelId());
            map.put("uses", t.getUses());
            map.put("respawnTime", t.getRespawnTime());
            map.put("glowChance", t.getGlowChance());
            map.put("skillType", t.getSkillType());
            map.put("skillLevelData", t.getSkillLevelData());
            map.put("axes", t.getAxes());
            map.put("drops", t.getDrops());
            return representMapping(Tag.MAP, map, options.getDefaultFlowStyle());
        });
        this.representers.put(Skill.class, data -> {
            Skill s = (Skill) data;
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("level", s.level());
            map.put("experience", s.experience());
            map.put("health", s.health());
            map.put("stamina", s.stamina());
            return representMapping(Tag.MAP, map, options.getDefaultFlowStyle());
        });
        // Axe
        this.representers.put(Axe.class, data -> {
            Axe a = (Axe) data;
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("item", a.item());
            map.put("damage", a.damage());
            map.put("criticalHit", a.criticalHit());
            return representMapping(Tag.MAP, map, options.getDefaultFlowStyle());
        });
        // Drop
        this.representers.put(Drop.class, data -> {
            Drop d = (Drop) data;
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("item", d.item());
            map.put("amount", d.amount());
            return representMapping(Tag.MAP, map, options.getDefaultFlowStyle());
        });
    }

}
