package lt.tomexas.mystcore.resources.data.trees;

import com.ticxo.modelengine.api.ModelEngineAPI;
import lombok.Getter;
import lombok.Setter;
import lt.tomexas.mystcore.Main;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

@Getter
public class Tree {

    private static final Main plugin = Main.getInstance();
    private static final Logger logger = plugin.getLogger();
    private static final Map<UUID, Tree> REGISTRY = new HashMap<>();

    // Tree properties
    private final UUID uuid;
    private final UUID textEntityId;
    @Setter
    private UUID healthEntityId;

    @Setter
    private Player harvester;
    private final World world;
    private final Location location;
    private final List<Block> barrierBlocks;
    private String entityText;
    @Setter
    private boolean chopped = false;

    // Tree configuration
    private final String modelId;
    @Setter
    private int respawnTime;
    private final int glowChance;
    private final String skillType;
    private final List<Skill> skillData;
    private final List<Axe> axes;
    private final List<ItemStack> drops;

    public Tree(@NotNull UUID uuid,
                UUID textEntityId,
                Location location,
                List<Block> barrierBlocks,
                String modelId,
                int respawnTime,
                int glowChance,
                String skillType,
                List<Skill> skillData,
                List<Axe> axes,
                List<ItemStack> drops) {
        this.uuid = uuid;
        this.textEntityId = textEntityId;

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

    public static Tree getTree(UUID uuid) {
        if (!hasTree(uuid)) {
            logger.warning("Tree with UUID " + uuid + " is not registered in Tree registry.");
            return null;
        }
        return REGISTRY.get(uuid);
    }

    public static Map<UUID, Tree> getAllTrees() {
        return REGISTRY;
    }

    public static boolean hasTree(UUID uuid) {
        return REGISTRY.containsKey(uuid);
    }

    public static Tree getByBlock(Block block) {
        if (block == null || !block.getType().equals(Material.BARRIER)) {
            logger.warning("Block is null or not a barrier block.");
            return null;
        }
        for (Tree tree : REGISTRY.values()) {
            if (tree.getBarrierBlocks().contains(block)) {
                return tree;
            }
        }
        return null;
    }

    public static void removeTree(UUID uuid) {
        Tree tree = REGISTRY.remove(uuid);
        if (tree == null) {
            logger.warning("Tree with UUID " + uuid + " is not registered in Tree registry.");
            return;
        }

        // Remove the armor stand entity
        World world = tree.getWorld();
        if (world != null) {
            Entity textEntity = world.getEntity(tree.getTextEntityId());
            if (textEntity != null) {
                textEntity.remove();
            }
        }
        ModelEngineAPI.getModeledEntity(uuid).markRemoved();

        // Remove the barrier blocks if they exist
        List<Block> barrierBlocks = tree.getBarrierBlocks();
        if (barrierBlocks != null && !barrierBlocks.isEmpty()) {
            Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                for (Block block : barrierBlocks) {
                    block.setType(Material.AIR);
                }
            });
        }

        // Remove the tree from the database
        plugin.getResourcesDatabase().removeTree(uuid);
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
            Entity entity = world.getEntity(textEntityId);
            if (entity instanceof TextDisplay textEntity) {
                String capitalizedSkillType = skillType.substring(0, 1).toUpperCase() + skillType.substring(1).toLowerCase();
                String text = "§l" + getName(modelId) + "§r\n" + capitalizedSkillType + " (Lvl. " + skillData.getFirst().level() + ")";
                textEntity.text(Component.text(text));
                entityText = text;
            }
        }
        return entityText;
    }
}
