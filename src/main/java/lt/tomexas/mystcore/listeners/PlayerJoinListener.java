package lt.tomexas.mystcore.listeners;

import lt.tomexas.mystcore.data.MystPlayer;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        PlayerData playerData = PlayerData.get(player.getUniqueId());
        new MystPlayer(player, playerData);
    }
}
