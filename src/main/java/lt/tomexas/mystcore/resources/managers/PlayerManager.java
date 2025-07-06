package lt.tomexas.mystcore.resources.managers;

import lt.tomexas.mystcore.resources.data.trees.Axe;
import lt.tomexas.mystcore.resources.data.trees.Skill;
import lt.tomexas.mystcore.resources.data.trees.Tree;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Comparator;
import java.util.List;

public class PlayerManager {

    /**
     * Private constructor to prevent instantiation.
     * This class is a utility class and should not be instantiated.
     */
    private PlayerManager() {
        throw new UnsupportedOperationException("This class should not be instantiated directly.");
    }

    public static boolean hasRequiredAxe(Tree tree, Player player) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        List<Axe> axes = tree.getAxes();
        if (axes.isEmpty()) return false;
        Axe axe = axes.stream()
                .filter(a -> a.getItemStack().isSimilar(itemInHand))
                .findFirst()
                .orElse(null);
        if (axe == null) {
            player.sendMessage("§cYou need to hold at least a §4" + axes.getFirst().getItemStack().getType() + "§c to chop the tree!");
            player.setVelocity(player.getLocation().getDirection().multiply(-0.5).setY(0.3));
            return false;
        }

        return true;
    }

    public static boolean hasRequiredLevel(Tree tree, Player player) {
        PlayerData playerData = PlayerData.get(player.getUniqueId());
        Skill skill = tree.getSkillData().stream()
                .min(Comparator.comparingInt(Skill::level))
                .orElse(null);
        if (skill == null) return false;
        String skillName = tree.getSkillType();
        String capitalizedSkillName = skillName.substring(0, 1).toUpperCase() + skillName.substring(1);
        int level = playerData.getCollectionSkills().getLevel(skillName);
        if (level < skill.level()) {
            player.sendMessage("§cYou need to be at least §4" + capitalizedSkillName + " (Lvl. " + skill.level() + ")§c to chop this tree!");
            player.setVelocity(player.getLocation().getDirection().multiply(-0.5).setY(0.3));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
            return false;
        }

        return true;
    }

    public static boolean hasRequiredStamina(Tree tree, Player player) {
        PlayerData playerData = PlayerData.get(player.getUniqueId());
        Skill skill = tree.getSkillData().stream()
                .max(Comparator.comparingInt(Skill::level))
                .orElse(null);
        if (skill == null) return false;
        double stamina = playerData.getStamina();
        if (stamina < skill.stamina()) {
            player.sendMessage("§cYou need at least §4" + skill.stamina() + " stamina §cto chop this tree!");
            player.setVelocity(player.getLocation().getDirection().multiply(-0.5).setY(0.3));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
            return false;
        }

        return true;
    }

}
