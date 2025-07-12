package lt.tomexas.mystcore.submodules.resources.data.trees.config;

import lt.tomexas.mystcore.submodules.resources.data.trees.Axe;
import lt.tomexas.mystcore.submodules.resources.data.trees.Drop;
import lt.tomexas.mystcore.submodules.resources.data.trees.Skill;
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
        TypeDescription spawnerDesc = new TypeDescription(TreeSpawner.class);
        spawnerDesc.putListPropertyType("axes", Axe.class);
        spawnerDesc.putListPropertyType("skills", Skill.class);
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
                    asInt(raw.get("damage")),
                    asInt(raw.get("criticalHit"))
            );
        }
        if (node.getType() == Skill.class) {
            Map<Object, Object> raw = constructMapping((MappingNode) node);
            return new Skill(
                    (String) raw.get("type"),
                    asInt(raw.get("level")),
                    asInt(raw.get("experience")),
                    asInt(raw.get("health")),
                    asInt(raw.get("stamina"))
            );
        }
        if (node.getType() == Drop.class) {
            Map<Object, Object> raw = constructMapping((MappingNode) node);
            return new Drop(
                    (String) raw.get("item"),
                    asInt(raw.get("amount"))
            );
        }
        return super.constructObject(node);
    }

    private int asInt(Object val) {
        if (val instanceof Integer) return (Integer) val;
        if (val instanceof Number) return ((Number) val).intValue();
        if (val instanceof String) return Integer.parseInt((String) val);
        throw new IllegalArgumentException("Cannot convert to int: " + val);
    }

}
