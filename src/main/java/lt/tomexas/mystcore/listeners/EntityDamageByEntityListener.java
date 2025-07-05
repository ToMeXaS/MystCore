package lt.tomexas.mystcore.listeners;

import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EntityDamageByEntityListener implements Listener {

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            PlayerData playerData = PlayerData.get(player);

            if (playerData == null) return;
            if (playerData.getStamina() < 3) {
                event.setDamage(0); // Set damage to 0
            }

            playerData.setStamina(playerData.getStamina() - 3); // Decrease stamina
        }
    }
}
