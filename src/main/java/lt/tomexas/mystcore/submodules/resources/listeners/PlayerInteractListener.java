package lt.tomexas.mystcore.submodules.resources.listeners;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.entity.Dummy;
import com.ticxo.modelengine.api.entity.Hitbox;
import com.ticxo.modelengine.api.events.BaseEntityInteractEvent;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.nms.entity.HitboxEntity;
import lt.tomexas.mystcore.PluginLogger;
import lt.tomexas.mystcore.submodules.resources.data.trees.Axe;
import lt.tomexas.mystcore.submodules.resources.data.trees.Skill;
import lt.tomexas.mystcore.submodules.resources.data.trees.Tree;
import lt.tomexas.mystcore.submodules.resources.utils.PersistentDataUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerInteractListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        ItemStack item = event.getItem();
        if (!isSpawnerItem(item)) return;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        event.setCancelled(true);

        String modelId = pdc.get(PersistentDataUtil.MODEL_ID, PersistentDataType.STRING);
        Integer respawnTime = pdc.get(PersistentDataUtil.RESPAWN_TIME, PersistentDataType.INTEGER);
        Integer glowChance = pdc.get(PersistentDataUtil.GLOW_CHANCE, PersistentDataType.INTEGER);
        String skillType = pdc.get(PersistentDataUtil.SKILL_TYPE, PersistentDataType.STRING);
        List<Skill> skillData = PersistentDataUtil.loadSkillsFromPDC(pdc);
        List<Axe> axes = PersistentDataUtil.loadAxesFromPDC(pdc);
        List<ItemStack> drops = PersistentDataUtil.loadDropsFromPDC(pdc);

        if (modelId == null || respawnTime == null || glowChance == null || skillType == null) {
            player.sendMessage("§cInvalid tree spawner item data!");
            return;
        }

        Location clickedBlockLocation = event.getClickedBlock().getLocation();
        Location treeLocation = clickedBlockLocation.clone().add(0.5,1,0.5);
        Location location = clickedBlockLocation.clone().add(0.5, 1, 0.5);

        Dummy<?> dummy = spawnTreeModel(treeLocation, modelId);
        UUID uuid = dummy.getUUID();
        TextDisplay textDisplay = spawnTextDisplay(location);

        List<Block> barrierBlocks = spawnBarrierBlocks(clickedBlockLocation);

        new Tree(uuid,
                textDisplay,
                location,
                barrierBlocks,
                modelId,
                respawnTime,
                glowChance,
                skillType,
                skillData,
                axes,
                drops);

        player.sendMessage("§aTree spawned successfully!");
    }

    private boolean isSpawnerItem(ItemStack item) {
        if (item == null || item.getType().isAir() || !item.getType().equals(Material.OAK_SAPLING) || !item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        return pdc.has(PersistentDataUtil.MODEL_ID) &&
                pdc.has(PersistentDataUtil.RESPAWN_TIME, PersistentDataType.INTEGER) &&
                pdc.has(PersistentDataUtil.SKILL_TYPE, PersistentDataType.STRING) &&
                pdc.has(PersistentDataUtil.SKILL_DATA, PersistentDataType.STRING) &&
                pdc.has(PersistentDataUtil.AXES, PersistentDataType.STRING) &&
                pdc.has(PersistentDataUtil.DROPS, PersistentDataType.STRING);
    }

    private Dummy<?> spawnTreeModel(Location location, String modelId) {
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

    private TextDisplay spawnTextDisplay(Location location) {
        location = location.add(0, 2, -1);
        location.setYaw(180f);
        TextDisplay textDisplay = location.getWorld().spawn(location, TextDisplay.class);;
        textDisplay.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
        textDisplay.setShadowed(true);
        textDisplay.setSeeThrough(false);
        textDisplay.setViewRange(0.2f);
        return textDisplay;
    }

    private List<Block> spawnBarrierBlocks(Location location) {
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
}
