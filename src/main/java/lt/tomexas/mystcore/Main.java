package lt.tomexas.mystcore;

import co.aikar.commands.PaperCommandManager;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import lombok.Getter;
import lt.tomexas.mystcore.listeners.*;
import lt.tomexas.mystcore.playerfontimage.PlayerFontImage;
import lt.tomexas.mystcore.submodules.resources.ResourcesMain;
import lt.tomexas.mystcore.submodules.resources.listeners.BaseEntityInteractListener;
import lt.tomexas.mystcore.submodules.resources.ResourcesDatabase;
import lt.tomexas.mystcore.submodules.resources.listeners.PlayerInteractListener;
import lt.tomexas.mystcore.submodules.resources.commands.MystResourcesCommand;
import lt.tomexas.mystcore.submodules.resources.data.trees.Tree;
import lt.tomexas.mystcore.submodules.worldguard.flags.DenyEntryFlag;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class Main extends JavaPlugin {

    @Getter
    private static Main instance;
    @Getter
    private Database database;
    @Getter
    private ResourcesDatabase resourcesDatabase;
    @Getter
    private PlayerFontImage playerFontImage;

    @Override
    public void onLoad() {
        registerWorldGuardFlags();
    }

    @Override
    public void onEnable() {
        instance = this;
        new ResourcesMain();
        this.playerFontImage = PlayerFontImage.initialize(this);
        setupDatabases();
        registerEvents();
        registerCommands();
        registerPlaceholders();
        loadResources();
        initializePlayers();
        startPlayerTask();
    }

    @Override
    public void onDisable() {
        closeDatabases();
    }

    private void setupDatabases() {
        try {
            if (!getDataFolder().exists()) {
                getDataFolder().mkdirs();
            }
            database = new Database(getDataFolder().getAbsolutePath() + "/players.db");
            resourcesDatabase = new ResourcesDatabase(getDataFolder().getAbsolutePath());
            for (Player player : Bukkit.getOnlinePlayers()) {
                database.addPlayer(player);
            }
        } catch (SQLException e) {
            getLogger().severe("Failed to connect to the database! " + e.getMessage());
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    private void closeDatabases() {
        try {
            if (database != null) {
                database.closeConnection();
            }

            if (resourcesDatabase != null) {
                resourcesDatabase.addOrUpdateTrees(Tree.getAllTrees().keySet());
                resourcesDatabase.closeConnection();
            }
        } catch (SQLException e) {
            getLogger().severe("Error while closing database connections: " + e.getMessage());
        }
    }

    private void registerWorldGuardFlags() {
        DenyEntryFlag.register();
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(), this);
        getServer().getPluginManager().registerEvents(new EntityDamageByEntityListener(), this);
        getServer().getPluginManager().registerEvents(new BaseEntityInteractListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerAreaEnterListener(), this);
    }

    private void registerCommands() {
        PaperCommandManager manager = new PaperCommandManager(this);
        manager.registerCommand(new MystResourcesCommand());
        manager.getCommandCompletions().registerAsyncCompletion("treeIds", c -> getTreeFileNames());
    }

    private void registerPlaceholders() {
        new Placeholders(this).register();
    }

    private void loadResources() {
        resourcesDatabase.loadAllTrees();
    }

    private void initializePlayers() {
        database.getAllPlayerHeads().forEach((uuid, playerHead) -> {
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            PlayerData playerData = PlayerData.get(player.getUniqueId());
            new MystPlayer(player, playerData, playerHead, null);
        });
    }

    private void startPlayerTask() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                MystPlayer mystPlayer = MystPlayer.getMystPlayer(player);
                if (mystPlayer == null) continue;

                updateIslandMembers(mystPlayer);
                updatePlayerStamina(mystPlayer);
            }
        }, 0L, 20L); // Run every second
    }

    private void updateIslandMembers(MystPlayer mystPlayer) {
        Island island = SuperiorSkyblockAPI.getPlayer(mystPlayer.getUuid()).getIsland();
        if (island == null) return;

        List<OfflinePlayer> islandMembers = new ArrayList<>();
        for (SuperiorPlayer member : island.getIslandMembers(true)) {
            if (!member.getUniqueId().equals(mystPlayer.getUuid())) {
                islandMembers.add(member.asOfflinePlayer());
            }
        }
        mystPlayer.setIslandMembers(islandMembers);
    }

    private void updatePlayerStamina(MystPlayer mystPlayer) {
        Player player = mystPlayer.getSpigotPlayer().getPlayer();
        PlayerData playerData = PlayerData.get(player);

        if (mystPlayer.isJumping() || player.isSprinting()) {
            if (playerData.getStamina() > 2) {
                playerData.setStamina(playerData.getStamina() - 3);
            } else {
                player.setFoodLevel(4);
            }
        }

        if (playerData.getStamina() > 3) {
            player.setFoodLevel(20);
            player.removePotionEffect(PotionEffectType.MINING_FATIGUE);
        }
    }

    private List<String> getTreeFileNames() {
        File treesFolder = new File(getDataFolder(), "trees");
        if (!treesFolder.isDirectory()) return Collections.emptyList();
        File[] files = treesFolder.listFiles(File::isFile);
        if (files == null) return Collections.emptyList();
        return Arrays.stream(files)
                .map(File::getName)
                .collect(Collectors.toList());
    }
}
