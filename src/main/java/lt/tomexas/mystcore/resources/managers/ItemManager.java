package lt.tomexas.mystcore.resources.managers;

import lt.tomexas.mystcore.resources.data.trees.Axe;
import lt.tomexas.mystcore.resources.data.trees.Skill;
import lt.tomexas.mystcore.resources.utils.PersistentDataUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
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
    public static ItemStack getItemStack(FileConfiguration config) {
        String modelId = config.getString("model_id");
        int respawnTime = config.getInt("respawn_time", -1);
        int glowChance = config.getInt("glow_chance", -1);
        String skillType = config.getString("skill_type");
        List<Skill> skills = PersistentDataUtil.loadSkillsFromConfig(config);
        List<Axe> axes = PersistentDataUtil.loadAxesFromConfig(config);
        List<ItemStack> drops = PersistentDataUtil.loadDropsFromConfig(config);

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
    public static ItemStack getItemStack(String itemName) {
        if (!itemName.contains("minecraft:")) return null;
        Material material = Material.matchMaterial(itemName.substring("minecraft:".length()));
        if (material == null) {
            throw new IllegalArgumentException("Invalid axe type: " + itemName);
        }
        return ItemStack.of(material);
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
    private static boolean isValidConfig(String modelId, int respawnTime, int glowChance, String skillType, List<?> skills, List<?> axes, List<ItemStack> drops) {
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
    private static void savePersistentData(PersistentDataContainer pdc, String modelId, int respawnTime, int glowChance, String skillType, List<Skill> skills, List<Axe> axes, List<ItemStack> drops) {
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
    private static List<Component> createItemLore(String modelId, List<ItemStack> drops) {
        return List.of(
                Component.text("§7Right click a block to spawn §e`" + modelId + "`"),
                Component.text(""),
                Component.text("§7Drops: §e" + drops.stream()
                        .map(drop -> "x" + drop.getAmount() + " " + drop.getType().name())
                        .collect(Collectors.joining(", ")))
        );
    }
}
