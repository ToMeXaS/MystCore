package lt.tomexas.mystcore.submodules.resources.trees.listeners;


import com.ticxo.modelengine.api.events.BaseEntityInteractEvent;
import lt.tomexas.mystcore.Main;
import lt.tomexas.mystcore.data.MystPlayer;
import lt.tomexas.mystcore.submodules.resources.trees.data.Tree;
import lt.tomexas.mystcore.submodules.resources.trees.managers.TreeChopperManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class TreeEntityInteractListener implements Listener {

    private final Main plugin = Main.getInstance();
    private final TreeChopperManager treeChopperManager = plugin.getTreeChopperManager();

    @EventHandler
    public void onTreeEntityInteract(BaseEntityInteractEvent event) {
        Player player = event.getPlayer();
        MystPlayer mystPlayer = MystPlayer.getMystPlayer(player);
        if (mystPlayer == null) return;
        UUID entityId = event.getBaseEntity().getUUID();
        Tree tree = Tree.getTreeByUuid(entityId);
        if (!event.getAction().equals(BaseEntityInteractEvent.Action.ATTACK)) return;
        if (tree == null) return;
        if (!mystPlayer.hasRequiredAxe(tree)) return;
        if (!mystPlayer.hasRequiredLevel(tree)) return;
        if (!mystPlayer.hasRequiredStamina(tree)) return;
        this.treeChopperManager.handleChop(mystPlayer, entityId);
    }
}
