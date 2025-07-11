package lt.tomexas.mystcore.submodules.resources.managers;

import lt.tomexas.mystcore.Main;
import lt.tomexas.mystcore.PluginLogger;
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
        File configFile = new File(this.CONFIG_PATH, this.CONFIG_NAME);

        this.config = YamlConfiguration.loadConfiguration(configFile);
        //PluginLogger.debug("Config loaded: " + configFile.getPath());
    }

    public FileConfiguration getConfig() {
        if (this.config == null) {
            loadConfig();
        }
        return config;
    }
}
