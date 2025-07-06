package lt.tomexas.mystcore;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

public class MystPlayer {

    private static final Map<UUID, MystPlayer> REGISTRY = new HashMap<>();

    private final OfflinePlayer player;
    @Getter
    private final String playerFontImage;
    @Setter
    @Getter
    private List<OfflinePlayer> islandMembers;
    @Setter
    @Getter
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

    public OfflinePlayer getSpigotPlayer() {
        return player;
    }

}
