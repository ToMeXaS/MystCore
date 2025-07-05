package lt.tomexas.mystcore;

import co.aikar.commands.PaperCommandManager;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.sun.source.tree.Tree;
import lt.tomexas.mystcore.listeners.BlockBreakListener;
import lt.tomexas.mystcore.listeners.EntityDamageByEntityListener;
import lt.tomexas.mystcore.listeners.PlayerJoinListener;
import lt.tomexas.mystcore.listeners.PlayerMoveListener;
import lt.tomexas.mystcore.playerfontimage.PlayerFontImage;
import lt.tomexas.mystcore.resources.ResourcesMain;
import lt.tomexas.mystcore.resources.listeners.BaseEntityInteractListener;
import lt.tomexas.mystcore.resources.ResourcesDatabase;
import lt.tomexas.mystcore.resources.listeners.PlayerInteractListener;
import lt.tomexas.mystcore.resources.commands.MystResourcesCommand;
import lt.tomexas.mystcore.resources.data.trees.TreeData;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class Main extends JavaPlugin {

    private static Main instance;
    private Database database;
    private ResourcesDatabase resourcesDatabase;
    private PlayerFontImage playerFontImage;

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
                resourcesDatabase.addOrUpdateTrees(TreeData.getAllTrees().keySet());
                resourcesDatabase.closeConnection();
            }
        } catch (SQLException e) {
            getLogger().severe("Error while closing database connections: " + e.getMessage());
        }
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(), this);
        getServer().getPluginManager().registerEvents(new EntityDamageByEntityListener(), this);
        getServer().getPluginManager().registerEvents(new BaseEntityInteractListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(), this);
    }

    private void registerCommands() {
        PaperCommandManager manager = new PaperCommandManager(this);
        manager.registerCommand(new MystResourcesCommand());
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
            new MystPlayer(player, playerHead, null);
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

    public static Main getInstance() {
        return instance;
    }

    public PlayerFontImage getPlayerFontImage() {
        return playerFontImage;
    }

    public Database getDatabase() {
        return database;
    }

    public ResourcesDatabase getResourcesDatabase() {
        return resourcesDatabase;
    }
}
