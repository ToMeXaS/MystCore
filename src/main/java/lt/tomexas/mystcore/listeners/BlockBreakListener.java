package lt.tomexas.mystcore.listeners;

import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class BlockBreakListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.get(player);

        // Check if the player has enough stamina
        if (playerData.getStamina() < 3) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 999999, 5, false, false));
            return;
        }
        playerData.setStamina(playerData.getStamina() - 3);
    }
}
