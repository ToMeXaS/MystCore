package lt.tomexas.mystcore;

import co.aikar.commands.PaperCommandManager;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import lombok.Getter;
import lt.tomexas.mystcore.data.MystPlayer;
import lt.tomexas.mystcore.listeners.*;
import lt.tomexas.mystcore.submodules.playerfontimage.PlayerFontImage;
import lt.tomexas.mystcore.submodules.resources.data.trees.Tree;
import lt.tomexas.mystcore.submodules.resources.data.trees.config.TreeConfig;
import lt.tomexas.mystcore.submodules.resources.data.trees.config.TreeConfigConstructor;
import lt.tomexas.mystcore.submodules.resources.data.trees.config.TreeConfigRepresenter;
import lt.tomexas.mystcore.submodules.resources.listeners.TreeEntityInteractListener;
import lt.tomexas.mystcore.submodules.resources.ResourcesDatabase;
import lt.tomexas.mystcore.submodules.resources.listeners.TreeSpawnerPlaceListener;
import lt.tomexas.mystcore.submodules.resources.commands.MystResourcesCommand;
import lt.tomexas.mystcore.managers.ConfigManager;
import lt.tomexas.mystcore.submodules.resources.managers.TreeChopperManager;
import lt.tomexas.mystcore.submodules.worldguard.flags.DenyEntryFlag;
import lt.tomexas.mystcore.submodules.worldguard.listeners.PlayerAreaEnterListener;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;

import java.io.File;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

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
    private TreeChopperManager treeChopperManager;

    @Override
    public void onLoad() {
        //registerWorldGuardFlags(); // Uncomment if you want to register WorldGuard flags on load
    }

    @Override
    public void onEnable() {
        instance = this;
        initManagers();
        registerEvents();
        registerCommands();
        registerPlaceholders();
        initDatabases();
        initializePlayers();
        startPlayerTask();
    }

    @Override
    public void onDisable() {
        // Save any necessary data or perform cleanup here
        saveResourcesData();
        getLogger().info("MystCore has been disabled.");
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
        manager.getCommandCompletions().registerAsyncCompletion("treeIds", c -> getTreeFileNames());
    }

    private void registerPlaceholders() {
        new Placeholders(this).register();
    }

    private void saveResourcesData() {
        this.resourcesDatabase.addOrUpdateTrees(Tree.getAllTrees().keySet());
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

        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        dumperOptions.setPrettyFlow(true);

        this.treeConfigs = new ConfigManager<>(
                TreeConfig.class,
                getDataFolder().getPath() + "/trees/oak_tree.yml",
                new TreeConfigConstructor(TreeConfig.class, new LoaderOptions()),
                new TreeConfigRepresenter(dumperOptions)
                ).loadConfigDir();
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
        Player player = mystPlayer.getPlayer();
        PlayerData playerData = PlayerData.get(player);

        if (player.isSprinting()) {
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
