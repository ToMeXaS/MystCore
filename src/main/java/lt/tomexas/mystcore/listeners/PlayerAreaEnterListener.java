package lt.tomexas.mystcore.listeners;

import lt.tomexas.mystcore.submodules.worldguard.flags.DenyEntryFlag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerAreaEnterListener implements Listener {

    @EventHandler
    public void onAreaEnter(PlayerMoveEvent event) {
        if (!DenyEntryFlag.canEnter(event.getPlayer())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("You cannot enter this area due to combat restrictions.");
        }
        event.getPlayer().sendMessage("You can enter this area.");
    }

}
