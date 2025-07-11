package lt.tomexas.mystcore.data;

import lombok.Getter;
import lombok.Setter;
import lt.tomexas.mystcore.Main;
import lt.tomexas.mystcore.PluginLogger;
import lt.tomexas.mystcore.submodules.resources.data.interfaces.AxeRequirementHolder;
import lt.tomexas.mystcore.submodules.resources.data.interfaces.SkillRequirementHolder;
import lt.tomexas.mystcore.submodules.resources.data.trees.Axe;
import lt.tomexas.mystcore.submodules.resources.data.trees.Skill;
import lt.tomexas.mystcore.submodules.resources.data.trees.Tree;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

@Getter
public class MystPlayer {

    private static final Map<UUID, MystPlayer> REGISTRY = new HashMap<>();

    private final Player player;
    private final PlayerData playerData;
    @Setter
    private List<OfflinePlayer> islandMembers;

    public MystPlayer(@NotNull Player player, @NotNull PlayerData playerData) {
        this.player = player;
        this.playerData = playerData;
        REGISTRY.put(player.getUniqueId(), this);
    }

    public static MystPlayer getMystPlayer(Player player) {
        if (!REGISTRY.containsKey(player.getUniqueId())) {
            PluginLogger.debug("Player " + player.getName() + " is not registered in MystPlayer registry.");
            return null;
        }
        return REGISTRY.get(player.getUniqueId());
    }

    public UUID getUuid() {
        return player.getUniqueId();
    }

    public Skill getSkill(SkillRequirementHolder obj, String skillType) {
        return obj.getSkillData().stream()
                .filter(s -> s.type().equalsIgnoreCase(skillType))
                .findFirst()
                .orElse(null);
    }

    public Axe getAxe(AxeRequirementHolder obj, ItemStack itemInHand) {
        return obj.getAxes().stream()
                .filter(a -> a.getItemStack().isSimilar(itemInHand))
                .findFirst()
                .orElse(null);
    }

    public boolean hasRequiredLevel(SkillRequirementHolder obj) {
        Skill skill = obj.getSkillData().stream()
                .min(Comparator.comparingInt(Skill::level))
                .orElse(null);
        if (skill == null) return false;
        String skillName = obj.getSkillType();
        String capitalizedSkillName = skillName.substring(0, 1).toUpperCase() + skillName.substring(1);
        int level = this.playerData.getCollectionSkills().getLevel(skillName);
        if (level < skill.level()) {
            this.player.sendMessage("§cYou need to be at least §4" + capitalizedSkillName + " (Lvl. " + skill.level() + ")§c to chop this tree!");
            this.player.setVelocity(this.player.getLocation().getDirection().multiply(-0.5).setY(0.3));
            this.player.playSound(this.player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
            return false;
        }

        return true;
    }

    public boolean hasRequiredAxe(AxeRequirementHolder obj) {
        ItemStack itemInHand = this.player.getInventory().getItemInMainHand();
        List<Axe> axes = obj.getAxes();
        if (axes.isEmpty()) return false;
        Axe axe = axes.stream()
                .filter(a -> a.getItemStack().isSimilar(itemInHand))
                .findFirst()
                .orElse(null);
        if (axe == null) {
            this.player.sendMessage("§cYou need to hold at least a §4" + axes.getFirst().getItemStack().getType() + "§c to chop the tree!");
            this.player.setVelocity(this.player.getLocation().getDirection().multiply(-0.5).setY(0.3));
            return false;
        }

        return true;
    }

    public boolean hasRequiredStamina(SkillRequirementHolder obj) {
        Skill skill = obj.getSkillData().stream()
                .max(Comparator.comparingInt(Skill::level))
                .orElse(null);
        if (skill == null) return false;
        double stamina = this.playerData.getStamina();
        if (stamina < skill.stamina()) {
            this.player.sendMessage("§cYou need at least §4" + skill.stamina() + " stamina §cto chop this tree!");
            this.player.setVelocity(this.player.getLocation().getDirection().multiply(-0.5).setY(0.3));
            this.player.playSound(this.player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
            return false;
        }

        return true;
    }

}
