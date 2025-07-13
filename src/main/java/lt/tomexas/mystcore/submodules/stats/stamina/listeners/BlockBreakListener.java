package lt.tomexas.mystcore.submodules.stats.stamina.listeners;

import lt.tomexas.mystcore.Main;
import lt.tomexas.mystcore.data.MystPlayer;
import lt.tomexas.mystcore.data.enums.Permissions;
import lt.tomexas.mystcore.submodules.stats.stamina.config.StaminaConfig;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlockBreakListener implements Listener {

    private final Main plugin = Main.getInstance();
    private final StaminaConfig config = plugin.getStaminaConfig();

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission(Permissions.BYPASS_STAMINA.asString()) && config.isEnableBypass()) return;
        MystPlayer mystPlayer = MystPlayer.getMystPlayer(player);
        if (mystPlayer == null) return;
        PlayerData playerData = mystPlayer.getPlayerData();

        if (!config.isBlockBreakEnabled()) return;

        double staminaCost = getStaminaCost(player);

        // Check if the player has enough stamina
        if (playerData.getStamina() < staminaCost) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 999999, 5, false, false));
            return;
        }
        playerData.setStamina(playerData.getStamina() - getStaminaCost(player));
    }

    public double getStaminaCost(Player player) {
        if (!config.isEnablePermissionDrain()) return config.getBlockBreakCost();
        Pattern pattern = Permissions.STAMINA_BLOCKBREAK_DRAIN.asPattern();
        for (PermissionAttachmentInfo perm : player.getEffectivePermissions()) {
            Matcher matcher = pattern.matcher(perm.getPermission());
            if (matcher.matches()) {
                return Double.parseDouble(matcher.group(1));
            }
        }
        return config.getBlockBreakCost();
    }
}
