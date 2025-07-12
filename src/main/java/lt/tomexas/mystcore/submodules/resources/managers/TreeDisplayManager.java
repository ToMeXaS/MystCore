package lt.tomexas.mystcore.submodules.resources.managers;

import lt.tomexas.mystcore.Main;
import lt.tomexas.mystcore.PluginLogger;
import lt.tomexas.mystcore.submodules.resources.data.trees.Tree;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;

public class TreeDisplayManager implements Listener {

    private final Main plugin = Main.getInstance();
    private BukkitTask updaterTask;

    public TreeDisplayManager() {
        Bukkit.getPluginManager().registerEvents(this, plugin);

        if (!Bukkit.getOnlinePlayers().isEmpty()) {
            startUpdater();
        }
    }

    private void startUpdater() {
        if (updaterTask != null && !updaterTask.isCancelled()) return;
        updaterTask = new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getOnlinePlayers().forEach(player -> {
                    RayTraceResult result = player.rayTraceBlocks(5);
                    if (result == null || result.getHitBlock() == null) return;
                    Tree tree = Tree.getByRayTraceResult(result);
                    if (tree == null) return;

                    BlockFace blockFace = result.getHitBlockFace();
                    if (blockFace == null) return;

                    TextDisplay textDisplay = tree.getTextDisplay();
                    TextDisplay healthDisplay = tree.getHealthDisplay();

                    // Calculate yaw for the opposite block face
                    BlockFace opposite = blockFace.getOppositeFace();
                    float yaw = switch (opposite) {
                        case SOUTH -> 180f;
                        case EAST  -> 90f;
                        case WEST  -> -90f;
                        default -> 0f;
                    };

                    // Process textDisplay
                    if (textDisplay != null) {
                        Location textLoc = getTargetLocation(result, blockFace, 0.7);
                        if (textLoc != null) {
                            textLoc.setY(textDisplay.getY());
                            textDisplay.teleport(textLoc);
                            textDisplay.setRotation(yaw, 0f);
                        } else {
                            PluginLogger.debug("Target location is null for block: " + result.getHitBlock());
                        }
                    }

                    // Process healthDisplay
                    if (healthDisplay != null) {
                        Location healthLoc = getTargetLocation(result, blockFace, 0.5);
                        if (healthLoc != null) {
                            healthLoc.setY(healthDisplay.getY());
                            healthDisplay.teleport(healthLoc);
                            healthDisplay.setRotation(yaw, 0f);
                        } else {
                            PluginLogger.debug("Target location is null for block: " + result.getHitBlock());
                        }
                    }
                });
            }
        }.runTaskTimer(plugin, 0L, 20L); // Run every second
    }

    private void stopUpdater() {
        if (updaterTask != null && !updaterTask.isCancelled()) {
            updaterTask.cancel();
            updaterTask = null;
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerQuitEvent event) {
        if (Bukkit.getOnlinePlayers().size() == 1) {
            startUpdater();
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (Bukkit.getOnlinePlayers().isEmpty()) {
                stopUpdater();
            }
        }, 1L); // Check after 1 tick
    }

    private Location getTargetLocation(RayTraceResult result, BlockFace blockFace, double distance) {
        Block block = result.getHitBlock();
        if (block == null) return null;
        Location base = block.getLocation().clone().add(0.5, 0.5, 0.5);

        return base.clone().add(
                blockFace.getModX() * distance,
                blockFace.getModY() * distance,
                blockFace.getModZ() * distance
        );
    }
}
