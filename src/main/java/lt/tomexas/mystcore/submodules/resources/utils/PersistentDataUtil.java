package lt.tomexas.mystcore.submodules.resources.utils;

import lt.tomexas.mystcore.Main;
import lt.tomexas.mystcore.submodules.resources.data.trees.Axe;
import lt.tomexas.mystcore.submodules.resources.data.trees.Skill;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class PersistentDataUtil {
    private static final Main plugin = Main.getInstance();
    private static final Logger logger = plugin.getLogger();
    public static final NamespacedKey MODEL_ID = new NamespacedKey(Main.getInstance(), "model_id");
    public static final NamespacedKey RESPAWN_TIME = new NamespacedKey(Main.getInstance(), "respawn_time");
    public static final NamespacedKey GLOW_CHANCE = new NamespacedKey(Main.getInstance(), "glow_chance");
    public static final NamespacedKey SKILL_TYPE = new NamespacedKey(Main.getInstance(), "skill_type");
    public static final NamespacedKey SKILL_DATA = new NamespacedKey(Main.getInstance(), "skill_data");
    public static final NamespacedKey AXES = new NamespacedKey(Main.getInstance(), "axes");
    public static final NamespacedKey DROPS = new NamespacedKey(Main.getInstance(), "drops");

    public PersistentDataUtil() {
        // Constructor
    }

    // Save a list of Skills to PersistentDataContainer
    public static void saveSkillsToPDC(PersistentDataContainer container, List<Skill> skills) {
        if (skills == null || skills.isEmpty()) {
            container.remove(SKILL_DATA); // Remove key if no skills
            return;
        }

        List<String> serializedSkills = new ArrayList<>();
        for (Skill skill : skills) {
            if (skill != null) {
                try {
                    // Serialize each skill (assuming Skill has a custom serialize method)
                    serializedSkills.add(skill.serialize());
                } catch (Exception e) {
                    logger.warning("Failed to serialize Skill: " + e.getMessage());
                }
            }
        }

        if (!serializedSkills.isEmpty()) {
            container.set(SKILL_DATA, PersistentDataType.STRING, String.join(";", serializedSkills));
        } else {
            container.remove(SKILL_DATA); // Remove key if serialization fails
        }
    }

    // Load a list of Skills from PersistentDataContainer
    public static List<Skill> loadSkillsFromPDC(PersistentDataContainer container) {
        String serializedData = container.get(SKILL_DATA, PersistentDataType.STRING);
        if (serializedData == null || serializedData.isEmpty()) {
            return new ArrayList<>(); // Return an empty list if no data is found
        }

        List<Skill> skills = new ArrayList<>();
        for (String serializedSkill : serializedData.split(";")) {
            try {
                skills.add(Skill.deserialize(serializedSkill));
            } catch (Exception e) {
                logger.warning("Failed to deserialize Skill: " + e.getMessage());
            }
        }

        return skills;
    }

    // Load a list of Skills from FileConfiguration
    public static List<Skill> loadSkillsFromConfig(FileConfiguration config) {
        List<Skill> skills = new ArrayList<>();
        ConfigurationSection skillsSection = config.getConfigurationSection("skill_data");

        if (skillsSection != null) {
            for (String key : skillsSection.getKeys(false)) {
                String type = skillsSection.getString(key + ".type", "woodcutting"); // Default to empty string if not specified
                int level = skillsSection.getInt(key + ".level", 1); // Default level to 1 if not specified
                double experience = skillsSection.getDouble(key + ".experience", 0.0); // Default experience to 0.0
                double health = skillsSection.getDouble(key + ".health", 0.0); // Default health to 0.0
                double stamina = skillsSection.getDouble(key + ".stamina", 0.0); // Default stamina to 0.0

                if (level > 0) {
                    skills.add(new Skill(type, level, experience, health, stamina));
                } else {
                    logger.warning("Skill level is missing for key: " + key);
                }
            }
        }

        return skills;
    }

    // Save a list of Axes to PersistentDataContainer
    public static void saveAxesToPDC(PersistentDataContainer container, List<Axe> axes) {
        if (axes == null || axes.isEmpty()) {
            container.remove(AXES); // Remove key if no axes
            return;
        }

        List<String> serializedAxes = new ArrayList<>();
        for (Axe axe : axes) {
            if (axe != null) {
                try {
                    // Serialize each axe (assuming Axe has a custom serialize method)
                    serializedAxes.add(axe.serialize());
                } catch (Exception e) {
                    logger.warning("Failed to serialize Axe: " + e.getMessage());
                }
            }
        }

        if (!serializedAxes.isEmpty()) {
            container.set(AXES, PersistentDataType.STRING, String.join(";", serializedAxes));
        } else {
            container.remove(AXES); // Remove key if serialization fails
        }
    }

    // Load a list of Axes from PersistentDataContainer
    public static List<Axe> loadAxesFromPDC(PersistentDataContainer container) {
        String serializedData = container.get(AXES, PersistentDataType.STRING);
        if (serializedData == null || serializedData.isEmpty()) {
            return new ArrayList<>(); // Return an empty list if no data is found
        }

        List<Axe> axes = new ArrayList<>();
        for (String serializedAxe : serializedData.split(";")) {
            try {
                axes.add(Axe.deserialize(serializedAxe));
            } catch (Exception e) {
                logger.warning("Failed to deserialize Axe: " + e.getMessage());
            }
        }

        return axes;
    }

    // Load a list of Axes from FileConfiguration
    public static List<Axe> loadAxesFromConfig(FileConfiguration config) {
        List<Axe> axes = new ArrayList<>();
        ConfigurationSection axesSection = config.getConfigurationSection("axes");

        if (axesSection != null) {
            for (String key : axesSection.getKeys(false)) {
                String itemType = axesSection.getString(key + ".type", ""); // Default to empty string if not specified
                int damage = axesSection.getInt(key + ".damage", 0); // Default damage to 0
                int criticalHit = axesSection.getInt(key + ".critical_hit", 0); // Default health to 0.0

                if (!itemType.isEmpty()) {
                    axes.add(new Axe(itemType, damage, criticalHit));
                } else {
                    logger.warning("Axe itemType is missing for key: " + key);
                }
            }
        }

        return axes;
    }

    // Save a list of ItemStacks to PersistentDataContainer
    public static void saveDropsToPDC(PersistentDataContainer container, List<ItemStack> items) {
        if (items == null || items.isEmpty()) {
            container.remove(DROPS); // Remove key if no items
            return;
        }

        List<String> serializedItems = new ArrayList<>();
        for (ItemStack item : items) {
            if (item != null) {
                try {
                    YamlConfiguration config = new YamlConfiguration();
                    config.set("item", item);
                    serializedItems.add(config.saveToString());
                } catch (Exception e) {
                    System.err.println("Failed to serialize ItemStack: " + e.getMessage());
                }
            }
        }

        if (!serializedItems.isEmpty()) {
            container.set(DROPS, PersistentDataType.STRING, String.join(";", serializedItems));
        } else {
            container.remove(DROPS); // Remove key if serialization fails
        }
    }

    public static List<ItemStack> loadDropsFromPDC(PersistentDataContainer container) {
        String serializedData = container.get(DROPS, PersistentDataType.STRING);
        if (serializedData == null || serializedData.isEmpty()) {
            return new ArrayList<>(); // Return an empty list if no data is found
        }

        List<ItemStack> items = new ArrayList<>();
        for (String serializedItem : serializedData.split(";")) {
            YamlConfiguration config = new YamlConfiguration();
            try {
                config.loadFromString(serializedItem);
                ItemStack item = config.getItemStack("item");
                if (item != null) {
                    items.add(item);
                }
            } catch (Exception e) {
                System.err.println("Failed to deserialize ItemStack: " + e.getMessage());
            }
        }

        return items;
    }

    // Load a list of ItemStacks from PersistentDataContainer
    public static List<ItemStack> loadDropsFromConfig(FileConfiguration config) {
        List<ItemStack> drops = new ArrayList<>();
        ConfigurationSection dropsSection = config.getConfigurationSection("drops");

        if (dropsSection != null) {
            for (String key : dropsSection.getKeys(false)) {
                String type = dropsSection.getString(key + ".type");
                int amount = dropsSection.getInt(key + ".amount", 1); // Default to 1 if not specified


                if (type != null && type.startsWith("minecraft:")) {
                    String materialName = type.substring("minecraft:".length());
                    Material material = Material.matchMaterial(materialName);
                    if (material != null) {
                        drops.add(new ItemStack(material, amount));
                    } else {
                        System.err.println("Invalid material type: " + type);
                    }
                } else {
                    System.err.println("Unsupported type: " + type);
                }
            }
        }

        return drops;
    }

}
