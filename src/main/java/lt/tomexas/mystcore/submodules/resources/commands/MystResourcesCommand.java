package lt.tomexas.mystcore.submodules.resources.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import com.ticxo.modelengine.api.ModelEngineAPI;
import lt.tomexas.mystcore.submodules.resources.trees.managers.CommandManager;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.util.RayTraceResult;

import java.util.UUID;

@CommandAlias("mr|myst-resources|myst-res")
public class MystResourcesCommand extends BaseCommand {

    @Default
    @Subcommand("help")
    public void onHelp(Player player) {
        CommandManager.sendHelpMessage(player);
    }

    @Subcommand("gettreespawner")
    @CommandCompletion("@treeIds")
    public void onGetTreeSpawner(Player player, String treeId) {
        CommandManager.handleGetTreeSpawner(player, treeId);
    }

    @Subcommand("removetree")
    public void onRemoveTree(Player player) {
        RayTraceResult result = player.rayTraceBlocks(3.0f);
        CommandManager.handleRemoveTree(player, result);
    }

    @Subcommand("setrespawntime")
    @CommandCompletion("@range:1-3600")
    public void onSetRespawnTime(Player player, int time) {
        Block block = player.getTargetBlock(null, 10);
        CommandManager.handleSetRespawnTime(player, block, String.valueOf(time));
    }

    @Subcommand("forcedelete")
    public void onForceDelete(Player player, String uuid) {
        ModelEngineAPI.getModeledEntity(UUID.fromString(uuid)).markRemoved();
        player.sendMessage("§aSuccessfully deleted entity with UUID: " + uuid);
    }
}
