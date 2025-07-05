package lt.tomexas.mystcore.resources.managers;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.entity.BaseEntity;
import com.ticxo.modelengine.api.entity.Dummy;
import lt.tomexas.mystcore.resources.ResourcesMain;
import lt.tomexas.mystcore.resources.data.trees.TreeData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public final class CommandManager {

    private static final ResourcesMain resourcesMain = ResourcesMain.getInstance();

    // Private constructor to prevent instantiation
    private CommandManager() {
        throw new UnsupportedOperationException("CommandManager is a utility class and cannot be instantiated.");
    }

    /**
     * Sends the help message to the player.
     *
     * @param player the player to send the message to
     */
    public static void sendHelpMessage(Player player) {
        player.sendMessage("§7MystCore Resources Commands:");
        player.sendMessage("§e/mr help §7- Show this help message");
        player.sendMessage("§e/mr gettreespawner <id> §7- Get tree spawner item for the given ID");
        player.sendMessage("§e/mr removetree §7- Removes tree you're looking at");
        player.sendMessage("§e/mr setrespawntime <respawn_time_in_seconds> §7- Set respawn time for the tree you're looking at");
    }

    /**
     * Handles the "gettreespawner" command.
     *
     * @param player the player executing the command
     * @param args   the command arguments
     */
    public static void handleGetTreeSpawner(Player player, String[] args) {
        if (!validateArgsLength(player, args, 2, "Usage: /mr gettreespawner <id>")) return;

        String id = args[1];
        FileConfiguration config = resourcesMain.getFileConfigurations().get(id);
        if (config == null) {
            player.sendMessage("§cNo configuration found for the given ID: " + id);
            return;
        }

        ItemStack item = ItemManager.getItemStack(config);
        if (item == null) {
            player.sendMessage("§cFailed to create item stack from configuration.");
            return;
        }

        player.getInventory().addItem(item);
        player.sendMessage("§aGiven tree spawner item!");
    }

    public static void handleRemoveTree(Player player, String[] args) {
        UUID uuid = UUID.fromString(args[1]);
        BaseEntity<?> entity = ModelEngineAPI.getModeledEntity(uuid).getBase();
        if (TreeData.hasTree(uuid)) {
            TreeData.removeTree(uuid);
            player.sendMessage("§aTree removed successfully!");
        } else if ((entity instanceof Dummy<?> dummy)) {
            ModelEngineAPI.getModeledEntity(dummy.getUUID()).markRemoved();
        } else {
            player.sendMessage("§cTree with this UUID does not exist!");
        }
    }

    /**
     * Handles the "setrespawntime" command.
     *
     * @param player the player executing the command
     * @param args   the command arguments
     */
    public static void handleSetRespawnTime(Player player, String[] args) {
        if (!validateArgsLength(player, args, 2, "Usage: /mr setrespawntime <respawn_time>")) return;

        int respawnTime = parseInteger(args[1], "respawn time", player);
        if (respawnTime == -1) {
            player.sendMessage("§cInvalid respawn time! It must be a positive integer.");
            return;
        }

        Entity targetEntity = player.getTargetEntity(10);
        if (targetEntity != null && TreeData.hasTree(targetEntity.getUniqueId())) {
            TreeData tree = TreeData.getTree(targetEntity.getUniqueId());
            tree.setRespawnTime(respawnTime);
            player.sendMessage("§aRespawn time set successfully to " + respawnTime + " seconds!");
        } else {
            player.sendMessage("§cNo tree found at the targeted location!");
        }
    }

    /**
     * Validates the length of the command arguments.
     *
     * @param player       the player executing the command
     * @param args         the command arguments
     * @param requiredArgs the required number of arguments
     * @param usageMessage the usage message to display if validation fails
     * @return true if the arguments are valid, false otherwise
     */
    private static boolean validateArgsLength(Player player, String[] args, int requiredArgs, String usageMessage) {
        if (args.length < requiredArgs) {
            player.sendMessage("§c" + usageMessage);
            return false;
        }
        return true;
    }

    /**
     * Parses an integer from a string and sends an error message if parsing fails.
     *
     * @param input     the string to parse
     * @param fieldName the name of the field being parsed
     * @param player    the player executing the command
     * @return the parsed integer, or -1 if parsing fails
     */
    private static int parseInteger(String input, String fieldName, Player player) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            player.sendMessage("§cThe " + fieldName + " must be a valid integer!");
            return -1;
        }
    }

}
