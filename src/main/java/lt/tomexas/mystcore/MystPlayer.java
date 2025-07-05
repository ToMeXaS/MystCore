package lt.tomexas.mystcore;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

public class MystPlayer {

    private static final Map<UUID, MystPlayer> REGISTRY = new HashMap<>();

    private final OfflinePlayer player;
    private final String playerFontImage;
    private List<OfflinePlayer> islandMembers;
    private boolean jumping;

    public MystPlayer(@NotNull OfflinePlayer player, @NotNull String playerFontImage, @Nullable List<OfflinePlayer> islandMembers) {
        this.player = player;
        this.playerFontImage = playerFontImage;
        this.islandMembers = islandMembers;
        REGISTRY.put(player.getUniqueId(), this);
    }

    public static MystPlayer getMystPlayer(OfflinePlayer player) {
        if (!hasMystPlayer(player)) {
            Main.getInstance().getLogger().warning("Player " + player.getName() + " is not registered in MystPlayer registry.");
            return null;
        }
        return REGISTRY.get(player.getUniqueId());
    }
    public static boolean hasMystPlayer(OfflinePlayer player) {
        return REGISTRY.containsKey(player.getUniqueId());
    }

    public UUID getUuid() {
        return player.getUniqueId();
    }

    public boolean isJumping() {
        return jumping;
    }

    public void setJumping(boolean jumping) {
        this.jumping = jumping;
    }

    public OfflinePlayer getSpigotPlayer() {
        return player;
    }

    public List<OfflinePlayer> getIslandMembers() {
        return islandMembers;
    }

    public void setIslandMembers(List<OfflinePlayer> islandMembers) {
        this.islandMembers = islandMembers;
    }

    public String getPlayerFontImage() {
        return playerFontImage;
    }
}
