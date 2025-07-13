package lt.tomexas.mystcore.submodules.stats.stamina.listeners;

import lt.tomexas.mystcore.Main;
import lt.tomexas.mystcore.data.MystPlayer;
import lt.tomexas.mystcore.submodules.stats.stamina.config.StaminaConfig;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class BlockBreakListener implements Listener {

    private final Main plugin = Main.getInstance();
    private final StaminaConfig config = plugin.getStaminaConfig();

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        MystPlayer mystPlayer = MystPlayer.getMystPlayer(player);
        if (mystPlayer == null) return;
        PlayerData playerData = mystPlayer.getPlayerData();

        if (!config.isBlockBreakEnabled()) return;

        // Check if the player has enough stamina
        if (playerData.getStamina() < config.getBlockBreakCost()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 999999, 5, false, false));
            return;
        }
        playerData.setStamina(playerData.getStamina() - config.getBlockBreakCost());
    }
}
