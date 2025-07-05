package lt.tomexas.mystcore.resources;

import lt.tomexas.mystcore.Main;
import lt.tomexas.mystcore.resources.managers.TreeChopperManager;
import lt.tomexas.mystcore.resources.managers.TreeConfigManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.checkerframework.checker.units.qual.C;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ResourcesMain {

    private final Main plugin = Main.getInstance();
    private static ResourcesMain instance;
    private final String CONFIG_PATH = plugin.getDataFolder().getPath();
    private final TreeChopperManager treeChopperManager;
    private final Map<String, FileConfiguration> fileConfigurations = new HashMap<>();
    private boolean stressTesting = false;

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
            plugin.getLogger().info("Default config created: " + configFile.getPath());
        }
    }

    public static ResourcesMain getInstance() {
        return instance;
    }

    public TreeChopperManager getTreeChopperManager() {
        return treeChopperManager;
    }

    public Map<String, FileConfiguration> getFileConfigurations() {
        return fileConfigurations;
    }

    public boolean isStressTesting() {
        return stressTesting;
    }

    public void setStressTesting(boolean stressTesting) {
        this.stressTesting = stressTesting;
    }
}
