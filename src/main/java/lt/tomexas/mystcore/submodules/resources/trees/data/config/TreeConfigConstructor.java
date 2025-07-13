package lt.tomexas.mystcore.submodules.resources.trees.data.config;

import lt.tomexas.mystcore.other.NumParser;
import lt.tomexas.mystcore.submodules.resources.trees.data.Axe;
import lt.tomexas.mystcore.submodules.resources.trees.data.ChopSound;
import lt.tomexas.mystcore.submodules.resources.trees.data.Drop;
import lt.tomexas.mystcore.submodules.resources.trees.data.Skill;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;

import java.util.Map;

public class TreeConfigConstructor extends Constructor {

    public TreeConfigConstructor(Class<?> theRoot, LoaderOptions options) {
        super(theRoot, options);

        // Register property types for TreeSpawner
        TypeDescription spawnerDesc = new TypeDescription(TreeConfig.class);
        spawnerDesc.putListPropertyType("axes", Axe.class);
        spawnerDesc.putListPropertyType("chopSound", ChopSound.class);
        spawnerDesc.putListPropertyType("skillLevelData", Skill.class);
        spawnerDesc.putListPropertyType("drops", Drop.class);

        this.addTypeDescription(spawnerDesc);
    }

    @Override
    protected Object constructObject(Node node) {
        // This is the key override: when the node represents an Axe, Skill, or Drop mapping, use our custom logic!
        if (node.getType() == Axe.class) {
            Map<Object, Object> raw = constructMapping((MappingNode) node);
            return new Axe(
                    (String) raw.get("item"),
                    NumParser.asDouble(raw.get("damage")),
                    NumParser.asDouble(raw.get("criticalHit"))
            );
        }
        if (node.getType() == Skill.class) {
            Map<Object, Object> raw = constructMapping((MappingNode) node);
            return new Skill(
                    NumParser.asInt(raw.get("level")),
                    NumParser.asDouble(raw.get("experience")),
                    NumParser.asDouble(raw.get("health")),
                    NumParser.asDouble(raw.get("stamina"))
            );
        }
        if (node.getType() == Drop.class) {
            Map<Object, Object> raw = constructMapping((MappingNode) node);
            return new Drop(
                    (String) raw.get("item"),
                    NumParser.asInt(raw.get("amount"))
            );
        }
        if (node.getType() == ChopSound.class) {
            Map<Object, Object> raw = constructMapping((MappingNode) node);
            return new ChopSound(
                    (String) raw.get("type"),
                    NumParser.asFloat(raw.get("volume")),
                    NumParser.asFloat(raw.get("pitch"))
            );
        }
        return super.constructObject(node);
    }

}
