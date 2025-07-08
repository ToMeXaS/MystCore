package lt.tomexas.mystcore.submodules.worldguard.listeners;

import lt.tomexas.mystcore.submodules.worldguard.flags.DenyEntryFlag;
import lt.tomexas.mystcore.submodules.worldguard.records.EntryCheckResult;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerAreaEnterListener implements Listener {

    @EventHandler
    public void onAreaEnter(PlayerMoveEvent event) {
        // Only check if the player moved to a different block (not just looking around)
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();

        EntryCheckResult result = DenyEntryFlag.canEnter(player, event.getTo());
        if (!result.canEnter()) {
            event.setCancelled(true);
            player.sendMessage(result.denyMessage());
        }
    }

}
