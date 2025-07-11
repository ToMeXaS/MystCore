package lt.tomexas.mystcore.submodules.resources.managers;

import com.ticxo.modelengine.api.ModelEngineAPI;
import lt.tomexas.mystcore.Main;
import lt.tomexas.mystcore.submodules.resources.data.trees.Tree;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;

import java.util.UUID;

public final class CommandManager {

    private static final Main plugin = Main.getInstance();

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
        player.sendMessage("");
    }

    /**
     * Handles the "gettreespawner" command.
     *
     * @param player the player executing the command
     * @param args   the command arguments
     */
    public static void handleGetTreeSpawner(Player player, String treeId) {
        FileConfiguration config = plugin.getConfigManager().getFileConfigurations().get(treeId);
        if (config == null) {
            player.sendMessage("§cNo configuration found for the given ID: " + treeId + "!");
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

    /**
     * Handles the "removetree" command.
     *
     * @param player the player executing the command
     * @param result the ray trace result from the player's view
     */
    public static void handleRemoveTree(Player player, RayTraceResult result) {
        Tree tree = Tree.getByRayTraceResult(result);
        if (tree != null) {
            UUID uuid = tree.getUuid();
            if (!tree.remove()) {
                player.sendMessage("§cFailed to remove the tree!");
                return;
            }

            ModelEngineAPI.getModeledEntity(uuid).markRemoved();

            // Remove the barrier blocks if they exist
            Bukkit.getScheduler().runTask(Main.getInstance(), () ->
                    tree.getBarrierBlocks().forEach(block -> block.setType(Material.AIR))
            );

            // Remove the tree from the database
            plugin.getResourcesDatabase().removeTree(uuid);
            player.sendMessage("§aTree removed successfully!");
        } else {
            player.sendMessage("§cNo tree found at the targeted location!");
        }
    }

    /**
     * Handles the "setrespawntime" command.
     *
     * @param player the player executing the command
     * @param block  the block the player is looking at
     * @param time   the respawn time to set
     */
    public static void handleSetRespawnTime(Player player, Block block, String time) {

        int respawnTime = parseInteger(time, "respawn time", player);
        if (respawnTime == -1) {
            player.sendMessage("§cInvalid respawn time! It must be a positive integer.");
            return;
        }

        Tree tree = Tree.getByBlock(block);
        if (tree == null) {
            player.sendMessage("§cNo tree found at the targeted location!");
            return;
        }
        UUID uuid = tree.getUuid();

        if (Tree.exists(uuid)) {
            tree.setRespawnTime(respawnTime);
            player.sendMessage("§aRespawn time set successfully to " + respawnTime + " seconds!");
        } else {
            player.sendMessage("§cNo tree found at the targeted location!");
        }
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
