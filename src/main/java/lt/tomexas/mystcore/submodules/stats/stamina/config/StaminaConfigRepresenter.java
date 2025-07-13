package lt.tomexas.mystcore.submodules.stats.stamina.config;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.util.LinkedHashMap;
import java.util.Map;

public class StaminaConfigRepresenter extends Representer {

    public StaminaConfigRepresenter(DumperOptions options) {
        super(options);
        this.addClassTag(StaminaConfig.class, Tag.MAP);
        this.representers.put(StaminaConfig.class, data -> {
            StaminaConfig t = (StaminaConfig) data;
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("sprintEnabled", t.isSprintEnabled());
            map.put("sprintCost", t.getSprintCost());
            map.put("blockBreakEnabled", t.isBlockBreakEnabled());
            map.put("blockBreakCost", t.getBlockBreakCost());
            map.put("attackEnabled", t.isAttackEnabled());
            map.put("attackCost", t.getAttackCost());
            return representMapping(Tag.MAP, map, options.getDefaultFlowStyle());
        });
    }

}
