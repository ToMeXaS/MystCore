package lt.tomexas.mystcore.resources.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import com.ticxo.modelengine.api.entity.Dummy;
import lt.tomexas.mystcore.resources.managers.CommandManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;

@CommandAlias("mr|myst-resources")
public class MystResourcesCommand extends BaseCommand {

    @Default
    @Subcommand("help")
    public void onHelp(Player player) {
        CommandManager.sendHelpMessage(player);
    }

    @Subcommand("gettreespawner")
    public void onGetTreeSpawner(Player player, String treeId) {
        CommandManager.handleGetTreeSpawner(player, treeId);
    }

    @Subcommand("removetree")
    public void onRemoveTree(Player player) {
        Block block = player.getTargetBlock(null, 10);
        CommandManager.handleRemoveTree(player, block);
    }

    @Subcommand("setrespawntime")
    public void onSetRespawnTime(Player player, int time) {
        Block block = player.getTargetBlock(null, 10);
        CommandManager.handleSetRespawnTime(player, block, String.valueOf(time));
    }
}
