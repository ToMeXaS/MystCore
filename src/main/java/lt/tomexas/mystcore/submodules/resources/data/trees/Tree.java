package lt.tomexas.mystcore.submodules.resources.data.trees;

import lombok.Getter;
import lombok.Setter;
import lt.tomexas.mystcore.PluginLogger;
import lt.tomexas.mystcore.submodules.resources.data.interfaces.AxeRequirementHolder;
import lt.tomexas.mystcore.submodules.resources.data.interfaces.SkillRequirementHolder;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class Tree implements SkillRequirementHolder, AxeRequirementHolder {

    private static final Map<UUID, Tree> REGISTRY = new HashMap<>();

    // Tree properties
    private final UUID uuid;
    private TextDisplay textDisplay;
    private TextDisplay healthDisplay;

    private Player harvester;
    private final World world;
    private final Location location;
    private final List<Block> barrierBlocks;
    private String entityText;
    private boolean chopped = false;

    // Tree configuration
    private final String modelId;
    private int respawnTime;
    private int glowChance;
    private String skillType;
    private List<Skill> skillData;
    private List<Axe> axes;
    private List<Drop> drops;

    public Tree(@NotNull UUID uuid,
                TextDisplay textDisplay,
                Location location,
                List<Block> barrierBlocks,
                String modelId,
                int respawnTime,
                int glowChance,
                String skillType,
                List<Skill> skillData,
                List<Axe> axes,
                List<Drop> drops) {
        this.uuid = uuid;
        this.textDisplay = textDisplay;

        this.world = location.getWorld();
        this.location = location;
        this.barrierBlocks = barrierBlocks;

        this.modelId = modelId;
        this.respawnTime = respawnTime;
        this.glowChance = glowChance;
        this.skillType = skillType;
        this.skillData = skillData;
        this.axes = axes;
        this.drops = drops;

        this.entityText = getDisplayText(this.world);
        REGISTRY.put(uuid, this);
    }

    public static Map<UUID, Tree> getAllTrees() {
        return REGISTRY;
    }

    public static boolean exists(UUID uuid) {
        return REGISTRY.containsKey(uuid);
    }

    public boolean remove() {
        if (!exists(this.uuid)) {
            PluginLogger.debug("Tree with UUID " + this.uuid + " does not exist in the registry.");
            return false;
        }
        UUID uuid = this.getUuid();
        REGISTRY.remove(uuid);
        removeTextDisplay();
        removeHealthDisplay();
        return true;
    }

    public void removeTextDisplay() {
        if (this.textDisplay != null) {
            this.textDisplay.remove();
            this.textDisplay = null;
        }
    }

    public void removeHealthDisplay() {
        if (this.healthDisplay != null) {
            this.healthDisplay.remove();
            this.healthDisplay = null;
            PluginLogger.info("Removed health display for tree " + this.getName(this.modelId) + " at " + this.location.toVector().toString() + ".");
        }

    }

    public static Tree getTreeByUuid(UUID uuid) {
        if (!exists(uuid)) {
            PluginLogger.debug("Tree with UUID " + uuid + " is not registered in Tree registry.");
            return null;
        }
        return REGISTRY.get(uuid);
    }

    public static Tree getByBlock(Block block) {
        if (block == null || block.getType() != Material.BARRIER) return null;
        return REGISTRY.values().stream()
                .filter(tree -> tree.getBarrierBlocks().contains(block))
                .findFirst()
                .orElseGet(() -> {
                    PluginLogger.debug("No tree found for the given block.");
                    return null;
                });
    }

    public static Tree getByRayTraceResult(RayTraceResult result) {
        if (result == null || result.getHitBlock() == null) return null;
        Block block = result.getHitBlock();
        return getByBlock(block);
    }

    private String getName(String modelName) {
        if (modelName == null || modelName.isEmpty()) {
            return modelName; // Return as is if null or empty
        }
        // Remove "_tree" and capitalize the first letter
        String name = modelName.replace("_tree", "");
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    private String getDisplayText(World world) {
        if (world != null) {
            Entity entity = world.getEntity(this.textDisplay.getUniqueId());
            if (entity instanceof TextDisplay textEntity) {
                String capitalizedSkillType = getSkillType().substring(0, 1).toUpperCase() + getSkillType().substring(1).toLowerCase();
                String text = "§l" + getName(modelId) + "§r\n" + capitalizedSkillType + " (Lvl. " + getSkillData().getFirst().level() + ")";
                textEntity.text(Component.text(text));
                entityText = text;
            }
        }
        return entityText;
    }

    @Override
    public List<Skill> getSkillData() {
        return this.skillData;
    }

    @Override
    public String getSkillType() {
        return this.skillType;
    }

    @Override
    public List<Axe> getAxes() {
        return this.axes;
    }
}
