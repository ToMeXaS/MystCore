package lt.tomexas.mystcore.listeners;

import lt.tomexas.mystcore.Database;
import lt.tomexas.mystcore.Main;
import lt.tomexas.mystcore.MystPlayer;
import lt.tomexas.mystcore.playerfontimage.impl.MinotarSource;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final Main plugin = Main.getInstance();
    private final Database database = plugin.getDatabase();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        //String playerHead;
        if (!database.playerExists(player)) {
            database.addPlayer(player);
            //playerHead = plugin.getPlayerFontImage().getHeadAsString(player.getUniqueId(), true, new MinotarSource(true));
        } else {
            database.updatePlayer(player);
            //playerHead = database.getPlayerHead(player);
        }

        PlayerData playerData = PlayerData.get(player.getUniqueId());
        new MystPlayer(player, playerData, null);
    }
}
