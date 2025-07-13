package lt.tomexas.mystcore;

import co.aikar.commands.PaperCommandManager;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import lombok.Getter;
import lt.tomexas.mystcore.data.MystPlayer;
import lt.tomexas.mystcore.listeners.*;
import lt.tomexas.mystcore.submodules.playerfontimage.PlayerFontImage;
import lt.tomexas.mystcore.submodules.resources.trees.data.Tree;
import lt.tomexas.mystcore.submodules.resources.trees.data.config.TreeConfig;
import lt.tomexas.mystcore.submodules.resources.trees.data.config.TreeConfigConstructor;
import lt.tomexas.mystcore.submodules.resources.trees.data.config.TreeConfigRepresenter;
import lt.tomexas.mystcore.submodules.resources.trees.listeners.TreeEntityInteractListener;
import lt.tomexas.mystcore.submodules.resources.ResourcesDatabase;
import lt.tomexas.mystcore.submodules.resources.trees.listeners.TreeSpawnerPlaceListener;
import lt.tomexas.mystcore.submodules.resources.commands.MystResourcesCommand;
import lt.tomexas.mystcore.managers.ConfigManager;
import lt.tomexas.mystcore.submodules.resources.trees.managers.TreeChopperManager;
import lt.tomexas.mystcore.submodules.stats.stamina.listeners.BlockBreakListener;
import lt.tomexas.mystcore.submodules.stats.stamina.listeners.EntityDamageByEntityListener;
import lt.tomexas.mystcore.submodules.stats.stamina.config.StaminaConfig;
import lt.tomexas.mystcore.submodules.stats.stamina.config.StaminaConfigRepresenter;
import lt.tomexas.mystcore.submodules.stats.stamina.tasks.UpdatePlayerStamina;
import lt.tomexas.mystcore.submodules.worldguard.flags.DenyEntryFlag;
import lt.tomexas.mystcore.submodules.worldguard.listeners.PlayerAreaEnterListener;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.constructor.Constructor;

import java.sql.SQLException;
import java.util.*;

public final class Main extends JavaPlugin {

    @Getter
    private static Main instance;
    @Getter
    private ResourcesDatabase resourcesDatabase;
    @Getter
    private PlayerFontImage playerFontImage;

    @Getter
    private Map<String, TreeConfig> treeConfigs = new HashMap<>();
    @Getter
    private StaminaConfig staminaConfig;

    @Getter
    private TreeChopperManager treeChopperManager;

    @Override
    public void onLoad() {
        //registerWorldGuardFlags(); // Uncomment if you want to register WorldGuard flags on load
    }

    @Override
    public void onEnable() {
        instance = this;
        initConfigs();
        initManagers();
        registerEvents();
        registerCommands();
        registerPlaceholders();
        initDatabases();
        initializePlayers();
        startPlayerTasks();
    }

    @Override
    public void onDisable() {
        // Save any necessary data or perform cleanup here
        try {
            saveResourcesData();
            getLogger().info("MystCore has been disabled.");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void registerWorldGuardFlags() {
        DenyEntryFlag.register();
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(), this);
        getServer().getPluginManager().registerEvents(new EntityDamageByEntityListener(), this);
        getServer().getPluginManager().registerEvents(new TreeEntityInteractListener(), this);
        getServer().getPluginManager().registerEvents(new TreeSpawnerPlaceListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerAreaEnterListener(), this);
    }

    private void registerCommands() {
        PaperCommandManager manager = new PaperCommandManager(this);
        manager.registerCommand(new MystResourcesCommand());
        manager.getCommandCompletions().registerAsyncCompletion("treeIds", c ->
                this.treeConfigs.keySet().stream()
                        .filter(id -> id.startsWith(c.getInput()))
                        .toList()
        );
    }

    private void registerPlaceholders() {
        new Placeholders(this).register();
    }

    private void saveResourcesData() throws SQLException {
        this.resourcesDatabase.addOrUpdateTrees(Tree.getAllTrees().keySet());
        this.resourcesDatabase.closeConnection();
    }

    private void initDatabases() {
        try {
            this.resourcesDatabase = new ResourcesDatabase();
            this.resourcesDatabase.loadAllTrees();
        } catch (SQLException e) {
            PluginLogger.debug("Failed to initialize databases: " + e.getMessage());
        }

    }

    private void initializePlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            MystPlayer mystPlayer = MystPlayer.getMystPlayer(player);
            if (mystPlayer == null) {
                PlayerData playerData = PlayerData.get(player.getUniqueId());
                new MystPlayer(player, playerData);
            }
        }
    }

    private void initManagers() {
        this.treeChopperManager = new TreeChopperManager();
    }

    private void initConfigs() {
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        dumperOptions.setPrettyFlow(true);

        this.treeConfigs = new ConfigManager<>(
                TreeConfig.class,
                getDataFolder().getPath() + "/trees/oak_tree.yml",
                new TreeConfigConstructor(TreeConfig.class, new LoaderOptions()),
                new TreeConfigRepresenter(dumperOptions)
        ).loadConfigDir();

        this.staminaConfig = new ConfigManager<>(
                StaminaConfig.class,
                getDataFolder().getPath() + "/stats/stamina.yml",
                new Constructor(StaminaConfig.class, new LoaderOptions()),
                new StaminaConfigRepresenter(dumperOptions)
        ).loadConfig();
    }

    private void startPlayerTasks() {
        new UpdatePlayerStamina().runTaskTimerAsynchronously(this, 0L, 20L);
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
}
