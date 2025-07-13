package lt.tomexas.mystcore.submodules.stats.stamina.listeners;

import lt.tomexas.mystcore.Main;
import lt.tomexas.mystcore.data.MystPlayer;
import lt.tomexas.mystcore.submodules.stats.stamina.config.StaminaConfig;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EntityDamageByEntityListener implements Listener {

    private final Main plugin = Main.getInstance();
    private final StaminaConfig config = plugin.getStaminaConfig();

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            if (player.hasPermission("mystcore.bypass.stamina") && config.isEnableBypass()) return;
            MystPlayer mystPlayer = MystPlayer.getMystPlayer(player);
            if (mystPlayer == null) return;
            PlayerData playerData = mystPlayer.getPlayerData();

            if (playerData == null) return;
            if (playerData.getStamina() < config.getAttackCost()) {
                event.setDamage(0);
            }

            playerData.setStamina(playerData.getStamina() - config.getAttackCost()); // Decrease stamina
        }
    }
}
