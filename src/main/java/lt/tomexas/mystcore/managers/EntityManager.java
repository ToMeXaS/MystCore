package lt.tomexas.mystcore.managers;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.entity.Dummy;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import io.papermc.paper.advancement.AdvancementDisplay;
import lt.tomexas.mystcore.Main;
import lt.tomexas.mystcore.PluginLogger;
import lt.tomexas.mystcore.data.MystPlayer;
import lt.tomexas.mystcore.submodules.resources.trees.data.Skill;
import lt.tomexas.mystcore.submodules.resources.trees.data.Tree;
import lt.tomexas.mystcore.other.ProgressBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.RayTraceResult;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EntityManager {

    private static final Main plugin = Main.getInstance();

    private EntityManager() {
        throw new UnsupportedOperationException("EntityManager is a utility class and cannot be instantiated.");
    }

    public static Dummy<?> spawnTreeModel(Location location, String modelId) {
        Dummy<?> dummy = new Dummy<>();
        dummy.setLocation(location);
        //ArmorStand armorStand = location.getWorld().spawn(location, ArmorStand.class);
        //armorStand.setInvisible(true);
        ModeledEntity modeledEntity = ModelEngineAPI.createModeledEntity(dummy);
        ActiveModel activeModel = ModelEngineAPI.createActiveModel(modelId);
        activeModel.setDamageTint(Color.WHITE);
        modeledEntity.addModel(activeModel, true);
        return dummy;
    }

    public static TextDisplay spawnTextDisplay(Location location) {
        location = location.add(0, 2, -1);
        location.setYaw(180f);
        TextDisplay textDisplay = location.getWorld().spawn(location, TextDisplay.class);;
        textDisplay.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
        textDisplay.setShadowed(true);
        textDisplay.setSeeThrough(false);
        textDisplay.setViewRange(0.2f);
        return textDisplay;
    }

    public static List<Block> spawnBarrierBlocks(Location location) {
        List<Block> barrierBlocks = new ArrayList<>();
        World world = location.getWorld();
        if (world == null) return barrierBlocks;

        // Place 3 vertical barrier blocks at the exact location
        for (int y = 0; y < 4; y++) {
            Block block = world.getBlockAt(location.clone().add(0, y, 0));
            if (block.getType() == Material.AIR) {
                block.setType(Material.BARRIER);
                barrierBlocks.add(block);
            }
        }

        return barrierBlocks;
    }

    public static void updateTextDisplay(UUID entityId) {
        Tree tree = Tree.getTreeByUuid(entityId);
        if (tree == null) return;

        TextDisplay textDisplay = tree.getTextDisplay();
        if (textDisplay != null) {
            textDisplay.text(Component.text("§6§l[" + tree.getHarvester().getName() + "]§f\n" + tree.getEntityText()));
        }
    }

    public static void updateHealthDisplay(MystPlayer mystPlayer, UUID entityId, double hits) {
        Tree tree = Tree.getTreeByUuid(entityId);
        if (tree == null) return;
        TextDisplay display = tree.getHealthDisplay();
        Skill skill = mystPlayer.getSkill(tree);
        if (skill == null) {
            PluginLogger.debug("No skill found for tree " + tree + " and type " + tree.getSkillType());
            return;
        }
        double health = skill.health() - hits;
        int maxHealth = (int) skill.health();
        health = Math.max(0, health);
        if (display == null) display = createHealthDisplay(mystPlayer, entityId);
        display.text(Component.text(ProgressBar.createAndReturn(health, maxHealth)));
    }

    private static TextDisplay createHealthDisplay(MystPlayer mystPlayer, UUID entityId) {
        Tree tree = Tree.getTreeByUuid(entityId);
        if (tree == null) return null;

        Player player = mystPlayer.getPlayer();
        double maxDistance = 5.0;
        RayTraceResult result = player.rayTraceBlocks(maxDistance);

        if (result == null || result.getHitBlock() == null) return null;

        BlockFace blockFace = result.getHitBlockFace();
        if (blockFace == null) return null;

        // Calculate yaw for the opposite block face
        BlockFace opposite = blockFace.getOppositeFace();
        float yaw = switch (opposite) {
            case SOUTH -> 180f;
            case EAST  -> 90f;
            case WEST  -> -90f;
            default   -> 0f; // for NORTH and any other
        };

        // Calculate the display location: center of hit block, offset outwards by 0.5 on the face, Y set to tree Y + 1
        Location displayLocation = result.getHitBlock().getLocation().add(0.5, 0.5, 0.5);
        displayLocation.add(blockFace.getDirection().multiply(0.5));
        displayLocation.setYaw(yaw);

        TextDisplay display = tree.getWorld().spawn(displayLocation, TextDisplay.class, textDisplay -> {
            textDisplay.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
            textDisplay.setShadowed(true);
            textDisplay.setSeeThrough(false);
            textDisplay.setViewRange(0.2f);
            int health = (int) mystPlayer.getSkill(tree).health();
            textDisplay.text(Component.text(ProgressBar.createAndReturn(health, health)));
        });

        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            if (!mystPlayer.getPlayer().equals(onlinePlayer))
                onlinePlayer.hideEntity(plugin, display);
        }

        tree.setHealthDisplay(display);
        return display;
    }
}
