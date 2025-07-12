package lt.tomexas.mystcore.submodules.resources.managers;

import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.items.ItemBuilder;
import lt.tomexas.mystcore.submodules.resources.data.trees.Axe;
import lt.tomexas.mystcore.submodules.resources.data.trees.Drop;
import lt.tomexas.mystcore.submodules.resources.data.trees.Skill;
import lt.tomexas.mystcore.submodules.resources.data.trees.config.TreeSpawner;
import lt.tomexas.mystcore.submodules.resources.utils.PersistentDataUtil;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.stream.Collectors;

public class ItemManager {

    private ItemManager() {
        throw new UnsupportedOperationException("ItemManager is a utility class and cannot be instantiated.");
    }

    /**
     * Creates an ItemStack based on the given configuration.
     *
     * @param config the configuration to load item data from
     * @return the created ItemStack, or null if the configuration is invalid
     */
    public static ItemStack getItemStack(TreeSpawner config) {
        String modelId = config.getModelId();
        int respawnTime = config.getRespawnTime();
        int glowChance = config.getGlowChance();
        String skillType = config.getSkillType();
        List<Skill> skills = config.getSkills();
        List<Axe> axes = config.getAxes();
        List<Drop> drops = config.getDrops();

        if (!isValidConfig(modelId, respawnTime, glowChance, skillType, skills, axes, drops)) {
            return null;
        }

        ItemStack item = new ItemStack(Material.OAK_SAPLING, 1);
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta == null) return null;

        PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();
        savePersistentData(pdc, modelId, respawnTime, glowChance, skillType, skills, axes, drops);

        itemMeta.setEnchantmentGlintOverride(true);
        itemMeta.itemName(Component.text("§e`" + modelId + "` spawner"));
        itemMeta.lore(createItemLore(modelId, drops));

        item.setItemMeta(itemMeta);
        return item;
    }

    /**
     * Creates an ItemStack for a given item name.
     *
     * @param itemName the name of the item
     * @return the created ItemStack, or null if the item name is invalid
     */
    public static ItemStack getItemStack(String itemName, int amount) {
        if (itemName == null || amount <= 0) return null;

        if (itemName.startsWith("minecraft:")) {
            String materialName = itemName.substring("minecraft:".length()).toUpperCase();
            Material material = Material.matchMaterial(materialName);
            if (material == null) {
                throw new IllegalArgumentException("Invalid Minecraft item type: " + materialName);
            }
            return new ItemStack(material, amount);
        }

        if (itemName.startsWith("nexo:")) {
            // Replace with your own way of fetching Nexo items
            String nexoId = itemName.substring("nexo:".length());
            ItemBuilder builder = NexoItems.itemFromId(nexoId);
            if (builder == null) {
                throw new IllegalArgumentException("Invalid Nexo item ID: " + nexoId);
            }
            return builder.setAmount(amount).build();
        }

        if (itemName.startsWith("mmoitems:")) {
            String mmoItemStr = itemName.substring("mmoitems:".length());
            String[] parts = mmoItemStr.split(":");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid MMOItems item format: " + mmoItemStr);
            }
            String mmoItemType = parts[0];
            String mmoItemId = parts[1];
            MMOItem mmoItem = MMOItems.plugin.getMMOItem(MMOItems.plugin.getTypes().get(mmoItemType), mmoItemId);
            if (mmoItem == null) {
                throw new IllegalArgumentException("Invalid MMOItems item type or ID: " + mmoItemStr);
            }
            ItemStack item = mmoItem.newBuilder().build();
            if (item == null) {
                throw new IllegalArgumentException("Failed to create MMOItems item: " + mmoItemStr);
            }
            item.setAmount(amount);
            return item;
        }

        // Unknown namespace
        throw new IllegalArgumentException("Unknown item namespace: " + itemName);
    }

    /**
     * Validates the configuration values.
     *
     * @param modelId     the model ID
     * @param respawnTime the respawn time
     * @param glowChance  the glow chance
     * @param skillType   the skill type
     * @param skills      the list of skills
     * @param axes        the list of axes
     * @param drops       the list of drops
     * @return true if the configuration is valid, false otherwise
     */
    private static boolean isValidConfig(String modelId, int respawnTime, int glowChance, String skillType, List<?> skills, List<?> axes, List<Drop> drops) {
        return modelId != null && !modelId.isEmpty()
                && respawnTime > 0
                && glowChance >= 0
                && skillType != null && !skillType.isEmpty()
                && skills != null && !skills.isEmpty()
                && axes != null && !axes.isEmpty()
                && drops != null && !drops.isEmpty();
    }

    /**
     * Saves persistent data to the given PersistentDataContainer.
     *
     * @param pdc         the PersistentDataContainer to save data to
     * @param modelId     the model ID
     * @param respawnTime the respawn time
     * @param glowChance  the glow chance
     * @param skillType   the skill type
     * @param skills      the list of skills
     * @param axes        the list of axes
     * @param drops       the list of drops
     */
    private static void savePersistentData(PersistentDataContainer pdc, String modelId, int respawnTime, int glowChance, String skillType, List<Skill> skills, List<Axe> axes, List<Drop> drops) {
        pdc.set(PersistentDataUtil.MODEL_ID, PersistentDataType.STRING, modelId);
        pdc.set(PersistentDataUtil.RESPAWN_TIME, PersistentDataType.INTEGER, respawnTime);
        pdc.set(PersistentDataUtil.GLOW_CHANCE, PersistentDataType.INTEGER, glowChance);
        pdc.set(PersistentDataUtil.SKILL_TYPE, PersistentDataType.STRING, skillType);
        PersistentDataUtil.saveSkillsToPDC(pdc, skills);
        PersistentDataUtil.saveAxesToPDC(pdc, axes);
        PersistentDataUtil.saveDropsToPDC(pdc, drops);
    }


    /**
     * Creates the lore for an item.
     *
     * @param modelId the model ID
     * @param drops   the list of drops
     * @return the list of lore components
     */
    private static List<Component> createItemLore(String modelId, List<Drop> drops) {
        return List.of(
                Component.text("§7Right click a block to spawn §e`" + modelId + "`"),
                Component.text(""),
                Component.text("§7Drops: §e" + drops.stream()
                        .map(drop -> "x" + drop.amount() + " " + drop.getItemStack().getType().name())
                        .collect(Collectors.joining(", ")))
        );
    }
}
