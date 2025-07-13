package lt.tomexas.mystcore.submodules.stats.stamina.tasks;

import lt.tomexas.mystcore.Main;
import lt.tomexas.mystcore.data.MystPlayer;
import lt.tomexas.mystcore.submodules.stats.stamina.config.StaminaConfig;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdatePlayerStamina extends BukkitRunnable {

    private final Main plugin = Main.getInstance();
    private final StaminaConfig config = plugin.getStaminaConfig();

    @Override
    public void run() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (player.hasPermission("mystcore.bypass.stamina") && config.isEnableBypass()) return;
            MystPlayer mystPlayer = MystPlayer.getMystPlayer(player);
            if (mystPlayer == null) return;
            PlayerData playerData = mystPlayer.getPlayerData();

            if (!config.isSprintEnabled()) return;

            double staminaCost = getStaminaCost(player);

            if (player.isSprinting()) {
                if (playerData.getStamina() >= staminaCost) {
                    playerData.setStamina(playerData.getStamina() - staminaCost);
                } else {
                    player.setFoodLevel(4);
                }
            }

            if (playerData.getStamina() > staminaCost) {
                player.setFoodLevel(20);
                player.removePotionEffect(PotionEffectType.MINING_FATIGUE);
            }
        });
    }

    public double getStaminaCost(Player player) {
        if (!config.isEnablePermissionDrain()) return config.getSprintCost();
        Pattern pattern = Pattern.compile("mystcore.stamina.sprint.drain\\.(\\d+)");
        for (PermissionAttachmentInfo perm : player.getEffectivePermissions()) {
            Matcher matcher = pattern.matcher(perm.getPermission());
            if (matcher.find()) {
                return Double.parseDouble(matcher.group(1));
            }
        }
        return config.getSprintCost();
    }
}
