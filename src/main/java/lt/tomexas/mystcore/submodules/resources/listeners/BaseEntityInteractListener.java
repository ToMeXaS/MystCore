package lt.tomexas.mystcore.submodules.resources.listeners;


import com.ticxo.modelengine.api.events.BaseEntityInteractEvent;
import lt.tomexas.mystcore.submodules.resources.ResourcesMain;
import lt.tomexas.mystcore.submodules.resources.data.trees.Tree;
import lt.tomexas.mystcore.submodules.resources.managers.PlayerManager;
import lt.tomexas.mystcore.submodules.resources.managers.TreeChopperManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class BaseEntityInteractListener implements Listener {

    private final ResourcesMain resourcesMain = ResourcesMain.getInstance();
    private final TreeChopperManager treeChopperManager = resourcesMain.getTreeChopperManager();

    @EventHandler
    public void onEntityInteract(BaseEntityInteractEvent event) {
        Player player = event.getPlayer();
        UUID entityId = event.getBaseEntity().getUUID();
        Tree tree = Tree.getTreeByUuid(entityId);
        if (!event.getAction().equals(BaseEntityInteractEvent.Action.ATTACK)) return;
        if (tree == null) return;
        if (!PlayerManager.hasRequiredAxe(tree, player)) return;
        if (!PlayerManager.hasRequiredLevel(tree, player)) return;
        if (!PlayerManager.hasRequiredStamina(tree, player)) return;

        treeChopperManager.startChopping(player, entityId);
    }
}
