package lt.tomexas.mystcore.submodules.resources;

import lombok.Getter;
import lt.tomexas.mystcore.Main;
import lt.tomexas.mystcore.PluginLogger;
import lt.tomexas.mystcore.submodules.resources.managers.TreeChopperManager;
import lt.tomexas.mystcore.submodules.resources.managers.TreeConfigManager;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ResourcesMain {

    private final Main plugin = Main.getInstance();
    @Getter
    private static ResourcesMain instance;
    private final String CONFIG_PATH = plugin.getDataFolder().getPath();
    @Getter
    private final TreeChopperManager treeChopperManager;
    @Getter
    private final Map<String, FileConfiguration> fileConfigurations = new HashMap<>();

    public ResourcesMain() {
        instance = this;
        this.treeChopperManager = new TreeChopperManager();
        loadTreeConfiguration();
    }

    private void loadTreeConfiguration() {
        File configDir = new File(CONFIG_PATH + "/trees/");

        // Ensure the directory exists
        if (!configDir.exists()) {
            saveDefaultConfig();
        }

        // List and iterate through the files
        File[] files = configDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".yml")) {
                    plugin.getLogger().info("Found tree config file: " + file.getName());
                    // You can load the configuration here if needed
                    TreeConfigManager treeConfigManager = new TreeConfigManager(file.getName());
                    FileConfiguration config = treeConfigManager.getConfig();
                    this.fileConfigurations.put(file.getName(), config);
                }
            }
        }
    }

    public void saveDefaultConfig() {
        File configFile = new File(CONFIG_PATH, "oak_tree_1.yml");

        if (!configFile.exists()) {
            plugin.saveResource("trees/oak_tree_1.yml", false);
            PluginLogger.debug("Default config created: " + configFile.getPath());
        }
    }

}
