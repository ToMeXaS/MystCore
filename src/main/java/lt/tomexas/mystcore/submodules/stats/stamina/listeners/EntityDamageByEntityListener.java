package lt.tomexas.mystcore.submodules.stats.stamina.listeners;

import lt.tomexas.mystcore.Main;
import lt.tomexas.mystcore.data.MystPlayer;
import lt.tomexas.mystcore.submodules.stats.stamina.config.StaminaConfig;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EntityDamageByEntityListener implements Listener {

    private final Main plugin = Main.getInstance();
    private final StaminaConfig config = plugin.getStaminaConfig();

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            if (player.hasPermission("mystcore.bypass.stamina") && config.isEnableBypass()) return;
            if (!config.isAttackEnabled()) return;
            MystPlayer mystPlayer = MystPlayer.getMystPlayer(player);
            if (mystPlayer == null) return;
            PlayerData playerData = mystPlayer.getPlayerData();

            double staminaCost = getStaminaCost(player);

            if (playerData == null) return;
            if (playerData.getStamina() < staminaCost) {
                event.setDamage(0);
            }

            playerData.setStamina(playerData.getStamina() - staminaCost); // Decrease stamina
        }
    }

    public double getStaminaCost(Player player) {
        if (!config.isEnablePermissionDrain()) return config.getAttackCost();
        Pattern pattern = Pattern.compile("mystcore.stamina.attack.drain\\.(\\d+)");
        for (PermissionAttachmentInfo perm : player.getEffectivePermissions()) {
            Matcher matcher = pattern.matcher(perm.getPermission());
            if (matcher.find()) {
                return Double.parseDouble(matcher.group(1));
            }
        }
        return config.getAttackCost();
    }
}
