package lt.tomexas.mystcore.resources.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
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
        CommandManager.handleGetTreeSpawner(player, new String[] {treeId});
    }

    @Subcommand("removetree")
    public void onRemoveTree(Player player, String uuid) {
        CommandManager.handleRemoveTree(player, new String[] {uuid});
    }

    @Subcommand("setrespawntime")
    public void onSetRespawnTime(Player player, String uuid, int time) {
        CommandManager.handleSetRespawnTime(player, new String[] {uuid, String.valueOf(time)});
    }
}
