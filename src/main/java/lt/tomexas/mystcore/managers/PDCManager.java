package lt.tomexas.mystcore.managers;

import lt.tomexas.mystcore.Main;
import lt.tomexas.mystcore.submodules.resources.trees.data.Axe;
import lt.tomexas.mystcore.submodules.resources.trees.data.ChopSound;
import lt.tomexas.mystcore.submodules.resources.trees.data.Drop;
import lt.tomexas.mystcore.submodules.resources.trees.data.Skill;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class PDCManager {
    private static final Main plugin = Main.getInstance();
    private static final Logger logger = plugin.getLogger();
    public static final NamespacedKey MODEL_ID = new NamespacedKey(Main.getInstance(), "model_id");
    public static final NamespacedKey RESPAWN_TIME = new NamespacedKey(Main.getInstance(), "respawn_time");
    public static final NamespacedKey GLOW_CHANCE = new NamespacedKey(Main.getInstance(), "glow_chance");
    public static final NamespacedKey SKILL_TYPE = new NamespacedKey(Main.getInstance(), "skill_type");
    public static final NamespacedKey CHOP_SOUND = new NamespacedKey(Main.getInstance(), "chop_sound");
    public static final NamespacedKey SKILL_DATA = new NamespacedKey(Main.getInstance(), "skill_data");
    public static final NamespacedKey AXES = new NamespacedKey(Main.getInstance(), "axes");
    public static final NamespacedKey DROPS = new NamespacedKey(Main.getInstance(), "drops");

    public PDCManager() {
        // Constructor
    }

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

    public static void saveDropsToPDC(PersistentDataContainer container, List<Drop> drops) {
        if (drops == null || drops.isEmpty()) {
            container.remove(DROPS); // Remove key if no axes
            return;
        }

        List<String> serializedDrops = new ArrayList<>();
        for (Drop drop : drops) {
            if (drop != null) {
                try {
                    // Serialize each axe (assuming Axe has a custom serialize method)
                    serializedDrops.add(drop.serialize());
                } catch (Exception e) {
                    logger.warning("Failed to serialize Axe: " + e.getMessage());
                }
            }
        }

        if (!serializedDrops.isEmpty()) {
            container.set(DROPS, PersistentDataType.STRING, String.join(";", serializedDrops));
        } else {
            container.remove(DROPS); // Remove key if serialization fails
        }
    }

    public static List<Drop> loadDropsFromPDC(PersistentDataContainer container) {
        String serializedData = container.get(DROPS, PersistentDataType.STRING);
        if (serializedData == null || serializedData.isEmpty()) {
            return new ArrayList<>(); // Return an empty list if no data is found
        }

        List<Drop> drops = new ArrayList<>();
        for (String serializedDrop : serializedData.split(";")) {
            try {
                drops.add(Drop.deserialize(serializedDrop));
            } catch (Exception e) {
                logger.warning("Failed to deserialize Axe: " + e.getMessage());
            }
        }

        return drops;
    }

    public static void saveChopSoundToPDC(PersistentDataContainer container, ChopSound chopSound) {
        if (chopSound == null) {
            container.remove(CHOP_SOUND); // Remove key if no chop sound
            return;
        }

        try {
            String serializedChopSound = chopSound.serialize();
            container.set(CHOP_SOUND, PersistentDataType.STRING, serializedChopSound);
        } catch (Exception e) {
            logger.warning("Failed to serialize ChopSound: " + e.getMessage());
        }
    }

    public static ChopSound loadChopSoundFromPDC(PersistentDataContainer container) {
        String serializedChopSound = container.get(CHOP_SOUND, PersistentDataType.STRING);
        if (serializedChopSound == null || serializedChopSound.isEmpty()) {
            return null; // Return null if no chop sound is found
        }
        return ChopSound.deserialize(serializedChopSound);
    }

}
