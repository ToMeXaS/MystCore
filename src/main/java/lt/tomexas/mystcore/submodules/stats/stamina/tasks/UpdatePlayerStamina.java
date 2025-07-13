package lt.tomexas.mystcore.submodules.stats.stamina.tasks;

import lt.tomexas.mystcore.Main;
import lt.tomexas.mystcore.data.MystPlayer;
import lt.tomexas.mystcore.submodules.stats.stamina.config.StaminaConfig;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class UpdatePlayerStamina extends BukkitRunnable {

    private final Main plugin = Main.getInstance();
    private final StaminaConfig config = plugin.getStaminaConfig();

    @Override
    public void run() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            MystPlayer mystPlayer = MystPlayer.getMystPlayer(player);
            if (mystPlayer == null) return;
            PlayerData playerData = mystPlayer.getPlayerData();

            if (!config.isSprintEnabled()) return;

            if (player.isSprinting()) {
                if (playerData.getStamina() >= config.getSprintCost()) {
                    playerData.setStamina(playerData.getStamina() - config.getSprintCost());
                } else {
                    player.setFoodLevel(4);
                }
            }

            if (playerData.getStamina() > config.getSprintCost()) {
                player.setFoodLevel(20);
                player.removePotionEffect(PotionEffectType.MINING_FATIGUE);
            }
        });
    }
}
