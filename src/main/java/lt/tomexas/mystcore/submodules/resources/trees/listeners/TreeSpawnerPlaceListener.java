package lt.tomexas.mystcore.submodules.resources.trees.listeners;

import com.ticxo.modelengine.api.entity.Dummy;
import lt.tomexas.mystcore.submodules.resources.trees.data.*;
import lt.tomexas.mystcore.managers.EntityManager;
import lt.tomexas.mystcore.managers.ItemManager;
import lt.tomexas.mystcore.managers.PDCManager;
import org.bukkit.*;
import org.bukkit.block.Block;
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

import java.util.List;
import java.util.UUID;

public class TreeSpawnerPlaceListener implements Listener {

    @EventHandler
    public void onTreeSpawnerPlace(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        ItemStack item = event.getItem();
        if (!ItemManager.isSpawnerItem(item)) return;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        event.setCancelled(true);

        String modelId = pdc.get(PDCManager.MODEL_ID, PersistentDataType.STRING);
        Integer respawnTime = pdc.get(PDCManager.RESPAWN_TIME, PersistentDataType.INTEGER);
        Integer glowChance = pdc.get(PDCManager.GLOW_CHANCE, PersistentDataType.INTEGER);
        String skillType = pdc.get(PDCManager.SKILL_TYPE, PersistentDataType.STRING);
        ChopSound chopSound = PDCManager.loadChopSoundFromPDC(pdc);
        List<Skill> skillData = PDCManager.loadSkillsFromPDC(pdc);
        List<Axe> axes = PDCManager.loadAxesFromPDC(pdc);
        List<Drop> drops = PDCManager.loadDropsFromPDC(pdc);

        if (modelId == null || respawnTime == null || glowChance == null || skillType == null) {
            player.sendMessage("§cInvalid tree spawner item data!");
            return;
        }

        Location clickedBlockLocation = event.getClickedBlock().getLocation();
        Location treeLocation = clickedBlockLocation.clone().add(0.5,1,0.5);
        Location location = clickedBlockLocation.clone().add(0.5, 1, 0.5);

        Dummy<?> dummy = EntityManager.spawnTreeModel(treeLocation, modelId);
        UUID uuid = dummy.getUUID();
        TextDisplay textDisplay = EntityManager.spawnTextDisplay(location);

        List<Block> barrierBlocks = EntityManager.spawnBarrierBlocks(clickedBlockLocation);

        new Tree(uuid,
                textDisplay,
                location,
                barrierBlocks,
                modelId,
                respawnTime,
                glowChance,
                skillType,
                chopSound,
                skillData,
                axes,
                drops);

        player.sendMessage("§aTree spawned successfully!");
    }
}
