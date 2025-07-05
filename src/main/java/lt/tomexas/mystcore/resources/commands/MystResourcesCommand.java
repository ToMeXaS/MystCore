package lt.tomexas.mystcore.resources.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import com.ticxo.modelengine.api.entity.Dummy;
import lt.tomexas.mystcore.resources.managers.CommandManager;
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
        Entity entity = player.getTargetEntity(10, false);
        if (!(entity instanceof Dummy<?> dummy)) {
            player.sendMessage("§cYou must be looking at a tree to remove it!");
            return;
        }
        CommandManager.handleRemoveTree(player, dummy.getUUID());
    }

    @Subcommand("setrespawntime")
    public void onSetRespawnTime(Player player, int time) {
        Entity entity = player.getTargetEntity(10, false);
        if (!(entity instanceof Dummy<?> dummy)) {
            player.sendMessage("§cYou must be looking at a tree to remove it!");
            return;
        }
        CommandManager.handleSetRespawnTime(player, dummy.getUUID(), String.valueOf(time));
    }
}
