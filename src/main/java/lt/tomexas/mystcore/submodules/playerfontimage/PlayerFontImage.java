package lt.tomexas.mystcore.submodules.playerfontimage;

import lt.tomexas.mystcore.submodules.playerfontimage.impl.MinotarSource;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;


/**
 * A class for retrieving player head representations as BaseComponents.
 * The head representation is generated based on the players UUID and skin source.
 *
 * @author Minso
 */
public class PlayerFontImage {

    /**
     * The default SkinSource used in the code of this plugin.
     */
    public static SkinSource defaultSource = new MinotarSource();

    private static PlayerFontImage instance;

    private final JavaPlugin plugin;

    /**
     * Constructs a new ChatHeadAPI instance.
     *
     * @param plugin The JavaPlugin instance associated with the ChatHeadAPI.
     */
    public PlayerFontImage(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Initializes the ChatHeadAPI with the provided JavaPlugin instance.
     *
     * @param plugin The JavaPlugin instance to associate with the ChatHeadAPI.
     * @throws IllegalStateException If ChatHeadAPI has already been initialized.
     */
    public static PlayerFontImage initialize(JavaPlugin plugin) {
        if (instance != null) {
            throw new IllegalStateException("PlayerHeadAPI has already been initialized.");
        }
        instance = new PlayerFontImage(plugin);
        return instance;
    }


    /**
     * Creates a 8x8 grid of pixels representing a Minecraft player's head.
     * Each pixel in the grid is represented by a TextComponent with a specified hexadecimal color.
     *
     * @param uuid       The UUID of the player whose head is to be retrieved & created.
     * @param overlay    A boolean value indicating whether to apply overlay on the players head.
     * @param skinSource An enum specifying the source from which to retrieve the player's skin.
     *                   Supported sources include MOJANG, MINOTAR, and CRAFATAR.
     * @return An array of BaseComponents representing the player's head.
     * Each BaseComponent represents a single pixel, forming a 8x8 grid of pixels.
     */
    public BaseComponent[] getHead(UUID uuid, boolean overlay, SkinSource skinSource) {
        return skinSource.getHead(Bukkit.getOfflinePlayer(uuid), overlay);
    }

    public BaseComponent[] getHead(OfflinePlayer player, boolean overlay, SkinSource skinSource) {
        return skinSource.getHead(player, overlay);
    }

    public BaseComponent[] getHead(OfflinePlayer player, boolean overlay) {
        return defaultSource.getHead(player, overlay);
    }

    public BaseComponent[] getHead(OfflinePlayer player) {
        return defaultSource.getHead(player, true);
    }

    public BaseComponent[] getBust(OfflinePlayer player, boolean overlay, SkinSource skinSource) {
        return skinSource.getBust(player, overlay);
    }

    public BaseComponent[] getBust(UUID uuid, boolean overlay, SkinSource skinSource) {
        return skinSource.getBust(Bukkit.getOfflinePlayer(uuid), overlay);
    }

    /**
     * Exports the BaseComponent[] from the getHead method to a String.
     *
     * @param uuid       The UUID of the player whose head is to be retrieved & created.
     * @param overlay    A boolean value indicating whether to apply overlay on the players head.
     * @param skinSource An enum specifying the source from which to retrieve the player's skin.
     *                   Supported sources include MOJANG, MINOTAR, and CRAFATAR.
     */
    public String getHeadAsString(UUID uuid, boolean overlay, SkinSource skinSource) {
        return getHeadAsString(Bukkit.getOfflinePlayer(uuid), overlay, skinSource);
    }

    /**
     * Exports the BaseComponent[] from the getHead method to a String.
     *
     * @param player     The Player object representing the player whose head is to be retrieved.
     * @param overlay    A boolean value indicating whether to apply overlay on the players head.
     * @param skinSource An enum specifying the source from which to retrieve the player's skin.
     *                   Supported sources include MOJANG, MINOTAR, and CRAFATAR.
     */
    public String getHeadAsString(OfflinePlayer player, boolean overlay, SkinSource skinSource) {
        return TextComponent.toLegacyText(
                this.getHead(player, overlay, skinSource)
        );
    }

    public String getHeadAsString(OfflinePlayer player) {
        return getHeadAsString(player, true, defaultSource);
    }

    public String getBustAsString(UUID uuid, boolean overlay, SkinSource skinSource) {
        return getBustAsString(Bukkit.getOfflinePlayer(uuid), overlay, skinSource);
    }

    public String getBustAsString(OfflinePlayer player, boolean overlay, SkinSource skinSource) {
        return TextComponent.toLegacyText(
                this.getBust(player, overlay, skinSource)
        );
    }

    public String getBustAsString(OfflinePlayer player) {
        return getBustAsString(player, true, defaultSource);
    }

}
