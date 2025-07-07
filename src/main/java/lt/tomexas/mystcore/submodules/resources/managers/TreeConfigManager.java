package lt.tomexas.mystcore.submodules.resources.managers;

import lt.tomexas.mystcore.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class TreeConfigManager {

    private final Main plugin = Main.getInstance();
    private final String CONFIG_PATH = plugin.getDataFolder().getPath() +"/trees/";
    private final String CONFIG_NAME;
    private FileConfiguration config;

    public TreeConfigManager(String configName) {
        this.CONFIG_NAME = configName;
    }

    public void loadConfig() {
        File configFile = new File(CONFIG_PATH, CONFIG_NAME);

        config = YamlConfiguration.loadConfiguration(configFile);
        plugin.getLogger().info("Config loaded: " + configFile.getPath());
    }

    public FileConfiguration getConfig() {
        if (config == null) {
            loadConfig();
        }
        return config;
    }
}
