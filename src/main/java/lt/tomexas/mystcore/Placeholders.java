package lt.tomexas.mystcore;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class Placeholders extends PlaceholderExpansion {

    private final Main plugin;

    public Placeholders(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getAuthor() {
        return "ToMeXaS";
    }

    @Override
    public @NotNull String getIdentifier() {
        return "mystcore";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getServer().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (params.equalsIgnoreCase("player_head")) {
            MystPlayer mystPlayer = MystPlayer.getMystPlayer(player);
            if (mystPlayer == null) return "No player found";
            return mystPlayer.getPlayerFontImage();
        }

        if (params.equalsIgnoreCase("is_player_freezing")) {
            if(player.getPlayer() == null) return "No player found";
            return player.getPlayer().getFreezeTicks() > 0 ? "yes" : "no";
        }

        /*if (params.contains("island_member:")) {
            if (player.getPlayer() == null) return "";
            MystPlayer mystPlayer = MystPlayer.getMystPlayer(player);
            String[] string = params.split(":");
            if (mystPlayer == null) return "No player found";
            if (string.length > 1) {
                String integer = string[1];
                int index = Integer.parseInt(integer);
                if (index > mystPlayer.getIslandMembers().size()) return "Maximum index is " + mystPlayer.getIslandMembers().size();
                return mystPlayer.getIslandMembers().get(index-1).getName();
            }
        }

        if (params.contains("otherplayer_head:")) {
            if (player.getPlayer() == null) return "";
            String[] string = params.split(":");
            if (string.length > 1) {
                String playerName = string[1];
                OfflinePlayer targetPlayer = plugin.getServer().getOfflinePlayer(playerName);
                MystPlayer mystPlayer = MystPlayer.getMystPlayer(targetPlayer);
                if (mystPlayer != null) {
                    return mystPlayer.getPlayerFontImage();
                } else {
                    return "No player found";
                }
            }
        }*/
        return null; // Placeholder not found
    }
}
